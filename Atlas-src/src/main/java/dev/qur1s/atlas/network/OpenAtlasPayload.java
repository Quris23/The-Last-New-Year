package dev.qur1s.atlas.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** S2C: open the Atlas screen for the Atlas with this id (used for an Atlas standing on a lectern). */
public record OpenAtlasPayload(UUID atlasId) implements CustomPacketPayload {
    public static final Type<OpenAtlasPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAtlasPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, OpenAtlasPayload::atlasId,
            OpenAtlasPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
