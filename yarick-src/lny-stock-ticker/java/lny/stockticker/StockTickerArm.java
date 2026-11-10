package lny.stockticker;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

/**
 * What the client knows of the portable stock tickers out of the players' wrists (StockTickerDeploy). While one is out, or
 * still sliding back in, Chromeware draws that wrist as fitted with mantis blades: the skin opens, the frame swings out
 * and the ticker sits where the blade would be. Nothing of the server's state is kept here.
 */
public final class StockTickerArm {
   // The mantis blades' retract animation (16 ticks) and a little more, after which the wrist is drawn as the player's own again
   private static final int RETRACT_TICKS = 20;
   private static final Map<UUID, View> VIEWS = new ConcurrentHashMap<>();

   private StockTickerArm() {
   }

   public static void show(Player player, HumanoidArm side, boolean out) {
      if (out || VIEWS.containsKey(player.getUUID())) {
         VIEWS.put(player.getUUID(), new View(side, out, player.level().getGameTime()));
      }
   }

   /** Whether that wrist is opened for the ticker, on the client */
   public static boolean shown(Player player, HumanoidArm side) {
      View view = VIEWS.get(player.getUUID());
      if (view == null || view.side != side) {
         return false;
      }
      if (!view.out && player.level().getGameTime() - view.since > RETRACT_TICKS) {
         VIEWS.remove(player.getUUID(), view);
         return false;
      }
      return true;
   }

   /** Whether the ticker is out of that wrist or on its way out, rather than sliding back in */
   public static boolean out(Player player, HumanoidArm side) {
      View view = VIEWS.get(player.getUUID());
      return view != null && view.side == side && view.out;
   }

   public static void clear() {
      VIEWS.clear();
   }

   private record View(HumanoidArm side, boolean out, long since) {
   }
}
