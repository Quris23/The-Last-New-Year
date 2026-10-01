package dev.qur1s.sphereshields.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the player toggled a shield generator's "allow mobs through" switch in its access GUI. */
public record SetAllowMobsPayload(BlockPos generatorPos, boolean allowMobs) implements CustomPacketPayload {
    public static final Type<SetAllowMobsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("sphereshields", "set_allow_mobs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetAllowMobsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetAllowMobsPayload::generatorPos,
                    ByteBufCodecs.BOOL,
                    SetAllowMobsPayload::allowMobs,
                    SetAllowMobsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
