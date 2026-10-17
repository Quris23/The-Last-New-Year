package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasMarker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/** S2C: the complete list of an Atlas's flags in one dimension. */
public record AtlasMarkersPayload(UUID atlasId, String dimension, List<AtlasMarker> markers)
        implements CustomPacketPayload {
    public static final Type<AtlasMarkersPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "markers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasMarkersPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AtlasMarkersPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), AtlasMarkersPayload::dimension,
            AtlasMarker.STREAM_CODEC.apply(ByteBufCodecs.list(AtlasData.MAX_MARKERS + 8)), AtlasMarkersPayload::markers,
            AtlasMarkersPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
