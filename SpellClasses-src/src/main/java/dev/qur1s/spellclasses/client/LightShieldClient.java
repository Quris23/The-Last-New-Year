package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LightShieldClient {
    private LightShieldClient() {
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LIGHT_SHIELD.get(), LightShieldRenderer::new);
    }
}
