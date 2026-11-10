package lny.octotilt;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Whether the server has just come out of a stall. While it hangs, clients keep moving and queue up their movement; the
 * first tick after it handles them all at once, so a player seems to cover seconds of travel in one tick. Checks that
 * measure how far a player moves per tick (Octo Arms' rig speed check) let these ticks pass.
 */
public final class ServerLagGrace {
   // A tick starting this long after the one before ends a stall (a tick is 50 ms)
   private static final long STALL_NANOS = 250_000_000L;
   // Ticks after a stall the movement checks wait: queued movement plus their own 10 tick windows
   private static final int GRACE_TICKS = 40;
   private static long lastTick;
   private static int left;

   private ServerLagGrace() {
   }

   public static void onServerTick(ServerTickEvent.Pre event) {
      long now = System.nanoTime();
      if (lastTick != 0L && now - lastTick > STALL_NANOS) {
         left = GRACE_TICKS;
      } else if (left > 0) {
         left--;
      }
      lastTick = now;
   }

   public static boolean active() {
      return left > 0;
   }
}
