package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * A structure that was kept off the Atlas because it is underground appears on it when the player (with the Atlas in
 * the Atlas slot) comes inside it or close to it.
 */
@EventBusSubscriber(modid = "atlas")
public final class StructureDiscovery {
    private static final int CHECK_EVERY_TICKS = 40;
    private static final int CHUNK_RADIUS = 4;
    private static final int REACH = 16;

    private StructureDiscovery() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || (player.tickCount + player.getId()) % CHECK_EVERY_TICKS != 0) return;
        var atlas = Atlases.equippedStack(player);
        if (atlas.isEmpty()) return;
        UUID atlasId = AtlasItem.ensureId(atlas);
        ServerLevel level = player.serverLevel();
        AtlasData data = AtlasData.get(player.getServer());
        String dimension = level.dimension().location().toString();
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ChunkPos here = player.chunkPosition();
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(here.x + dx, here.z + dz);
                if (chunk == null) continue;
                chunk.getAllStarts().forEach((structure, start) -> {
                    if (start == null || !start.isValid()) return;
                    if (!start.getBoundingBox().inflatedBy(REACH).isInside(player.blockPosition())) return;
                    var key = registry.getKey(structure);
                    if (key == null || key.toString().equals("minecraft:buried_treasure") || StructureMarkers.isIgnored(key.toString())) return;
                    String id = key.toString();
                    StructureMarkers.add(data, atlasId, dimension, id, StructureMarkers.foundId(dimension, id, chunk.getPos()), start);
                });
            }
        }
    }
}
