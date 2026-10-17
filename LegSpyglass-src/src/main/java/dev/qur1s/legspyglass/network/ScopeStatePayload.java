package dev.qur1s.legspyglass.network;

import dev.qur1s.legspyglass.LegSpyglass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C2S: the player started / stopped looking through the leg spyglass. */
public record ScopeStatePayload(boolean scoping) implements CustomPacketPayload {
    public static final Type<ScopeStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LegSpyglass.MODID, "scope_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScopeStatePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ScopeStatePayload::scoping, ScopeStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
