package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Water Ball (Somake's Spells) deals twice the damage and is twice as large: the radius drives the hit area and the model scale. */
@Mixin(targets = "com.somake.somakespells.spells.aqua.WaterBallSpell", remap = false)
public abstract class WaterBallBoostMixin {
    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$doubleDamage(int level, net.minecraft.world.entity.LivingEntity caster,
                                           CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2f);
    }

    @Inject(method = "getRadius", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$doubleRadius(int level, net.minecraft.world.entity.LivingEntity caster,
                                           CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2f);
    }
}
