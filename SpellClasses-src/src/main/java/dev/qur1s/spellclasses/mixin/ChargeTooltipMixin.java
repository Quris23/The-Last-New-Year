package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.ChargedBuffTweaks;
import io.redspace.ironsspellbooks.spells.lightning.ChargeSpell;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps Overload's spellbook tooltip in sync with {@link ChargedBuffTweaks}' 25%/level movement speed. */
@Mixin(value = ChargeSpell.class, remap = false)
public abstract class ChargeTooltipMixin {
    private static final float DISPLAY_MULTIPLIER = 5.0f;

    @Inject(method = "getPercentSpeed", at = @At("RETURN"), cancellable = true)
    private void spellclasses$fixSpeedTooltip(int spellLevel, LivingEntity entity, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * DISPLAY_MULTIPLIER);
    }
}
