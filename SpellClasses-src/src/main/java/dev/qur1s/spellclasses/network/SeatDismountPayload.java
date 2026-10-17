package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the client has been holding sneak on a seat - get the player off it (works around sneak not reaching the server on Sable ships). */
public record SeatDismountPayload() implements CustomPacketPayload {
    public static final Type<SeatDismountPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "seat_dismount"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SeatDismountPayload> STREAM_CODEC =
            StreamCodec.unit(new SeatDismountPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
