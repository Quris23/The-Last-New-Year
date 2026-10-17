package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** C2S: a moderator asks to be taken to a spot of the map. */
public record TeleportPayload(UUID atlasId, String dimension, int x, int z) implements CustomPacketPayload {
    public static final Type<TeleportPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "teleport"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TeleportPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), TeleportPayload::dimension,
            ByteBufCodecs.VAR_INT, TeleportPayload::x,
            ByteBufCodecs.VAR_INT, TeleportPayload::z,
            TeleportPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
