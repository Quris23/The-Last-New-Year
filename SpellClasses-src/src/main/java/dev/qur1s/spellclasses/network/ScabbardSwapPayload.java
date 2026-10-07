package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the scabbard key was pressed - swap the sheathed item with the main hand. */
public record ScabbardSwapPayload() implements CustomPacketPayload {
    public static final Type<ScabbardSwapPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "scabbard_swap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScabbardSwapPayload> STREAM_CODEC =
            StreamCodec.unit(new ScabbardSwapPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
