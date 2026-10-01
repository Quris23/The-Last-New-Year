package dev.qur1s.spellclasses;

import com.lightning.northstar.block.tech.temperature_regulator.TemperatureRegulatorBlockEntity;
import dev.qur1s.sphereshields.GroundShieldTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Shared "is this regulator sealed by a SphereShields ground dome" check, used both to sync Cold
 * Sweat's world temperature ({@link dev.qur1s.spellclasses.NorthstarRegulatorColdSweatSync}) and to
 * hide Northstar's own leak particles/tooltip warning ({@link dev.qur1s.spellclasses.mixin.TemperatureRegulatorGoggleLeakMixin}),
 * which are otherwise based entirely on Northstar's own (irrelevant, in this case) room-sealing check.
 * <p>
 * The dome still has to fit the regulator's own capacity, by user design: a dome whose volume
 * (treated as a sphere, {@code 4/3 * pi * r^3}) exceeds {@code getMaximumSealedBlocks()} (the same
 * "Maximum sealed: N blocks" figure Northstar's own tooltip shows, driven by kinetic speed) counts as
 * too big, exactly like an oversized real sealed room would - sync is skipped and the regular "area too
 * big or unsealed" warning/particles are left alone to show through.
 */
public final class NorthstarSphereShieldsSupport {
    private NorthstarSphereShieldsSupport() {
    }

    /** The dome sealing this regulator, or null if unpowered, no dome contains it, or the dome is too big for its capacity. */
    public static GroundShieldTracker.Dome fieldDomeIfWithinCapacity(TemperatureRegulatorBlockEntity regulator) {
        Level level = regulator.getLevel();
        if (level == null) return null;

        boolean powered = Math.abs(regulator.getSpeed()) > 0 && !regulator.isOverStressed();
        if (!powered) return null;

        GroundShieldTracker.Dome dome = GroundShieldTracker.domeContaining(level.dimension(), Vec3.atCenterOf(regulator.getBlockPos()));
        if (dome == null) return null;

        double domeVolume = (4.0 / 3.0) * Math.PI * Math.pow(dome.radius(), 3);
        if (domeVolume > regulator.getMaximumSealedBlocks()) return null;

        return dome;
    }

    public static boolean isSealedByField(TemperatureRegulatorBlockEntity regulator) {
        return fieldDomeIfWithinCapacity(regulator) != null;
    }
}
