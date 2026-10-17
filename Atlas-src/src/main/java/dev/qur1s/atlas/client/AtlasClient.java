package dev.qur1s.atlas.client;

import dev.qur1s.atlas.network.AtlasDimensionsPayload;
import dev.qur1s.atlas.network.AtlasLivePayload;
import dev.qur1s.atlas.network.AtlasTracksPayload;
import dev.qur1s.atlas.network.AtlasMarkersPayload;
import dev.qur1s.atlas.network.AtlasSheetPayload;
import net.minecraft.client.Minecraft;

import java.util.UUID;

/** Client-only entry points, kept out of the common classes so a server never loads client code. */
public final class AtlasClient {
    private AtlasClient() {
    }

    public static void open(UUID atlasId) {
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance
                .forUI(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F));
        Minecraft.getInstance().setScreen(new AtlasScreen(atlasId));
    }

    public static void onDimensions(AtlasDimensionsPayload payload) {
        ClientAtlasCache.setDimensions(payload.atlasId(), payload.dimensions());
    }

    public static void onTracks(AtlasTracksPayload payload) {
        com.mojang.logging.LogUtils.getLogger().info("[atlas tracks] client: got {} segments for {}", payload.segments().length / 4, payload.dimension());
        ClientAtlasCache.setTracks(payload.atlasId(), payload.dimension(), payload.segments());
    }

    public static void onPlayers(dev.qur1s.atlas.network.AtlasPlayersPayload payload) {
        ClientAtlasCache.setPlayers(payload.players());
    }

    public static void onLive(AtlasLivePayload payload) {
        ClientAtlasCache.setLive(payload.atlasId(), payload.dimension(), payload.entries());
    }

    public static void onMarkers(AtlasMarkersPayload payload) {
        ClientAtlasCache.setMarkers(payload.atlasId(), payload.dimension(), payload.markers());
    }

    public static void onSheet(AtlasSheetPayload payload) {
        ClientAtlasCache.put(payload.atlasId(), payload.dimension(), payload.scale(), payload.centreX(), payload.centreZ(), payload.colors());
    }
}
