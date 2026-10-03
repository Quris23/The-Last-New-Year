package dev.qur1s.spellclasses.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import uk.co.iceconchy.aerowarptics.registry.AWBlockEntities;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RiftGatePortalSetup {
    private RiftGatePortalSetup() {
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (net.neoforged.fml.ModList.get().isLoaded("aerowarptics")) {
            event.registerBlockEntityRenderer(AWBlockEntities.RIFT_GATE.get(), context -> new RiftGatePortalRenderer());
        }
    }
}
