package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: the player opened an Atlas and wants its tiles for one dimension. */
public record RequestAtlasPayload(UUID atlasId, String dimension) implements CustomPacketPayload {
    public static final Type<RequestAtlasPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestAtlasPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestAtlasPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), RequestAtlasPayload::dimension,
            RequestAtlasPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
