package dev.qur1s.legspyglass.client;

import dev.qur1s.legspyglass.LegSpyglass;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = LegSpyglass.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> CuriosRendererRegistry.register(Items.SPYGLASS, SpyglassCurioRenderer::new));
    }

    /** The in-hand spyglass layer goes on both player models (wide and slim arms). */
    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : new PlayerSkin.Model[]{PlayerSkin.Model.WIDE, PlayerSkin.Model.SLIM}) {
            var renderer = event.getSkin(model);
            if (renderer instanceof LivingEntityRenderer<?, ?> living) {
                @SuppressWarnings("unchecked")
                var player = (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) living;
                player.addLayer(new SpyglassHandLayer(player));
            }
        }
    }
}
