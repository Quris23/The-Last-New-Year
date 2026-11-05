package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Counterspell (and other anti-magic) hits any living creature, not only players and Iron's casters. */
@Mixin(value = Utils.class, remap = false)
public abstract class CounterspellTargetsMixin {
    @Inject(method = "validAntiMagicTarget", at = @At("RETURN"), cancellable = true, require = 0)
    private static void spellclasses$anyLiving(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && entity instanceof LivingEntity && !entity.isSpectator()) cir.setReturnValue(true);
    }
}
