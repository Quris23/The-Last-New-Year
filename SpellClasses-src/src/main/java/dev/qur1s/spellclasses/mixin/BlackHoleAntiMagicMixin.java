package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iron's Black Hole is marked as vulnerable to anti-magic (Counterspell hits it), but its handler does nothing. Now
 * Counterspell (and any other anti-magic) dispels a Black Hole: it disappears at once.
 */
@Mixin(value = BlackHole.class, remap = false)
public abstract class BlackHoleAntiMagicMixin {
    @Inject(method = "onAntiMagic", at = @At("HEAD"), require = 0)
    private void spellclasses$dispel(MagicData magicData, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.level().isClientSide) self.discard();
    }
}
