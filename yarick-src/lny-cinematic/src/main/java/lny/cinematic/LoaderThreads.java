package lny.cinematic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

// While the cinematic plays, the game's loading pools run below normal priority. With every core busy at normal priority
// the graphics driver's own threads (normal as well, and out of reach) wait for their turn and frames stall for a tenth of
// a second or more; below normal, loading still gets every core nothing else wants, so it is hardly any slower. Threads
// those pools start meanwhile are born below normal too: once the cinematic is over they are put back along with the pools.
final class LoaderThreads {
   private static final String[] POOLS = {
      "background-scan-handler-", // FML scanning the mods
      "modloading-worker-", // FML constructing the mods and sending them events
      "ForkJoinPool.commonPool-worker-",
      "Worker-" // Minecraft's own, unless something put them lower already
   };
   private static final int LOWERED = Thread.NORM_PRIORITY - 2; // still Windows' below normal, and seldom set on purpose
   private static final long EVERY = 250_000_000L; // nanoseconds between looks for new threads

   private final Map<Thread, Integer> lowered = new WeakHashMap<>(); // and the priority each had
   private final Set<Thread> seen = Collections.newSetFromMap(new WeakHashMap<>());
   private final Set<Thread> bornLow = Collections.newSetFromMap(new WeakHashMap<>()); // to a lowered thread, presumably
   private long next;
   private boolean looked;
   private boolean restored;

   // Now and then, while the cinematic plays
   synchronized void lower(long now) {
      if (this.restored || this.looked && now - this.next < 0L) {
         return;
      }
      this.next = now + EVERY;
      this.look();
      for (Thread thread : threads()) {
         int priority = thread.getPriority();
         if (priority >= Thread.NORM_PRIORITY && priority <= Thread.NORM_PRIORITY + 1 && pool(thread.getName())
            && !this.lowered.containsKey(thread)) { // one put back up by someone else stays so
            this.lowered.put(thread, priority);
            thread.setPriority(LOWERED);
         }
      }
   }

   // Once the cinematic is over
   synchronized void restore() {
      if (this.restored) {
         return;
      }
      this.restored = true;
      this.lowered.forEach(LoaderThreads::raise);
      this.look(); // threads born to them since the last look; none can be any more
      this.bornLow.forEach(thread -> raise(thread, Thread.NORM_PRIORITY));
   }

   synchronized int count() {
      return this.lowered.size();
   }

   // New threads born below normal: what was there before the first look was not born to a lowered thread
   private void look() {
      boolean first = !this.looked;
      this.looked = true;
      for (Thread thread : threads()) {
         if (this.seen.add(thread) && !first && thread.getPriority() == LOWERED && !this.lowered.containsKey(thread)) {
            this.bornLow.add(thread);
         }
      }
   }

   private static void raise(Thread thread, int priority) {
      if (thread.isAlive() && thread.getPriority() == LOWERED) {
         thread.setPriority(priority);
      }
   }

   private static boolean pool(String name) {
      for (String pool : POOLS) {
         if (name.startsWith(pool)) {
            return true;
         }
      }
      return false;
   }

   private static Thread[] threads() {
      ThreadGroup group = Thread.currentThread().getThreadGroup();
      while (group.getParent() != null) {
         group = group.getParent();
      }
      Thread[] threads = new Thread[group.activeCount() + 16];
      int count;
      while ((count = group.enumerate(threads, true)) == threads.length) {
         threads = new Thread[threads.length * 2];
      }
      return Arrays.copyOf(threads, count);
   }
}
