package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasSheet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** S2C: one map sheet (128x128 map colour ids, with its scale and centre) of an Atlas. */
public record AtlasSheetPayload(UUID atlasId, String dimension, int scale, int centreX, int centreZ, byte[] colors)
        implements CustomPacketPayload {
    public static final Type<AtlasSheetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "sheet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasSheetPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AtlasSheetPayload::atlasId,
            ByteBufCodecs.stringUtf8(128), AtlasSheetPayload::dimension,
            ByteBufCodecs.VAR_INT, AtlasSheetPayload::scale,
            ByteBufCodecs.INT, AtlasSheetPayload::centreX,
            ByteBufCodecs.INT, AtlasSheetPayload::centreZ,
            ByteBufCodecs.byteArray(AtlasSheet.AREA), AtlasSheetPayload::colors,
            AtlasSheetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
