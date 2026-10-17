package dev.qur1s.spellclasses.compat;

import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.dragon_breath.DragonBreathProjectile;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.registry.CDPFanProcessingTypes;

/** Only touched when Create: Dragons Plus is installed (it owns the Ending and Freezing fan-processing types). */
final class DragonBreathEndingHooks {
    private DragonBreathEndingHooks() {
    }

    static void process(Level level, DragonBreathProjectile cone) {
        BreathProcessing.process(level, cone, CDPFanProcessingTypes.ENDING.get(), false);
    }

    /** Cone of Cold works like a fan blowing through frost: Create: Dragons Plus "Bulk Freezing". */
    static void processFrost(Level level, AbstractConeProjectile cone) {
        BreathProcessing.process(level, cone, CDPFanProcessingTypes.FREEZING.get(), false);
    }
}
