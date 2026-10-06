package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Wind Jump (Wind's Spellbooks) grants Iron's "Ascension" (reduced gravity) instead of Slow Falling. */
@Mixin(targets = "net.raptorzizi.wind_spellbooks.spells.wind.WindJumpSpell", remap = false)
public abstract class WindJumpAscensionMixin {
    @Redirect(method = "onCast", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            remap = false), require = 0)
    private boolean spellclasses$ascensionInsteadOfSlowFall(LivingEntity entity, MobEffectInstance effect) {
        if (effect.getEffect().is(MobEffects.SLOW_FALLING)) {
            effect = new MobEffectInstance(MobEffectRegistry.ASCENSION, effect.getDuration(), 0, false, true, true);
        }
        return entity.addEffect(effect);
    }
}
