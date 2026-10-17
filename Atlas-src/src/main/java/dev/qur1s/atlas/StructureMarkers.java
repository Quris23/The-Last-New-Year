package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Shared bits of putting a structure on an Atlas. */
public final class StructureMarkers {
    /** A structure whose top is this far below the ground is underground. */
    private static final int UNDERGROUND_DEPTH = 8;

    private StructureMarkers() {
    }

    /** Whether the structure is on the config's list of ones to leave off the Atlas (small ruins and the like). */
    public static boolean isIgnored(String structureId) {
        for (String entry : AtlasConfig.IGNORED_STRUCTURES.get()) {
            if (entry.endsWith("*") ? structureId.startsWith(entry.substring(0, entry.length() - 1)) : structureId.equals(entry)) return true;
        }
        return false;
    }

    /** The id of a structure's marker made by the terrain scan. */
    public static UUID scanId(String dimension, String structure, ChunkPos chunk) {
        return UUID.nameUUIDFromBytes((dimension + "|" + structure + "|" + chunk.x + "," + chunk.z).getBytes(StandardCharsets.UTF_8));
    }

    /** The id of a structure's marker made because a player came to it (kept apart, so a re-scan does not remove it). */
    public static UUID foundId(String dimension, String structure, ChunkPos chunk) {
        return UUID.nameUUIDFromBytes((dimension + "|" + structure + "|" + chunk.x + "," + chunk.z + "|found").getBytes(StandardCharsets.UTF_8));
    }

    /** Whether the structure lies under the ground (strongholds, mineshafts, ancient cities, trial chambers...). */
    public static boolean isUnderground(ServerLevel level, LevelChunk chunk, StructureStart start) {
        if (level.dimensionType().hasCeiling()) return false;     // the Nether has no "above ground"
        var box = start.getBoundingBox();
        BlockPos centre = box.getCenter();
        int lx = Math.max(0, Math.min(15, centre.getX() - chunk.getPos().getMinBlockX()));
        int lz = Math.max(0, Math.min(15, centre.getZ() - chunk.getPos().getMinBlockZ()));
        int ground = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, lx, lz);
        return box.maxY() < ground - UNDERGROUND_DEPTH;
    }

    public static boolean add(AtlasData data, UUID atlasId, String dimension, String structure, UUID markerId, StructureStart start) {
        BlockPos centre = start.getBoundingBox().getCenter();
        return data.addAutoMarker(atlasId, dimension, new AtlasMarker(markerId, centre.getX(), centre.getY(), centre.getZ(),
                AtlasMarker.COLOR_STRUCTURE, structure, StructureIcons.iconFor(structure), ""), 48);
    }
}
