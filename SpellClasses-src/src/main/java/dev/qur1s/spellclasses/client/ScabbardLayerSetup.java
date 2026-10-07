package dev.qur1s.spellclasses.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Adds the scabbard back layer to both player models (wide and slim arms). */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ScabbardLayerSetup {
    private ScabbardLayerSetup() {
    }

    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : new PlayerSkin.Model[]{PlayerSkin.Model.WIDE, PlayerSkin.Model.SLIM}) {
            var renderer = event.getSkin(model);
            if (renderer instanceof LivingEntityRenderer<?, ?> living) {
                @SuppressWarnings("unchecked")
                var player = (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) living;
                player.addLayer(new ScabbardLayer(player));
            }
        }
    }
}
