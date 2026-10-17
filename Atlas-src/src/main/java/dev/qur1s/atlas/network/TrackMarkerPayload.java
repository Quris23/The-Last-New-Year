package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: set the compass in hand to point at a spot (a marker). */
public record TrackMarkerPayload(UUID atlasId, String dimension, int x, int y, int z) implements CustomPacketPayload {
    public static final Type<TrackMarkerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "trackMarkerPayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrackMarkerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TrackMarkerPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), TrackMarkerPayload::dimension,
            ByteBufCodecs.VAR_INT, TrackMarkerPayload::x,
            ByteBufCodecs.VAR_INT, TrackMarkerPayload::y,
            ByteBufCodecs.VAR_INT, TrackMarkerPayload::z,
            TrackMarkerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
