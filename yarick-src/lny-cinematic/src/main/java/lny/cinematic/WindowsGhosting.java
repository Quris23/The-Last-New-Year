package lny.cinematic;

import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.User32;

/**
 * Turns off Windows' "not responding" ghost window for the game, from the very start of the launch (moved here from
 * witherstormmod, which could do it only once mods are constructed).
 * <p>
 * Applying mixins, and later opening a world, the main or render thread works for a long while without handling window
 * messages. Windows then puts its "not responding" ghost window over the game, and its hang reporter (WerFault /h) suspends
 * every thread of the process to look at it; on this machine that reporter got stuck once, so the game stayed suspended for
 * good. Without ghosting the window just looks frozen until the work is done. Called through LWJGL, which the early window
 * has at hand already (no JNA on its class path).
 */
final class WindowsGhosting {
   private WindowsGhosting() {
   }

   static void disable() {
      if (!System.getProperty("os.name", "").startsWith("Windows")) {
         return;
      }
      try {
         long function = User32.getLibrary().getFunctionAddress("DisableProcessWindowsGhosting");
         if (function != 0L) {
            JNI.invokeV(function);
            CinematicWindow.LOGGER.info("Window ghosting turned off");
         }
      } catch (Throwable e) {
         CinematicWindow.LOGGER.warn("Failed to turn off window ghosting", e);
      }
   }
}
