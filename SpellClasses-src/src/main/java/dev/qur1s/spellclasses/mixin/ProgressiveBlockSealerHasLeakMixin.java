package dev.qur1s.spellclasses.mixin;

import com.lightning.northstar.world.sealer.ProgressiveBlockSealer;
import dev.qur1s.spellclasses.SealerLeakSuppression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Forces {@code hasLeak()} to read false for just the duration of an
 * {@code addToGoggleTooltip(List, int, boolean)} call while
 * {@link SealerLeakSuppression#active} is set (see {@link TemperatureRegulatorGoggleLeakMixin}), so a
 * SphereShields-sealed regulator's tooltip shows the normal "blocks filled" line instead of the
 * misleading "area too big / unsealed" warning, without hiding the sealed-block count entirely and
 * without affecting other {@code ProgressiveBlockSealer} users (the flag stays false for them).
 */
@Mixin(value = ProgressiveBlockSealer.class, remap = false)
public abstract class ProgressiveBlockSealerHasLeakMixin {

    @Redirect(method = "addToGoggleTooltip(Ljava/util/List;IZ)V", at = @At(value = "INVOKE",
            target = "Lcom/lightning/northstar/world/sealer/ProgressiveBlockSealer;hasLeak()Z"))
    private boolean spellclasses$forceNoLeakForTooltip(ProgressiveBlockSealer self) {
        return !SealerLeakSuppression.active && self.hasLeak();
    }
}
