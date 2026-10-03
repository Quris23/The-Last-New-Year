package dev.qur1s.spellclasses.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Set only while a Rift Beacon summons a ship from ANOTHER dimension: the dimension the beacon's user stands in.
 * The drive picks it up in startWarp and keeps it as the destination dimension of that flight.
 */
public final class CrossFix {
    public static final ThreadLocal<ResourceKey<Level>> DIMENSION = new ThreadLocal<>();

    private CrossFix() {
    }
}
