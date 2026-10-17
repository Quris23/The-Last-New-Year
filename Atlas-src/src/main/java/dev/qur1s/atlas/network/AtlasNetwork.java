package dev.qur1s.atlas.network;

import dev.qur1s.atlas.Atlases;
import dev.qur1s.atlas.client.AtlasClient;
import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Registers the payloads and streams an Atlas's tiles to a player a few per tick. */
@EventBusSubscriber(modid = "atlas", bus = EventBusSubscriber.Bus.MOD)
public final class AtlasNetwork {
    private static final int TILES_PER_TICK = 6;
    private static final Map<UUID, Deque<AtlasSheetPayload>> QUEUES = new HashMap<>();

    private AtlasNetwork() {
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RequestAtlasPayload.TYPE, RequestAtlasPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) handleRequest(player, payload);
                }));
        registrar.playToServer(RemoveMarkerPayload.TYPE, RemoveMarkerPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    AtlasData data = AtlasData.get(player.getServer());
                    if (data.removeMarker(payload.atlasId(), payload.dimension(), payload.markerId())) {
                        sendMarkers(player, payload.atlasId(), payload.dimension());
                    }
                }));
        registrar.playToServer(EditMarkerPayload.TYPE, EditMarkerPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    if (AtlasData.get(player.getServer()).updateMarker(payload.atlasId(), payload.dimension(), payload.marker())) {
                        sendMarkers(player, payload.atlasId(), payload.dimension());
                    }
                }));
        registrar.playToServer(AddMarkerPayload.TYPE, AddMarkerPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    dev.qur1s.atlas.data.AtlasMarker clean = payload.marker().sanitized();
                    if (AtlasData.get(player.getServer()).addMarker(payload.atlasId(), payload.dimension(), clean)) {
                        sendMarkers(player, payload.atlasId(), payload.dimension());
                    }
                }));
        registrar.playToServer(TeleportPayload.TYPE, TeleportPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        dev.qur1s.atlas.AtlasTeleport.teleport(player, payload.dimension(), payload.x(), payload.z());
                    }
                }));
        registrar.playToServer(RequestLivePayload.TYPE, RequestLivePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    PacketDistributor.sendToPlayer(player, new AtlasLivePayload(payload.atlasId(), payload.dimension(),
                            liveEntries(player, payload.dimension())));
                    java.util.List<dev.qur1s.atlas.data.AtlasPlayerEntry> online = new java.util.ArrayList<>();
                    for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                        if (other != player && !dev.qur1s.atlas.AtlasConfig.SHOW_OTHER_PLAYERS.get()) continue;
                        online.add(new dev.qur1s.atlas.data.AtlasPlayerEntry(other.getUUID(), other.getGameProfile().getName(),
                                other.level().dimension().location().toString(), other.getBlockX(), other.getBlockZ()));
                    }
                    PacketDistributor.sendToPlayer(player, new AtlasPlayersPayload(online));
                }));
        registrar.playToServer(RequestTracksPayload.TYPE, RequestTracksPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    if (!dev.qur1s.atlas.AtlasConfig.SHOW_TRACKS.get() || !net.neoforged.fml.ModList.get().isLoaded("create")) return;
                    int[] segments = dev.qur1s.atlas.AtlasTrains.collectTracks(AtlasData.get(player.getServer()),
                            payload.atlasId(), payload.dimension(), payload.all());
                    com.mojang.logging.LogUtils.getLogger().info("[atlas tracks] server: {} segments in {} (all={})",
                            segments.length / 4, payload.dimension(), payload.all());
                    PacketDistributor.sendToPlayer(player, new AtlasTracksPayload(payload.atlasId(), payload.dimension(), segments));
                }));
        registrar.playToClient(AtlasTracksPayload.TYPE, AtlasTracksPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onTracks(payload)));
        registrar.playToServer(LoadWholeMapPayload.TYPE, LoadWholeMapPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!player.getAbilities().instabuild || !Atlases.carries(player, payload.atlasId())) return;
                    net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(payload.dimension());
                    if (id == null) return;
                    var level = player.getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.DIMENSION, id));
                    if (level != null) dev.qur1s.atlas.AtlasScan.enqueueWhole(player, payload.atlasId(), level);
                }));
        registrar.playToServer(TrackMarkerPayload.TYPE, TrackMarkerPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (!Atlases.carries(player, payload.atlasId())) return;
                    net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(payload.dimension());
                    if (id == null) return;
                    dev.qur1s.atlas.AtlasCompass.magnetize(player, net.minecraft.core.GlobalPos.of(
                            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id),
                            new net.minecraft.core.BlockPos(payload.x(), payload.y(), payload.z())));
                }));
        registrar.playToServer(RequestOpenPayload.TYPE, RequestOpenPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    net.minecraft.world.item.ItemStack equipped = Atlases.equippedStack(player);
                    if (equipped.isEmpty()) return;
                    PacketDistributor.sendToPlayer(player, new OpenAtlasPayload(dev.qur1s.atlas.AtlasItem.ensureId(equipped)));
                }));
        registrar.playToClient(OpenAtlasPayload.TYPE, OpenAtlasPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.open(payload.atlasId())));
        registrar.playToClient(AtlasPlayersPayload.TYPE, AtlasPlayersPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onPlayers(payload)));
        registrar.playToClient(AtlasLivePayload.TYPE, AtlasLivePayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onLive(payload)));
        registrar.playToClient(AtlasDimensionsPayload.TYPE, AtlasDimensionsPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onDimensions(payload)));
        registrar.playToClient(AtlasMarkersPayload.TYPE, AtlasMarkersPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onMarkers(payload)));
        registrar.playToClient(AtlasSheetPayload.TYPE, AtlasSheetPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> AtlasClient.onSheet(payload)));
    }

    /** Marks the player's bed (respawn point) on every atlas as a house; it cannot be deleted. */
    public static final java.util.UUID HOME_ID = new java.util.UUID(0L, 0L);

    public static void sendMarkers(ServerPlayer player, java.util.UUID atlasId, String dimension) {
        java.util.List<dev.qur1s.atlas.data.AtlasMarker> list =
                new java.util.ArrayList<>(AtlasData.get(player.getServer()).markers(atlasId, dimension));
        var bed = player.getRespawnPosition();
        if (bed != null && player.getRespawnDimension().location().toString().equals(dimension)) {
            list.add(new dev.qur1s.atlas.data.AtlasMarker(HOME_ID, bed.getX(), bed.getY(), bed.getZ(), -1, "", "house", ""));
        }
        PacketDistributor.sendToPlayer(player, new AtlasMarkersPayload(atlasId, dimension, list));
    }

    /** The other players in the dimension, and the Rift Gates the player may see. */
    private static java.util.List<dev.qur1s.atlas.data.AtlasLiveEntry> liveEntries(ServerPlayer player, String dimension) {
        java.util.List<dev.qur1s.atlas.data.AtlasLiveEntry> list = new java.util.ArrayList<>();
        if (dev.qur1s.atlas.AtlasConfig.SHOW_OTHER_PLAYERS.get()) {
            for (ServerPlayer other : player.getServer().getPlayerList().getPlayers()) {
                if (other == player || !other.level().dimension().location().toString().equals(dimension)) continue;
                list.add(new dev.qur1s.atlas.data.AtlasLiveEntry(dev.qur1s.atlas.data.AtlasLiveEntry.PLAYER, other.getUUID(),
                        other.getBlockX(), other.getBlockY(), other.getBlockZ(), other.getGameProfile().getName()));
            }
        }
        if (dev.qur1s.atlas.AtlasConfig.SHOW_RIFT_GATES.get() && net.neoforged.fml.ModList.get().isLoaded("aerowarptics")) {
            dev.qur1s.atlas.AtlasGates.collect(player, dimension, list);
        }
        if (dev.qur1s.atlas.AtlasConfig.SHOW_TRAINS.get() && net.neoforged.fml.ModList.get().isLoaded("create")) {
            dev.qur1s.atlas.AtlasTrains.collectTrains(dimension, list);
            dev.qur1s.atlas.AtlasTrains.collectStations(dimension, list);
        }
        return list;
    }

    private static void handleRequest(ServerPlayer player, RequestAtlasPayload request) {
        // Only the owner of an Atlas may read its tiles.
        if (!Atlases.carries(player, request.atlasId())) return;
        AtlasData.get(player.getServer()).purgeIgnored(request.atlasId(), dev.qur1s.atlas.StructureMarkers::isIgnored);
        Deque<AtlasSheetPayload> queue = new ArrayDeque<>();
        // coarse sheets first, so the detailed ones are drawn over them as they arrive
        AtlasData.get(player.getServer()).sheets(request.atlasId(), request.dimension()).values().stream()
                .sorted(java.util.Comparator.comparingInt((dev.qur1s.atlas.data.AtlasSheet sheet) -> -sheet.scale))
                .forEach(sheet -> queue.add(new AtlasSheetPayload(request.atlasId(), request.dimension(),
                        sheet.scale, sheet.centreX, sheet.centreZ, sheet.colors.clone())));
        QUEUES.put(player.getUUID(), queue);
        sendMarkers(player, request.atlasId(), request.dimension());
        PacketDistributor.sendToPlayer(player, new AtlasDimensionsPayload(request.atlasId(),
                AtlasData.get(player.getServer()).dimensions(request.atlasId())));
    }

    @EventBusSubscriber(modid = "atlas")
    public static final class Tick {
        private Tick() {
        }

        @SubscribeEvent
        static void onServerTick(ServerTickEvent.Post event) {
            if (QUEUES.isEmpty()) return;
            var iterator = QUEUES.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player == null) {
                    iterator.remove();
                    continue;
                }
                for (int i = 0; i < TILES_PER_TICK; i++) {
                    AtlasSheetPayload next = entry.getValue().poll();
                    if (next == null) break;
                    PacketDistributor.sendToPlayer(player, next);
                }
                if (entry.getValue().isEmpty()) iterator.remove();
            }
        }
    }
}
