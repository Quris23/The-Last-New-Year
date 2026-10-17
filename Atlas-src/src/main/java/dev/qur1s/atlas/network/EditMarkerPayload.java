package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasMarker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: save an edited marker (same id as the one it replaces). */
public record EditMarkerPayload(UUID atlasId, String dimension, AtlasMarker marker) implements CustomPacketPayload {
    public static final Type<EditMarkerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "edit_marker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EditMarkerPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, EditMarkerPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), EditMarkerPayload::dimension,
            AtlasMarker.STREAM_CODEC, EditMarkerPayload::marker,
            EditMarkerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
