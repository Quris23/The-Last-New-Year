package lny.stockticker;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireCombat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The portable stock ticker in a hands socket slides out of the right wrist before its menu opens, as Chromeware's mantis
 * blades deploy, and back in once the menu is closed. Hardware deployed on the right arm goes back in first, the ticker
 * following once it has (the client keeps that arm's hardware out of sight meanwhile, StockTickerArm). With gorilla arms
 * on the right, or a wrist that could not deploy blades either (riding, swimming, gliding, the arm lost), the menu opens at
 * once. Registered only when Chromeware and Create: Mobile Packages are installed.
 */
public final class StockTickerDeploy {
   private static final HumanoidArm SIDE = HumanoidArm.RIGHT;
   private static final Map<UUID, Out> OUT = new HashMap<>();

   private StockTickerDeploy() {
   }

   /** On the key: whether the ticker started sliding out (the menu opens after), or was already on its way */
   public static boolean start(ServerPlayer player) {
      if (OUT.containsKey(player.getUUID())) {
         return true;
      }
      if (!wrist(player)) {
         return false;
      }
      Out out = new Out();
      if (ArmControls.deployedOn(player, SIDE)) {
         MantisImplant.retract(player, SIDE);
         ProjectileLauncher.retract(player, SIDE);
         MonowireCombat.retract(player, SIDE);
         out.waiting = MantisImplant.DEPLOY_TICKS;
      }
      OUT.put(player.getUUID(), out);
      if (out.waiting == 0) {
         slide(player);
      }
      return true;
   }

   private static boolean wrist(Player player) {
      if (ArmImplants.get(player, SIDE).replacesLimb() || !ArmHardware.free(player, SIDE) || !player.isAlive() || player.isSpectator()) {
         return false;
      }
      LimbState body = LimbAttachments.get(player);
      return !body.isMissing(BodyPart.BODY) && !body.isMissing(ArmImplants.part(SIDE)) && !body.isMissing(ArmImplants.handPart(SIDE));
   }

   private static void slide(ServerPlayer player) {
      send(player, true);
      player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.4F, 1.4F);
   }

   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      Out out = OUT.get(player.getUUID());
      if (out == null) {
         return;
      }
      if (out.waiting > 0) {
         // The right arm's hardware still going back in
         if (--out.waiting == 0) {
            if (!player.isAlive() || StockTickerImplant.find(player).isEmpty()) {
               OUT.remove(player.getUUID());
            } else if (!wrist(player)) {
               OUT.remove(player.getUUID());
               StockTickerImplant.open(player);
            } else {
               slide(player);
            }
         }
      } else if (!out.opened) {
         if (++out.ticks < MantisImplant.DEPLOY_TICKS) {
            return;
         }
         out.opened = true;
         if (!player.isAlive() || StockTickerImplant.find(player).isEmpty() || !StockTickerImplant.open(player)) {
            retract(player, out);
         }
      } else if (!StockTickerImplant.isMenu(player.containerMenu)) {
         retract(player, out);
      }
   }

   private static void retract(ServerPlayer player, Out out) {
      OUT.remove(player.getUUID());
      send(player, false);
      player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.35F, 1.4F);
   }

   // Players coming into view see the ticker as it is, and stop seeing one that went back in while they were away
   public static void onStartTracking(PlayerEvent.StartTracking event) {
      if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer tracker) {
         Out out = OUT.get(target.getUUID());
         PacketDistributor.sendToPlayer(tracker, new StockTickerNetwork.Arm(target.getId(), SIDE == HumanoidArm.LEFT, out != null && out.waiting == 0));
      }
   }

   public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      OUT.remove(event.getEntity().getUUID());
   }

   private static void send(ServerPlayer player, boolean out) {
      PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new StockTickerNetwork.Arm(player.getId(), SIDE == HumanoidArm.LEFT, out));
   }

   private static final class Out {
      // Ticks left of the right arm's hardware going back in, before the ticker slides out
      int waiting;
      int ticks;
      boolean opened;
   }
}
