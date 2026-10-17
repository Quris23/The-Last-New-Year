package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: open the equipped Atlas (the server gives an unused one its id first). */
public record RequestOpenPayload() implements CustomPacketPayload {
    public static final Type<RequestOpenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "requestOpenPayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenPayload> STREAM_CODEC = StreamCodec.unit(new RequestOpenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
