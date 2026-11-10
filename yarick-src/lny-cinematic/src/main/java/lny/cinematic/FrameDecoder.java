package lny.cinematic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

// Decodes the video's jpg frames (1920x1080) a few frames ahead of the one being shown, on THREADS threads of their own;
// frames it fell behind on are skipped, not caught up on. Until warmedUp() it times how long a frame takes to decode (once
// the first frames are ready it decodes one of them again every WARM_UP_PAUSE), for the cinematic to start when that is
// fast enough.
final class FrameDecoder {
   static final int THREADS = 2;
   private static final int AHEAD = 8;
   private static final long WARM_UP_PAUSE = 50_000_000L; // nanoseconds: a sample now and then, not cores taken from the game

   record Frame(int index, ByteBuffer pixels, int width, int height, int channels) {
      void free() {
         STBImage.stbi_image_free(this.pixels);
      }
   }

   private final int count;
   private final ConcurrentSkipListMap<Integer, Frame> decoded = new ConcurrentSkipListMap<>();
   private final AtomicInteger next = new AtomicInteger();
   private final AtomicInteger warmUps = new AtomicInteger();
   private volatile int wanted;
   private volatile boolean stopped;
   private volatile boolean warmingUp = true;
   private volatile double decodeSeconds = Double.NaN; // how long a frame took to decode lately, while warming up

   FrameDecoder(int count) {
      this.count = count;
   }

   void start() {
      for (int i = 0; i < THREADS; i++) {
         Thread thread = new Thread(this::run, "LNY cinematic frames #" + i);
         thread.setDaemon(true);
         // above the game's loading threads (Java's NORM_PRIORITY + 1 still is Windows' normal one)
         thread.setPriority(Thread.NORM_PRIORITY + 2);
         thread.start();
      }
   }

   void want(int index) {
      this.wanted = index;
   }

   // Whether frames decode within that many seconds now, as far as warming up has found out
   boolean decodesWithin(double seconds) {
      double time = this.decodeSeconds;
      return time <= seconds; // false while it is NaN
   }

   double decodeSeconds() {
      return this.decodeSeconds;
   }

   void warmedUp() {
      this.warmingUp = false;
   }

   // The newest decoded frame not after index (older ones are dropped), or null if there is none new; the caller frees it
   Frame take(int index) {
      Map.Entry<Integer, Frame> entry = this.decoded.floorEntry(index);
      if (entry == null) {
         return null;
      }
      for (Integer older : this.decoded.headMap(entry.getKey()).keySet()) {
         Frame frame = this.decoded.remove(older);
         if (frame != null) {
            frame.free();
         }
      }
      return this.decoded.remove(entry.getKey());
   }

   void stop() {
      this.stopped = true;
      this.freeAll();
   }

   private void freeAll() {
      for (Integer index : this.decoded.keySet()) {
         Frame frame = this.decoded.remove(index);
         if (frame != null) {
            frame.free();
         }
      }
   }

   static ByteBuffer read(String path) throws IOException {
      try (InputStream in = FrameDecoder.class.getResourceAsStream(path)) {
         if (in == null) {
            throw new IOException("Missing " + path);
         }
         byte[] bytes = in.readAllBytes();
         ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
         buffer.put(bytes).flip();
         return buffer;
      }
   }

   static Frame decode(String path, int index, int channels) throws IOException {
      ByteBuffer file = read(path);
      try {
         int[] w = new int[1];
         int[] h = new int[1];
         int[] c = new int[1];
         ByteBuffer pixels = STBImage.stbi_load_from_memory(file, w, h, c, channels);
         if (pixels == null) {
            throw new IOException("Can't decode " + path + ": " + STBImage.stbi_failure_reason());
         }
         return new Frame(index, pixels, w[0], h[0], channels);
      } finally {
         MemoryUtil.memFree(file);
      }
   }

   private void run() {
      while (!this.stopped) {
         int wanted = this.wanted;
         int index = this.next.get();
         if (index < wanted) {
            this.next.compareAndSet(index, wanted); // fell behind: start again from the frame on screen
            continue;
         }
         if (index >= Math.min(this.count, wanted + AHEAD)) {
            if (this.warmingUp) {
               this.warmUp();
               LockSupport.parkNanos(WARM_UP_PAUSE);
            } else {
               LockSupport.parkNanos(2_000_000L);
            }
            continue;
         }
         if (!this.next.compareAndSet(index, index + 1)) {
            continue;
         }
         try {
            long start = System.nanoTime();
            Frame frame = decode(path(index), index, 3);
            this.timed(start);
            if (index < this.wanted) {
               frame.free(); // too late to be shown
            } else {
               this.decoded.put(index, frame);
               if (this.stopped) {
                  this.freeAll();
               }
            }
         } catch (Throwable e) {
            CinematicWindow.LOGGER.error("Can't decode cinematic frame {}", index, e);
         }
      }
   }

   private static String path(int index) {
      return String.format("/lnycinematic/video/%04d.jpg", index);
   }

   // Decodes one of the first frames again only to time it, the result is dropped
   private void warmUp() {
      int index = Math.floorMod(this.warmUps.getAndIncrement(), Math.min(AHEAD, this.count));
      try {
         long start = System.nanoTime();
         decode(path(index), index, 3).free();
         this.timed(start);
      } catch (Throwable e) {
         CinematicWindow.LOGGER.error("Can't decode cinematic frame {}", index, e);
         this.warmingUp = false;
      }
   }

   private void timed(long start) {
      if (this.warmingUp) {
         double seconds = (System.nanoTime() - start) / 1.0E9;
         double average = this.decodeSeconds;
         this.decodeSeconds = Double.isNaN(average) ? seconds : average * 0.75 + seconds * 0.25;
      }
   }
}
