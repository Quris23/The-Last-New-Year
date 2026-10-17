package dev.qur1s.reviveraltar.client;

import dev.qur1s.reviveraltar.ReviveAltarMod;
import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Set;

/**
 * Hides the normal survival HUD for the local player while they are a ghost.
 * (Spectator mode already hides most of this, this is a safety net.)
 */
@EventBusSubscriber(modid = ReviveAltarMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEventHandlers {
    private static final Set<ResourceLocation> HIDDEN_WHEN_GHOST = Set.of(
            VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.JUMP_METER
    );

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player == null || !RevivalManager.isGhost(player)) return;

        if (HIDDEN_WHEN_GHOST.contains(event.getName())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientGhostState.clear();
    }
}
