package dev.qur1s.spellclasses.mixin;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import dev.qur1s.spellclasses.RegulatorSyncTracker;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers this regulator's current state (Northstar's own {@code active} flag, whether it's simply
 * powered/not-overstressed regardless of leaks, and its target temperature) into
 * {@link RegulatorSyncTracker} every tick. The actual Cold Sweat WORLD-trait sync is applied centrally,
 * once per player per check, by {@link dev.qur1s.spellclasses.NorthstarRegulatorColdSweatSync} - see
 * that class and {@link RegulatorSyncTracker} for why this can't be done independently per regulator.
 */
@Mixin(value = TemperatureRegulatorBlockEntity.class, remap = false)
public abstract class TemperatureRegulatorColdSweatSyncMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void spellclasses$registerForSync(CallbackInfo ci) {
        TemperatureRegulatorBlockEntity self = (TemperatureRegulatorBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide()) return;

        boolean active = ((TemperatureRegulatorAccessor) self).spellclasses$isActive();
        boolean powered = Math.abs(self.getSpeed()) > 0 && !self.isOverStressed();

        RegulatorSyncTracker.update(level.dimension(), self.getBlockPos(),
                new RegulatorSyncTracker.RegulatorInfo(self, active, powered, self.getTemperature(), level.getGameTime()));
    }
}
