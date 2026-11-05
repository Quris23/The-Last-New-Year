package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the player picked a switch of the Face group in the implant ability wheel: 0 = Night Vision Goggles, 1 = Holographic Apple. */
public record NightVisionPayload(int which) implements CustomPacketPayload {
    public static final Type<NightVisionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "night_vision"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NightVisionPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, NightVisionPayload::which, NightVisionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
