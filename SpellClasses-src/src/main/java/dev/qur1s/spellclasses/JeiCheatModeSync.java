package dev.qur1s.spellclasses;

import mezz.jei.common.Internal;
import mezz.jei.gui.util.CheatModeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Turns JEI's cheat mode on automatically the moment the player enters creative. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class JeiCheatModeSync {
    private JeiCheatModeSync() {
    }

    private static boolean wasCreative = false;
    /** CheatModeUtil is JEI's internal API and moves around between versions; if it's missing, stop trying instead of crashing every tick. */
    private static boolean disabled = false;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (disabled) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            wasCreative = false;
            return;
        }

        boolean isCreative = player.isCreative();
        if (isCreative && !wasCreative) {
            try {
                CheatModeUtil.setCheatModeEnabled(Internal.getClientToggleState(), true);
            } catch (Throwable t) {
                disabled = true;
                return;
            }
        }
        wasCreative = isCreative;
    }
}
