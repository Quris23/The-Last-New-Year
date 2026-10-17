package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.data.SharedQuestStore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** A player who joins gets the shared "История" progress recorded while he was away. */
@EventBusSubscriber(modid = "spellclasses")
public final class SharedQuestEvents {
    private SharedQuestEvents() {
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            SharedQuestStore.get(player.getServer()).applyTo(player);
        }
    }
}
