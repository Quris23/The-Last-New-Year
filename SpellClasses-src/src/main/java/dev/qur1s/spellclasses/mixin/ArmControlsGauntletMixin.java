package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.GauntletImplants;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The gauntlets join Chromeware's arm implants: combat mode (deploy/retract) and the buttons of both arms. */
@Pseudo
@Mixin(targets = "com.chromeware.cyberware.ArmControls", remap = false)
public abstract class ArmControlsGauntletMixin {
    private static final ThreadLocal<Boolean> WAS_DEPLOYED = new ThreadLocal<>();

    @Inject(method = "deployable(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$deployable(Player p, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && GauntletImplants.has(p)) cir.setReturnValue(true);
    }

    @Inject(method = "anyDeployed(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$anyDeployed(Player p, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && GauntletImplants.deployed(p) && GauntletImplants.has(p)) cir.setReturnValue(true);
    }

    @Inject(method = "deployedOn(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)Z", at = @At("RETURN"),
            cancellable = true, remap = false, require = 0)
    private static void spellclasses$deployedOn(Player p, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && GauntletImplants.deployedOn(p, side)) cir.setReturnValue(true);
    }

    @Inject(method = "toggle(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), remap = false, require = 0)
    private static void spellclasses$toggleStart(Player p, CallbackInfo ci) {
        WAS_DEPLOYED.set(com.chromeware.cyberware.ArmControls.anyDeployed(p));
    }

    @Inject(method = "toggle(Lnet/minecraft/world/entity/player/Player;)V", at = @At("RETURN"), remap = false, require = 0)
    private static void spellclasses$toggleEnd(Player p, CallbackInfo ci) {
        Boolean was = WAS_DEPLOYED.get();
        WAS_DEPLOYED.remove();
        if (p.level().isClientSide()) return;
        if (was != null && !was && GauntletImplants.has(p)) {
            GauntletImplants.setDeployed(p, true);
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Боевой режим: включён"), true);
        } else if (was != null && was && GauntletImplants.has(p)) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Боевой режим: выключен"), true);
        }
    }

    @Inject(method = "act(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 0)
    private static void spellclasses$act(Player p, HumanoidArm side, CallbackInfo ci) {
        if (!p.level().isClientSide() && GauntletImplants.deployedOn(p, side)) {
            GauntletImplants.act(p, side);
            ci.cancel();
        }
    }

    @Inject(method = "charge(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 0)
    private static void spellclasses$charge(Player p, HumanoidArm side, CallbackInfo ci) {
        if (!p.level().isClientSide() && GauntletImplants.deployedOn(p, side)) {
            GauntletImplants.charge(p, side);
            ci.cancel();
        }
    }

    @Inject(method = "release(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 0)
    private static void spellclasses$release(Player p, HumanoidArm side, CallbackInfo ci) {
        if (!p.level().isClientSide() && GauntletImplants.deployedOn(p, side)) {
            GauntletImplants.release(p, side);
            ci.cancel();
        }
    }

    @Inject(method = "cancel(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)V", at = @At("HEAD"),
            remap = false, require = 0)
    private static void spellclasses$cancel(Player p, HumanoidArm side, CallbackInfo ci) {
        if (!p.level().isClientSide()) GauntletImplants.cancel(p, side);
    }

    /** Retracting the arm implants (also on installing or removing one) ends the gauntlets' combat mode. */
    @Pseudo
    @Mixin(targets = "com.chromeware.cyberware.ArmImplants", remap = false)
    public abstract static class Retract {
        @Inject(method = "retractAll(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), remap = false, require = 0)
        private static void spellclasses$retract(Player p, CallbackInfo ci) {
            GauntletImplants.retract(p);
        }
    }

    /** Client: the buttons of an arm with a gauntlet are held (charged and released), like the Gorilla Arms. */
    @Pseudo
    @Mixin(targets = "com.chromeware.client.cyberware.ArmInput", remap = false)
    public abstract static class Input {
        @Inject(method = "chargeable(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)Z",
                at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$chargeable(Player p, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValueZ() && GauntletImplants.deployedOn(p, side)) cir.setReturnValue(true);
        }
    }

    /** Installing: a held gauntlet goes into an arm socket like the other arm hardware. */
    @Pseudo
    @Mixin(targets = "com.chromeware.cyberware.Installing", remap = false)
    public abstract static class Install {
        @Inject(method = "supported(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$supported(net.minecraft.world.item.ItemStack item, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValueZ() && GauntletImplants.isGauntlet(item)) cir.setReturnValue(true);
        }

        @Inject(method = "installHeld(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
        private static void spellclasses$installHeld(Player player, CallbackInfo ci) {
            if (GauntletImplants.isGauntlet(player.getMainHandItem()) || GauntletImplants.isGauntlet(player.getOffhandItem())) {
                com.chromeware.cyberware.ArmHardware.installHeld(player, GauntletImplants::isGauntlet, false);
                ci.cancel();
            }
        }
    }
}
