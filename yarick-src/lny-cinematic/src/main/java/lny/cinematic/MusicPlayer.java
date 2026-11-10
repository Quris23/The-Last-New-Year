package lny.cinematic;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

// Plays the cinematic's ogg through Java Sound (Minecraft's OpenAL device is not open yet, and opening one of our own would
// fight with it later). The line is started on silence as soon as it opens: Java Sound only really starts the device with
// the first write, which can take a while, and the music should start the moment play() comes. The music's position on the
// line is the clock the video follows. This thread reads it after every write and the clock goes on from there by the wall
// clock, so that drawing never waits on the line (Java Sound locks it while writing). Before play() the clock stands at 0;
// without a sound device or once the music is over the wall clock carries on alone.
final class MusicPlayer {
   private static final int CHUNK = 2048; // frames per write
   private static final double PRE_ROLL = 0.04; // seconds of silence kept queued until play()
   private static final double SKIP_OVER = 0.1; // seconds the video may be ahead when the music starts before it skips on
   private static final double LINE_SLACK = 0.25; // seconds the clock goes on past the line's last position

   // Music seconds at nanos and how they go on from there: by the wall clock, or as far as the line can be ahead of a
   // position it told
   private record Clock(double seconds, long nanos, boolean wall) {
      double at(long now) {
         double since = (now - this.nanos) / 1.0E9;
         return this.seconds + (this.wall ? since : Math.min(since, LINE_SLACK));
      }
   }

   private final ByteBuffer ogg;
   private final float volume;
   private final CountDownLatch play = new CountDownLatch(1);
   private volatile boolean ready; // the line plays (silence so far), or can't: play() starts the music at once
   private volatile Clock clock; // null until play()
   private volatile double shown; // the latest seconds() handed out, the clock never goes back from it
   private volatile boolean stopped;
   private volatile float fadeSeconds = -1.0F;

   MusicPlayer(ByteBuffer ogg, float volume) {
      this.ogg = ogg;
      this.volume = volume;
   }

   // Opens and starts the sound device, which can take a while (Java Sound starting up, the computer busy); the music
   // waits for play()
   void open() {
      Thread thread = new Thread(this::run, "LNY cinematic music");
      thread.setDaemon(true);
      thread.setPriority(Thread.MAX_PRIORITY); // so that it doesn't stutter while the game loads on every core
      thread.start();
   }

   boolean ready() {
      return this.ready;
   }

   void play() {
      this.clock = new Clock(0.0, System.nanoTime(), true);
      this.play.countDown();
   }

   double seconds() {
      Clock clock = this.clock;
      if (clock == null) {
         return 0.0;
      }
      double seconds = Math.max(this.shown, clock.at(System.nanoTime()));
      this.shown = seconds;
      return seconds;
   }

   void fadeOut(float seconds) {
      if (this.clock == null) {
         this.stop(); // never played
      } else if (this.fadeSeconds < 0.0F) {
         this.fadeSeconds = Math.max(seconds, 0.01F);
      }
   }

   void stop() {
      this.stopped = true;
      this.play.countDown();
   }

   private void run() {
      long decoder = 0L;
      SourceDataLine line = null;
      try (MemoryStack stack = MemoryStack.stackPush()) {
         IntBuffer error = stack.mallocInt(1);
         decoder = STBVorbis.stb_vorbis_open_memory(this.ogg, error, null);
         if (decoder == 0L) {
            CinematicWindow.LOGGER.error("Can't decode the cinematic music, error {}", error.get(0));
            return;
         }
         STBVorbisInfo info = STBVorbisInfo.malloc(stack);
         STBVorbis.stb_vorbis_get_info(decoder, info);
         int channels = info.channels();
         int rate = info.sample_rate();
         int frameBytes = channels * 2;

         AudioFormat format = new AudioFormat(rate, 16, channels, true, false);
         line = AudioSystem.getSourceDataLine(format);
         line.open(format, rate / 5 * frameBytes);
         line.start();
         long written = this.preRoll(line, rate, frameBytes);
         if (this.stopped) {
            return;
         }
         // the video has gone on by the wall clock since play(): if that is more than a moment (the line was slow to
         // start), the music skips as far, otherwise the video waits for it
         double ahead = this.clock.at(System.nanoTime());
         int skip = ahead > SKIP_OVER ? (int)(ahead * rate) : 0;
         if (skip > 0) {
            STBVorbis.stb_vorbis_seek(decoder, skip);
         }
         // music seconds at the line's frame 0, which came before the silence
         double lineStart = (skip - written) / (double)rate;

         ShortBuffer pcm = MemoryUtil.memAllocShort(CHUNK * channels);
         byte[] bytes = new byte[CHUNK * frameBytes];
         float gain = this.volume;
         try {
            while (!this.stopped) {
               int frames = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
               if (frames <= 0) {
                  break;
               }
               float step = this.fadeSeconds > 0.0F ? this.volume / (this.fadeSeconds * rate) : 0.0F;
               for (int f = 0; f < frames; f++) {
                  if (step > 0.0F) {
                     gain = Math.max(0.0F, gain - step);
                  }
                  for (int c = 0; c < channels; c++) {
                     int i = f * channels + c;
                     int s = Math.round(pcm.get(i) * gain);
                     bytes[i * 2] = (byte)s;
                     bytes[i * 2 + 1] = (byte)(s >> 8);
                  }
               }
               line.write(bytes, 0, frames * frameBytes);
               this.clock = new Clock(lineStart + line.getLongFramePosition() / (double)rate, System.nanoTime(), false);
               if (step > 0.0F && gain <= 0.0F) {
                  break;
               }
            }
            if (!this.stopped) {
               line.drain();
               this.clock = new Clock(lineStart + line.getLongFramePosition() / (double)rate, System.nanoTime(), false);
            }
         } finally {
            MemoryUtil.memFree(pcm);
         }
      } catch (Throwable e) {
         CinematicWindow.LOGGER.error("The cinematic music failed, the video follows the wall clock", e);
      } finally {
         this.ready = true;
         Clock clock = this.clock;
         if (clock != null && !clock.wall()) {
            long now = System.nanoTime();
            this.clock = new Clock(clock.at(now), now, true);
         }
         if (line != null) {
            line.stop();
            line.close();
         }
         if (decoder != 0L) {
            STBVorbis.stb_vorbis_close(decoder);
         }
      }
   }

   // Keeps the started line playing silence, a little at a time, until play() or stop(); ready once it moves. Returns how
   // many frames of it were written.
   private long preRoll(SourceDataLine line, int rate, int frameBytes) throws InterruptedException {
      byte[] silence = new byte[rate / 100 * frameBytes]; // 10 ms
      int queue = (int)(PRE_ROLL * rate) * frameBytes;
      long written = 0L;
      while (!this.stopped && this.play.getCount() > 0L) {
         if (line.getBufferSize() - line.available() < queue) {
            line.write(silence, 0, silence.length); // the first write is what really starts the device
            written += silence.length / frameBytes;
         } else {
            if (!this.ready && line.getLongFramePosition() > 0L) {
               this.ready = true;
            }
            this.play.await(5L, TimeUnit.MILLISECONDS);
         }
      }
      return written;
   }
}
