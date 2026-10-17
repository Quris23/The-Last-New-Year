package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: ask for the Create tracks: on the explored part of the map, or everywhere ({@code all}). */
public record RequestTracksPayload(UUID atlasId, String dimension, boolean all) implements CustomPacketPayload {
    public static final Type<RequestTracksPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "request_tracks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestTracksPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestTracksPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), RequestTracksPayload::dimension,
            ByteBufCodecs.BOOL, RequestTracksPayload::all,
            RequestTracksPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
