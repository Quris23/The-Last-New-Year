package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.spells.fire.FireArrowSpell;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fire Arrow: 10-block explosion radius (was 3), double the cast time (was 20 ticks), triple damage. */
@Mixin(value = FireArrowSpell.class, remap = false)
public abstract class FireArrowMixin {
    private static final float NEW_RADIUS = 10.0f;
    private static final int NEW_CAST_TIME = 40;
    private static final float DAMAGE_MULTIPLIER = 3.0f;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 20))
    private int spellclasses$fasterCast(int original) {
        return NEW_CAST_TIME;
    }

    @Inject(method = "getRadius", at = @At("HEAD"), cancellable = true)
    private void spellclasses$widenRadius(int spellLevel, LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(NEW_RADIUS);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void spellclasses$tripleDamage(int spellLevel, LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * DAMAGE_MULTIPLIER);
    }
}
