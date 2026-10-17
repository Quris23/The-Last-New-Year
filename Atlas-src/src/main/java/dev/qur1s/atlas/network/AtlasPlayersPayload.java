package dev.qur1s.atlas.network;

import dev.qur1s.atlas.data.AtlasPlayerEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** S2C: everyone who is online and where they are, for the player search. */
public record AtlasPlayersPayload(List<AtlasPlayerEntry> players) implements CustomPacketPayload {
    public static final Type<AtlasPlayersPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atlas", "players"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasPlayersPayload> STREAM_CODEC = StreamCodec.composite(
            AtlasPlayerEntry.STREAM_CODEC.apply(ByteBufCodecs.list(256)), AtlasPlayersPayload::players,
            AtlasPlayersPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
