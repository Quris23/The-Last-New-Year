package dev.qur1s.spellclasses.mixin;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TemperatureRegulatorBlockEntity.class, remap = false)
public interface TemperatureRegulatorAccessor {
    @Accessor("active")
    boolean spellclasses$isActive();
}
