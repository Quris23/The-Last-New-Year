package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.item.ClassItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BeltRenderSetup {
    private BeltRenderSetup() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosRendererRegistry.register(ClassItems.BRASS_BELT.get(), BeltRenderers.Brass::new);
            CuriosRendererRegistry.register(ClassItems.BLACK_STEEL_BELT.get(), BeltRenderers.BlackSteel::new);
        });
    }
}
