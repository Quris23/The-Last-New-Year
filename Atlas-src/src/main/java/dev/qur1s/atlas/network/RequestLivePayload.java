package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: while the Atlas is open, ask once a second where the other players and the gates are. */
public record RequestLivePayload(UUID atlasId, String dimension) implements CustomPacketPayload {
    public static final Type<RequestLivePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "request_live"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestLivePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestLivePayload::atlasId,
            ByteBufCodecs.stringUtf8(128), RequestLivePayload::dimension,
            RequestLivePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
