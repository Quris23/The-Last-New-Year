package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wind Jump (Wind's Spellbooks) launches half as hard; the tooltip distance reads the same value. */
@Mixin(targets = "net.raptorzizi.wind_spellbooks.spells.wind.WindJumpSpell", remap = false)
public abstract class WindJumpStrengthMixin {
    @Inject(method = "getJumpStrength", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$halfStrength(int level, LivingEntity caster, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 0.5f);
    }
}
