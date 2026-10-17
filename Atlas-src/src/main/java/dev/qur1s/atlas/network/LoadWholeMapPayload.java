package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S (creative only): put everything of this dimension that exists in the world on the Atlas. */
public record LoadWholeMapPayload(UUID atlasId, String dimension) implements CustomPacketPayload {
    public static final Type<LoadWholeMapPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "load_whole_map"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LoadWholeMapPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, LoadWholeMapPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), LoadWholeMapPayload::dimension,
            LoadWholeMapPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
