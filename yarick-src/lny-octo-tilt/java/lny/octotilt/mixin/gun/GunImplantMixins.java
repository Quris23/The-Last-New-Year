package lny.octotilt.mixin.gun;

import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.DisabledNotice;
import lny.octotilt.gun.GunImplantDeploy;
import lny.octotilt.gun.GunImplants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The gun implants (GunImplants) as arm hardware of Chromeware; each applies only when Chromeware is installed
public class GunImplantMixins {
   // Fitted into an arm the way the projectile launch system is
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.Installing", remap = false)
   public static class Install {
      @Inject(method = "supported", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunSupported(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
         if (GunImplants.isImplant(item)) {
            cir.setReturnValue(true);
         }
      }

      @Inject(method = "installHeld", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunInstall(Player player, CallbackInfo ci) {
         if (GunImplants.isImplant(player.getMainHandItem()) || !ownSupported(player.getMainHandItem()) && GunImplants.isImplant(player.getOffhandItem())) {
            GunImplantDeploy.clean(player.getMainHandItem());
            GunImplantDeploy.clean(player.getOffhandItem());
            ArmHardware.installHeld(player, GunImplants::isImplant, true);
            ci.cancel();
         }
      }

      // what Chromeware would pick from the main hand before looking at the off hand
      private static boolean ownSupported(ItemStack stack) {
         return com.chromeware.cyberware.Installing.supported(stack);
      }
   }

   // The surgery chair sorts what it is handed: arm hardware goes into an arm, anything else into an implant socket
   @Pseudo
   @Mixin(targets = "com.chromeware.surgery.Surgery", remap = false)
   public static class Chair {
      @Inject(method = "armHardware", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunArmHardware(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
         if (GunImplants.isImplant(stack)) {
            cir.setReturnValue(true);
         }
      }
   }

   // What an implant takes of the implant capacity: the gun implants weigh as Chromeware's own arm weapons
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.CyberwareStats", remap = false)
   public static class Stats {
      @Inject(method = "get", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunCapacity(ItemStack item, CallbackInfoReturnable<com.chromeware.cyberware.CyberwareStats.Stats> cir) {
         if (GunImplants.isImplant(item)) {
            cir.setReturnValue(new com.chromeware.cyberware.CyberwareStats.Stats(GunImplants.CAPACITY, 0));
         }
      }
   }

   // The key that deploys arm hardware. With nothing out, an implanted gun comes first: it slides out alone. The stock
   // ticker's mixin (default priority) is applied after this one, so its own use of the key is asked before
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.ArmControls", remap = false, priority = 900)
   public static class Key {
      @Inject(method = "toggle", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunToggle(Player player, CallbackInfo ci) {
         if (!(player instanceof ServerPlayer serverPlayer) || DisabledNotice.blocked(player)) {
            return;
         }
         if (GunImplantDeploy.out(player) != null) {
            GunImplantDeploy.putAway(serverPlayer);
            ci.cancel();
         } else if (!ArmControls.anyDeployed(player) && GunImplantDeploy.installed(player) && GunImplantDeploy.deploy(serverPlayer)) {
            ci.cancel();
         }
      }

      @Inject(method = "deployable", at = @At("RETURN"), cancellable = true, remap = false)
      private static void octotilt$gunDeployable(Player player, CallbackInfoReturnable<Boolean> cir) {
         if (!cir.getReturnValue() && GunImplantDeploy.installed(player)) {
            cir.setReturnValue(true);
         }
      }
   }

   // Whenever Chromeware puts all arm hardware away (an implant fitted or removed, and so on)
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.ArmImplants", remap = false)
   public static class Retract {
      @Inject(method = "retractAll", at = @At("HEAD"), remap = false)
      private static void octotilt$gunRetract(Player player, CallbackInfo ci) {
         GunImplantDeploy.retractAll(player);
      }

      // before the implant to remove is read: the gun goes back into it first, or it would leave without its gun
      @Inject(method = "remove", at = @At("HEAD"), remap = false)
      private static void octotilt$gunRemove(Player player, net.minecraft.world.entity.HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
         GunImplantDeploy.retractAll(player);
      }
   }

   // The server takes only the colour keys of Chromeware's own implants
   @Pseudo
   @Mixin(targets = "com.chromeware.cyberware.ImplantColors", remap = false)
   public static class Colors {
      @Inject(method = "valid", at = @At("HEAD"), cancellable = true, remap = false)
      private static void octotilt$gunColors(String key, CallbackInfoReturnable<Boolean> cir) {
         int split = key.indexOf(':');
         if (split > 0 && GunImplants.byKey(key.substring(0, split)) != null && GunImplants.PARTS.contains(key.substring(split + 1))) {
            cir.setReturnValue(true);
         }
      }
   }
}
