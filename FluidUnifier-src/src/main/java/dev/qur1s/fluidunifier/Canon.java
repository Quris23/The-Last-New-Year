package dev.qur1s.fluidunifier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Maps every "alias" fluid (and its flowing variant) to its canonical fluid. */
public final class Canon {
    private Canon() {}

    private static volatile Map<Fluid, Fluid> map = Map.of();

    public static Fluid of(Fluid fluid) {
        Map<Fluid, Fluid> m = map;
        if (m.isEmpty() || fluid == null) {
            return fluid;
        }
        Fluid canonical = m.get(fluid);
        return canonical != null ? canonical : fluid;
    }

    /** Rebuilds the table from "alias=canonical" entries. Call once registries are populated. */
    public static void rebuild(List<? extends String> entries) {
        Map<Fluid, Fluid> built = new IdentityHashMap<>();
        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) {
                FluidUnifier.LOGGER.warn("Ignoring malformed alias entry '{}', expected alias=canonical", entry);
                continue;
            }
            ResourceLocation aliasId = ResourceLocation.tryParse(parts[0].trim());
            ResourceLocation canonId = ResourceLocation.tryParse(parts[1].trim());
            if (aliasId == null || canonId == null) {
                FluidUnifier.LOGGER.warn("Ignoring alias entry with invalid id: '{}'", entry);
                continue;
            }
            Fluid alias = BuiltInRegistries.FLUID.getOptional(aliasId).orElse(null);
            Fluid canon = BuiltInRegistries.FLUID.getOptional(canonId).orElse(null);
            if (alias == null) {
                continue; // that addon isn't installed - nothing to unify
            }
            if (canon == null || alias == canon) {
                FluidUnifier.LOGGER.warn("Canonical fluid '{}' for alias '{}' is missing", canonId, aliasId);
                continue;
            }
            built.put(alias, canon);
            if (alias instanceof FlowingFluid a && canon instanceof FlowingFluid c) {
                built.put(a.getSource(), c.getSource());
                built.put(a.getFlowing(), c.getFlowing());
            }
            FluidUnifier.LOGGER.info("Unifying fluid {} -> {}", aliasId, canonId);
        }
        map = built;
    }
}
