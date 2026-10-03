package dev.qur1s.spellclasses.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * Settlement Roads locates villages synchronously on the server thread while a world loads. In a world that can
 * never contain the structure (structures switched off, or a superflat preset without villages) that search never
 * succeeds and just keeps loading chunks - the world "does not get created". This tells whether the configured
 * target can exist at all, so the search is only skipped when it is pointless.
 */
public final class SettlementRoadsGuard {
    private static final Logger LOG = LoggerFactory.getLogger("spellclasses/settlementroads");

    private SettlementRoadsGuard() {
    }

    public static boolean targetCanExist(ServerLevel level) {
        try {
            if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) {
                LOG.info("Structures are disabled in this world - skipping Settlement Roads' village search");
                return false;
            }
            String wanted = configuredTarget();
            if (wanted == null) {
                return true;
            }
            boolean tag = wanted.startsWith("#");
            ResourceLocation id = ResourceLocation.parse(tag ? wanted.substring(1) : wanted);
            TagKey<Structure> tagKey = TagKey.create(Registries.STRUCTURE, id);
            for (Holder<StructureSet> set : level.getChunkSource().getGeneratorState().possibleStructureSets()) {
                for (StructureSet.StructureSelectionEntry entry : set.value().structures()) {
                    Holder<Structure> structure = entry.structure();
                    if (tag ? structure.is(tagKey) : structure.is(id)) {
                        return true;
                    }
                }
            }
            LOG.info("This world's generator has no '{}' - skipping Settlement Roads' village search", wanted);
            return false;
        } catch (Throwable t) {
            LOG.warn("Could not check the world for structures, leaving Settlement Roads alone", t);
            return true;
        }
    }

    private static String configuredTarget() {
        try {
            Field f = Class.forName("net.countered.settlementroads.config.ModConfig").getField("structureToLocate");
            return (String) f.get(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
