package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tornado (Wind's Spellbooks) is two and a half times as large: the radius drives both the hit area and the model scale. */
@Mixin(targets = "net.raptorzizi.wind_spellbooks.spells.wind.TornadoSpell", remap = false)
public abstract class WindTornadoSizeMixin {
    @Inject(method = "getRadius", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$doubleRadius(int level, net.minecraft.world.entity.LivingEntity caster,
                                           CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2.5f);
    }
}
