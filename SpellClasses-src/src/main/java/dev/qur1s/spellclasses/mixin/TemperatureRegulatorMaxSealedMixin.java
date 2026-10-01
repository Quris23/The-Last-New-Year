package dev.qur1s.spellclasses.mixin;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Northstar's own {@code getMaximumSealedBlocks()} is {@code config[sealer.temperature].blocksPerRpm *
 * |speed|} (config default: 16 blocks/rpm - the pack's observed 4096 cap at 256 rpm matches this
 * exactly: 16 * 256 = 4096). By user design, kept as the same linear-in-speed formula but recalibrated
 * so the cap is 33511 blocks at that same 256 rpm (33511 / 256 ≈ 130.902 blocks/rpm), rather than the
 * config's 16 - a flat override would have broken the speed-scaling the block is designed around. This
 * feeds both Northstar's own goggle tooltip and {@link dev.qur1s.spellclasses.NorthstarSphereShieldsSupport}'s
 * dome-vs-capacity check, since both read this same method.
 */
@Mixin(value = TemperatureRegulatorBlockEntity.class, remap = false)
public abstract class TemperatureRegulatorMaxSealedMixin {
    private static final float BLOCKS_PER_RPM = 33511f / 256f;

    @Inject(method = "getMaximumSealedBlocks", at = @At("HEAD"), cancellable = true)
    private void spellclasses$rescaleMaxSealedBlocks(CallbackInfoReturnable<Integer> cir) {
        TemperatureRegulatorBlockEntity self = (TemperatureRegulatorBlockEntity) (Object) this;
        cir.setReturnValue((int) (BLOCKS_PER_RPM * Math.abs(self.getSpeed())));
    }
}
