package dev.qur1s.spellclasses.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the player clicked a school button in the class book's screen. */
public record ChooseClassPayload(String schoolId) implements CustomPacketPayload {
    public static final Type<ChooseClassPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("spellclasses", "choose_class"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChooseClassPayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of((buf, s) -> buf.writeUtf(s), buf -> buf.readUtf(64)),
                    ChooseClassPayload::schoolId,
                    ChooseClassPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
