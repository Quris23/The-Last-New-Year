package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wind Blade (Wind's Spellbooks) deals half its original damage - the tooltip reads the same method. */
@Mixin(targets = "net.raptorzizi.wind_spellbooks.spells.wind.WindBladeSpell", remap = false)
public abstract class WindBladeDamageMixin {
    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$halfDamage(int level, net.minecraft.world.entity.LivingEntity caster,
                                         CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 0.5f);
    }
}
