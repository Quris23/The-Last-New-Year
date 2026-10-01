package dev.qur1s.spellclasses.mixin;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import com.lightning.northstar.world.sealer.ProgressiveBlockSealer;
import dev.qur1s.spellclasses.NorthstarSphereShieldsSupport;
import dev.qur1s.spellclasses.SealerLeakSuppression;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * When a regulator is sealed via a SphereShields dome instead of Northstar's own room detector (see
 * {@link NorthstarSphereShieldsSupport#isSealedByField}), Northstar's own leak-flood check still runs
 * and still thinks the room is unsealed/too large - it has no idea the dome exists. That produces a
 * "leak" particle trail and a misleading "Area too big or unsealed!" goggle tooltip line even though
 * the regulator is, by user design, working correctly through the field. Both come from the same
 * {@code ProgressiveBlockSealer} instance (via {@code getSealer()}), called once per invocation from
 * {@code tick()} and once from {@code addToGoggleTooltip(List, boolean)} - redirecting those two call
 * sites (rather than cancelling the whole methods) skips just the leak visuals/text while leaving any
 * other goggle info (load, target temperature) intact.
 */
@Mixin(value = TemperatureRegulatorBlockEntity.class, remap = false)
public abstract class TemperatureRegulatorGoggleLeakMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/lightning/northstar/world/sealer/ProgressiveBlockSealer;renderLeakPath(Lnet/minecraft/world/level/Level;)V"))
    private void spellclasses$skipLeakParticles(ProgressiveBlockSealer sealer, Level level) {
        TemperatureRegulatorBlockEntity self = (TemperatureRegulatorBlockEntity) (Object) this;
        if (!NorthstarSphereShieldsSupport.isSealedByField(self)) {
            sealer.renderLeakPath(level);
        }
    }

    @Redirect(method = "addToGoggleTooltip(Ljava/util/List;Z)Z", at = @At(value = "INVOKE",
            target = "Lcom/lightning/northstar/world/sealer/ProgressiveBlockSealer;addToGoggleTooltip(Ljava/util/List;IZ)V"))
    private void spellclasses$rewriteLeakTooltip(ProgressiveBlockSealer sealer, List<Component> tooltip, int indent, boolean isPlayerSneaking) {
        TemperatureRegulatorBlockEntity self = (TemperatureRegulatorBlockEntity) (Object) this;
        SealerLeakSuppression.active = NorthstarSphereShieldsSupport.isSealedByField(self);
        try {
            sealer.addToGoggleTooltip(tooltip, indent, isPlayerSneaking);
        } finally {
            SealerLeakSuppression.active = false;
        }
    }
}
