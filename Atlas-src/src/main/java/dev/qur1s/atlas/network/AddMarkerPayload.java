package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasMarker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: put a new marker where the player right-clicked the map. */
public record AddMarkerPayload(UUID atlasId, String dimension, AtlasMarker marker) implements CustomPacketPayload {
    public static final Type<AddMarkerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "add_marker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddMarkerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AddMarkerPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), AddMarkerPayload::dimension,
            AtlasMarker.STREAM_CODEC, AddMarkerPayload::marker,
            AddMarkerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
