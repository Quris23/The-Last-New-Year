package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.item.ClassItems;
import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
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

            // Iron's Spellbooks only auto-registers SpellBookCurioRenderer for grimoires registered
            // through ITS OWN DeferredRegister (ClientSetup iterates ItemRegistry.getIronsItems()) -
            // our own custom grimoire, registered through SpellClasses' registry, is invisible to that
            // loop and never gets a curio renderer, so it silently draws nothing in the artifact slot.
            CuriosRendererRegistry.register(ClassItems.STORM_ATLAS.get(), SpellBookCurioRenderer::new);
        });
    }
}
