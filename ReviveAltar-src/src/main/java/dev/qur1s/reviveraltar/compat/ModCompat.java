package dev.qur1s.reviveraltar.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/**
 * Soft integration with other installed mods, resolved by registry lookup so this mod
 * compiles and runs fine even when those mods are absent (the debuff simply becomes
 * unavailable in that case).
 */
public final class ModCompat {
    private ModCompat() {}

    private static final ResourceLocation CURSED_MARK_ID =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "cursed_mark");

    /** The "Born in Chaos" Cursed Mark debuff applied after a ritual revival. */
    public static Holder<MobEffect> cursedMark() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(CURSED_MARK_ID).orElse(null);
    }
}
