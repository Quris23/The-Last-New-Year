package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.holy.HasteSpell;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Haste's Hastened effect strength is a flat amplifier=7 regardless of level, which is 20% per
 * {@code HastenedEffect.getPercentForAmplifier} ((1 + amplifier) * 0.025). Raised to amplifier=19
 * for 50%.
 */
@Mixin(value = HasteSpell.class, remap = false)
public abstract class HasteAmplifierMixin {
    private static final int NEW_BASE_AMPLIFIER = 19;

    @Inject(method = "getAmplifier", at = @At("RETURN"), cancellable = true)
    private void spellclasses$strongerHaste(int spellLevel, LivingEntity caster, CallbackInfoReturnable<Integer> cir) {
        float multiplier = ((AbstractSpell) (Object) this).getEntityPowerMultiplier(caster);
        cir.setReturnValue((int) (NEW_BASE_AMPLIFIER * multiplier));
    }
}
