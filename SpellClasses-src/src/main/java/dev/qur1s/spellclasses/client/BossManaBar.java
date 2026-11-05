package dev.qur1s.spellclasses.client;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * The mana of the Ascended One (see AscendedOneMana) arrives as a boss bar named "Мана". Vanilla orders boss bars unpredictably
 * (the one with the higher value can end up on top), so that bar is not drawn in the list: its value is taken and drawn here at a
 * fixed place, directly under the Ascended One's own health bar at the top of the screen.
 */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class BossManaBar {
    private static final int Y = 42;
    private static float progress;
    private static long lastSeenMs;

    private BossManaBar() {
    }

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!"Мана".equals(event.getBossEvent().getName().getString())) return;
        progress = event.getBossEvent().getProgress();
        lastSeenMs = System.currentTimeMillis();
        event.setCanceled(true);
    }

    @SubscribeEvent
    static void onGui(RenderGuiEvent.Post event) {
        if (System.currentTimeMillis() - lastSeenMs > 300) return;
        GuiGraphics g = event.getGuiGraphics();
        int w = 182, h = 5;
        int x = g.guiWidth() / 2 - w / 2;
        int filled = Math.round((w - 2) * Math.max(0f, Math.min(1f, progress)));
        g.fill(x, Y, x + w, Y + h, 0xFF101828);
        g.fill(x + 1, Y + 1, x + 1 + filled, Y + h - 1, 0xFF3FA9FF);
    }
}
