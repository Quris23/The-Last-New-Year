package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: delete one flag from an Atlas. */
public record RemoveMarkerPayload(UUID atlasId, String dimension, UUID markerId) implements CustomPacketPayload {
    public static final Type<RemoveMarkerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "remove_marker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveMarkerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RemoveMarkerPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), RemoveMarkerPayload::dimension,
            UUIDUtil.STREAM_CODEC, RemoveMarkerPayload::markerId,
            RemoveMarkerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
