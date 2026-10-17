package dev.qur1s.legspyglass.network;

import dev.qur1s.legspyglass.LegSpyglass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** S2C: tells nearby clients (and the player) that an entity is, or no longer is, looking through the spyglass. */
public record ScopeBroadcastPayload(int entityId, boolean scoping) implements CustomPacketPayload {
    public static final Type<ScopeBroadcastPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LegSpyglass.MODID, "scope_broadcast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScopeBroadcastPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, ScopeBroadcastPayload::entityId,
                    ByteBufCodecs.BOOL, ScopeBroadcastPayload::scoping, ScopeBroadcastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
