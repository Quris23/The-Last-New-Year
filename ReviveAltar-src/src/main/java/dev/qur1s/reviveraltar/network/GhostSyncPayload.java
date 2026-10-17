package dev.qur1s.reviveraltar.network;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Tells a client whether a given player (by UUID) is currently a ghost. */
public record GhostSyncPayload(UUID player, boolean ghost) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GhostSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReviveAltarMod.MODID, "ghost_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, GhostSyncPayload::player,
            ByteBufCodecs.BOOL, GhostSyncPayload::ghost,
            GhostSyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<GhostSyncPayload> type() {
        return TYPE;
    }
}
