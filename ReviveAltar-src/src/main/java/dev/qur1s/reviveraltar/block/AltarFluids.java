package dev.qur1s.reviveraltar.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Soft lookup of the Mana/Blood fluids, by registry id — no hard compile dependency on either mod.
 * <p>
 * Mana comes from Create Wizardry's own fluid. Blood does too as an item ({@code
 * create_wizardry:blood_bucket}), but that bucket is actually a plain {@code BucketItem} wrapping
 * Iron's Spellbooks' {@code irons_spellbooks:blood} fluid, not Create Wizardry's own blood fluid —
 * so that's the id we have to filter the tank against.
 */
final class AltarFluids {
    private AltarFluids() {
    }

    private static final ResourceLocation MANA_ID = ResourceLocation.fromNamespaceAndPath("create_wizardry", "mana");
    private static final ResourceLocation BLOOD_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "blood");

    @Nullable
    static Fluid mana() {
        return BuiltInRegistries.FLUID.getOptional(MANA_ID).orElse(null);
    }

    @Nullable
    static Fluid blood() {
        return BuiltInRegistries.FLUID.getOptional(BLOOD_ID).orElse(null);
    }
}
