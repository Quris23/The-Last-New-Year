package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

/**
 * An arm without drawn blades looks only at how much blood was splashed over the whole body and ignores what its own strikes
 * left on it, so a weak punch or blood from blades that are now retracted showed nothing. When that lookup finds nothing, this
 * also accepts the blood recorded for this hand while it was empty (item id 0) or had blades drawn (Hemodynamics' id -2).
 */
@Pseudo
@Mixin(targets = "breenihilation.hemodynamics.client.body.BloodHandBlood", remap = false)
public abstract class HemodynamicsArmInkMixin {
    private static final int[] SPELLCLASSES$STRUCK_WITH = {0, -2};
    private static Method spellclasses$held;
    private static boolean spellclasses$broken;

    @Inject(method = "arm(Lnet/minecraft/world/entity/LivingEntity;I)Lbreenihilation/hemodynamics/client/body/BloodHandBlood;",
            at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$ownStrikes(LivingEntity entity, int side, CallbackInfoReturnable<Object> cir) {
        if (cir.getReturnValue() != null || spellclasses$broken) return;
        HumanoidArm arm = side == 0 ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        int hand = arm == entity.getMainArm() ? 0 : 1;
        try {
            if (spellclasses$held == null) {
                spellclasses$held = Class.forName("breenihilation.hemodynamics.client.body.BloodHandBlood")
                        .getDeclaredMethod("held", LivingEntity.class, int.class, int.class, int.class);
                spellclasses$held.setAccessible(true);
            }
            for (int item : SPELLCLASSES$STRUCK_WITH) {
                Object blood = spellclasses$held.invoke(null, entity, hand, item, side * 949 ^ 727774);
                if (blood != null) {
                    cir.setReturnValue(blood);
                    return;
                }
            }
        } catch (Throwable t) {
            spellclasses$broken = true;
        }
    }
}
