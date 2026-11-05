package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** S2C: what the client part of a cyberpsychosis attack does. {@code mask}: 1 = a random key, 2 = a mouse button or the arm hardware key, 4 = dizziness. */
public record SymptomPayload(int mask) implements CustomPacketPayload {
    public static final int KEYS = 1, MOUSE = 2, DIZZY = 4;

    public static final Type<SymptomPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "symptom"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SymptomPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SymptomPayload::mask, SymptomPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
