package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the player holds the use button with the Tide Piercer in the main hand (sent every other tick while it is held). */
public record TidePiercerPayload() implements CustomPacketPayload {
    public static final Type<TidePiercerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "tide_piercer_hold"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TidePiercerPayload> STREAM_CODEC = StreamCodec.unit(new TidePiercerPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
