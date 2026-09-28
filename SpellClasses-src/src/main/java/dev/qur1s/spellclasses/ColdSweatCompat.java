package dev.qur1s.spellclasses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.util.Optional;

/**
 * Gateway to the Cold Sweat integration. Never references Cold Sweat's own classes directly —
 * those live in {@link ColdSweatHooks}, which is only ever touched once {@link #isLoaded()} is
 * true, so this mod keeps working fine on a pack that doesn't have Cold Sweat installed.
 */
public final class ColdSweatCompat {
    private ColdSweatCompat() {
    }

    private static final boolean LOADED = ModList.get().isLoaded("cold_sweat");

    public static boolean isLoaded() {
        return LOADED;
    }

    /** Re-applies the class's cold/heat immunity (Ice/Infernal), clearing it if {@code school} is empty or unrelated. */
    public static void syncResistance(ServerPlayer player, Optional<ResourceLocation> school) {
        if (!LOADED) return;
        ColdSweatHooks.syncResistance(player, school);
    }

    /** Whether {@code school}'s matching environment currently holds for {@code player}. */
    public static boolean matchesEnvironment(ServerPlayer player, ResourceLocation school) {
        if (!LOADED) return false;
        return ColdSweatHooks.matchesEnvironment(player, school);
    }

    /** Adds/removes {@code school}'s +10% spell power modifier to match {@code active}. */
    public static void syncPowerBuff(ServerPlayer player, ResourceLocation school, boolean active) {
        if (!LOADED) return;
        ColdSweatHooks.syncPowerBuff(player, school, active);
    }
}
