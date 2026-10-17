package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/** S2C: the dimensions an Atlas has anything in, for the dimension list. */
public record AtlasDimensionsPayload(UUID atlasId, List<String> dimensions) implements CustomPacketPayload {
    public static final Type<AtlasDimensionsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "dimensions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasDimensionsPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AtlasDimensionsPayload::atlasId,
            ByteBufCodecs.stringUtf8(128).apply(ByteBufCodecs.list(64)), AtlasDimensionsPayload::dimensions,
            AtlasDimensionsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
