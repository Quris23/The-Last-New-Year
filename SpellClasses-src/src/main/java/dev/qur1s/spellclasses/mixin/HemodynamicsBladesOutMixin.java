package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hemodynamics asks Chromeware whether the Mantis Blades of an arm are out: null means "no blades here, treat it as a
 * normal hand", FALSE means "blades are installed but retracted" and then the hand is given no blood at all. With Mantis Blades
 * installed that made a bare-handed player stay clean until the blades were drawn. Retracted blades are now treated like a
 * normal hand, so fists get bloody as they do without the implant.
 */
@Pseudo
@Mixin(targets = "breenihilation.hemodynamics.compat.ChromewareCompat", remap = false)
public abstract class HemodynamicsBladesOutMixin {
    @Inject(method = "bladesOut(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/HumanoidArm;)Ljava/lang/Boolean;",
            at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$retractedIsNormalHand(net.minecraft.world.entity.LivingEntity entity,
                                                           net.minecraft.world.entity.HumanoidArm arm,
                                                           CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.FALSE.equals(cir.getReturnValue())) cir.setReturnValue(null);
    }
}
