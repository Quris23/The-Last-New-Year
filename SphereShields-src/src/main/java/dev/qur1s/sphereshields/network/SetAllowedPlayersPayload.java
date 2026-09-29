package dev.qur1s.sphereshields.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** C2S: the player edited a shield generator's access whitelist in its GUI. */
public record SetAllowedPlayersPayload(BlockPos generatorPos, List<UUID> allowed) implements CustomPacketPayload {
    public static final Type<SetAllowedPlayersPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("sphereshields", "set_allowed_players"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetAllowedPlayersPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SetAllowedPlayersPayload::generatorPos,
                    ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC),
                    SetAllowedPlayersPayload::allowed,
                    SetAllowedPlayersPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
