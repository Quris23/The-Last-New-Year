package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Turns the area a map has uncovered into the Atlas's own terrain: every chunk the map has explored is coloured from
 * the world itself, a few chunks per tick so the server does not stall. Chunks that are not loaded are read from disk.
 */
@EventBusSubscriber(modid = "atlas")
public final class AtlasScan {
    private static final int MAX_CHUNKS_PER_JOB = 20000;
    private static final long POUR_BUDGET_NANOS = 15_000_000L;
    private static final long WHOLE_BUDGET_NANOS = 35_000_000L;

    private record Job(UUID player, UUID atlasId, ResourceKey<Level> dimension, Deque<Long> chunks, int total, boolean colour, boolean whole) {
    }

    private static final Deque<Job> JOBS = new ArrayDeque<>();

    private AtlasScan() {
    }

    /** Queues every chunk that has an explored pixel on this map. */
    public static void enqueue(ServerPlayer player, UUID atlasId, MapItemSavedData map) {
        ServerLevel level = player.getServer().getLevel(map.dimension);
        if (level == null) return;
        boolean colour = false;     // the map itself is what the Atlas shows; the chunks are only searched for structures
        int size = 1 << map.scale;
        Set<Long> chunks = new LinkedHashSet<>();
        for (int py = 0; py < 128 && chunks.size() < MAX_CHUNKS_PER_JOB; py++) {
            for (int px = 0; px < 128; px++) {
                if (((map.colors[px + py * 128] & 0xFF) >> 2) == 0) continue;
                int blockX = map.centerX + (px - 64) * size;
                int blockZ = map.centerZ + (py - 64) * size;
                for (int cz = Math.floorDiv(blockZ, 16); cz <= Math.floorDiv(blockZ + size - 1, 16); cz++) {
                    for (int cx = Math.floorDiv(blockX, 16); cx <= Math.floorDiv(blockX + size - 1, 16); cx++) {
                        chunks.add(ChunkPos.asLong(cx, cz));
                    }
                }
            }
        }
        if (!chunks.isEmpty()) {
            JOBS.add(new Job(player.getUUID(), atlasId, map.dimension, new ArrayDeque<>(chunks), chunks.size(), colour, false));
        }
    }

    /** Whether the chunk is saved, and finished (loading a half-made chunk would make the game generate the rest). */
    private static boolean existsOnDisk(ServerLevel level, int cx, int cz) {
        try {
            var tag = level.getChunkSource().chunkMap.read(new ChunkPos(cx, cz)).join();
            return tag.isPresent() && "minecraft:full".equals(tag.get().getString("Status"));
        } catch (Throwable t) {
            return false;
        }
    }

    /** Queues every finished chunk of the dimension that is saved in the world (creative: "load the whole map"). */
    public static void enqueueWhole(ServerPlayer player, UUID atlasId, ServerLevel level) {
        java.nio.file.Path folder = net.minecraft.world.level.dimension.DimensionType
                .getStorageFolder(level.dimension(), player.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT))
                .resolve("region");
        Deque<Long> chunks = new ArrayDeque<>();
        try (var files = java.nio.file.Files.list(folder)) {
            for (java.nio.file.Path file : (Iterable<java.nio.file.Path>) files::iterator) {
                String[] parts = file.getFileName().toString().split("\\.");
                if (parts.length != 4 || !parts[0].equals("r") || !parts[3].equals("mca")) continue;
                int rx, rz;
                try {
                    rx = Integer.parseInt(parts[1]);
                    rz = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    continue;
                }
                // the first 4 KB of a region file say which of its 1024 chunks exist
                byte[] header = new byte[4096];
                try (var in = java.nio.file.Files.newInputStream(file)) {
                    if (in.readNBytes(header, 0, 4096) < 4096) continue;
                }
                for (int i = 0; i < 1024; i++) {
                    if ((header[i * 4] | header[i * 4 + 1] | header[i * 4 + 2]) != 0) {
                        chunks.add(ChunkPos.asLong(rx * 32 + (i & 31), rz * 32 + (i >> 5)));
                    }
                }
            }
        } catch (java.io.IOException e) {
            player.displayClientMessage(Component.translatable("message.atlas.scan_failed"), true);
            return;
        }
        if (chunks.isEmpty()) return;
        player.displayClientMessage(Component.translatable("message.atlas.scan_started", chunks.size()), true);
        JOBS.add(new Job(player.getUUID(), atlasId, level.dimension(), chunks, chunks.size(), true, true));
    }

    /**
     * Every structure that starts in this chunk and stands above ground gets a marker (its own icon, or the dungeon
     * for unknown ones). Underground ones stay off the Atlas until a player comes to them (see StructureDiscovery).
     */
    private static void markStructures(ServerLevel level, AtlasData data, UUID atlasId, String dimensionId, LevelChunk chunk) {
        var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        chunk.getAllStarts().forEach((structure, start) -> {
            if (start == null || !start.isValid()) return;
            var key = registry.getKey(structure);
            if (key == null) return;
            String id = key.toString();
            if (id.equals("minecraft:buried_treasure")) return;   // only a treasure map may reveal where the loot lies
            UUID markerId = StructureMarkers.scanId(dimensionId, id, chunk.getPos());
            if (StructureMarkers.isIgnored(id) || StructureMarkers.isUnderground(level, chunk, start)) {
                data.removeMarker(atlasId, dimensionId, markerId);     // an earlier scan may have put it there
                return;
            }
            StructureMarkers.add(data, atlasId, dimensionId, id, markerId, start);
        });
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        Job job = JOBS.peek();
        if (job == null) return;
        ServerLevel level = event.getServer().getLevel(job.dimension());
        if (level == null) {
            JOBS.poll();
            return;
        }
        AtlasData data = AtlasData.get(event.getServer());
        String dimensionId = job.dimension().location().toString();
        // work for a fixed slice of each tick rather than a fixed number of chunks: loaded chunks cost little, and the
        // creative "whole map" job may use more of the tick than a pour does
        long deadline = System.nanoTime() + (job.whole() ? WHOLE_BUDGET_NANOS : POUR_BUDGET_NANOS);
        while (!job.chunks().isEmpty() && System.nanoTime() < deadline) {
            long packed = job.chunks().peek();
            int cx = ChunkPos.getX(packed), cz = ChunkPos.getZ(packed);
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
            if (chunk == null) {
                // only chunks that were really explored (they are saved on disk); never generate new ones
                if (existsOnDisk(level, cx, cz)) {
                    chunk = (LevelChunk) level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, true);
                }
            }
            job.chunks().poll();
            if (chunk != null) {
                if (job.colour()) data.putChunk(job.atlasId(), dimensionId, cx, cz, AtlasTerrain.sample(level, chunk));
                markStructures(level, data, job.atlasId(), dimensionId, chunk);
            }
        }
        if (job.whole() && !job.chunks().isEmpty() && event.getServer().getTickCount() % 40 == 0) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(job.player());
            if (player != null) {
                int done = job.total() - job.chunks().size();
                player.displayClientMessage(Component.translatable("message.atlas.scan_progress", done * 100 / job.total(), done, job.total()), true);
            }
        }
        if (job.chunks().isEmpty()) {
            JOBS.poll();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(job.player());
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.atlas.scan_done", job.total()), true);
            }
        }
    }
}
