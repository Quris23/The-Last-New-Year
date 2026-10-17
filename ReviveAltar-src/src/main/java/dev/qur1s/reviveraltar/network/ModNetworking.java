package dev.qur1s.reviveraltar.network;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    @SubscribeEvent
    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ReviveAltarMod.MODID);
        registrar.playToClient(GhostSyncPayload.TYPE, GhostSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        dev.qur1s.reviveraltar.client.ClientGhostState.set(payload.player(), payload.ghost())));
    }
}
