package dev.qur1s.spellclasses.mixin;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FTB Quests registers its S2C custom payloads (UpdateTaskProgressMessage, ObjectCompletedMessage,
 * ...) directly against vanilla's own {@code CustomPacketPayload} system (via
 * {@code NetworkManager.registerReceiver}, which on NeoForge goes straight to the native payload
 * registrar), but then broadcasts them via {@code NetworkManager.sendToPlayers(players, payload)} -
 * Architectury's OLD "simple networking" aggregator path, which unconditionally re-wraps the
 * payload into its own {@code NetworkAggregator$BufCustomPacketPayload} before handing it to
 * vanilla's packet system. Vanilla's dispatch codec for the payload's own type id was registered
 * for the RAW payload class (from the first registration call), not the wrapper - so encoding the
 * wrapped packet throws a ClassCastException and disconnects the player, and since this happens
 * while broadcasting a quest-progress update, the progress change is lost entirely (crashes before
 * the team data gets persisted). This reproduces on every architectury/ftbquests version combo
 * tried, on both dedicated and singleplayer-integrated servers - it's a genuine bug in how FTB
 * Quests calls Architectury's API, not a version mismatch.
 * <p>
 * Fix: for FTB Quests' own payloads specifically, skip Architectury's aggregator entirely and send
 * a plain vanilla {@link ClientboundCustomPayloadPacket} directly - this is exactly what
 * {@code registerReceiver} already set vanilla up to decode correctly on the client.
 */
@Mixin(value = dev.architectury.networking.NetworkManager.class, remap = false)
public class ArchitecturyFtbQuestsSendFixMixin {
    @Inject(method = "sendToPlayers(Ljava/lang/Iterable;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void spellclasses$fixFtbQuestsBroadcast(Iterable<ServerPlayer> players, CustomPacketPayload payload, CallbackInfo ci) {
        if (!payload.getClass().getName().startsWith("dev.ftb.mods.ftbquests.")) return;

        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
        for (ServerPlayer player : players) {
            player.connection.send(packet);
        }
        ci.cancel();
    }
}
