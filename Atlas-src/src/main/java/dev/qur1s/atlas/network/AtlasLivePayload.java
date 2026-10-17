package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasLiveEntry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

/** S2C: the other players and Rift Gates in one dimension. */
public record AtlasLivePayload(UUID atlasId, String dimension, List<AtlasLiveEntry> entries) implements CustomPacketPayload {
    public static final Type<AtlasLivePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "live"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasLivePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AtlasLivePayload::atlasId,
            ByteBufCodecs.stringUtf8(128), AtlasLivePayload::dimension,
            AtlasLiveEntry.STREAM_CODEC.apply(ByteBufCodecs.list(512)), AtlasLivePayload::entries,
            AtlasLivePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
