package dev.qur1s.spellclasses;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * {@link ColdSweatHooks#syncResistance} (via {@link ColdSweatCompat}) previously only ran on login
 * and class selection - fine for the class-based resistance it was built for, but the Soul Lantern
 * heat immunity folded into it (see {@link ColdSweatHooks#holdingSoulLanternInNether}) needs to react
 * to the player picking the lantern up or putting it down mid-session. Re-running the same sync
 * periodically covers that without adding a second writer to the HEAT_RESISTANCE/COLD_RESISTANCE
 * traits - it's the exact same call, just on a timer too.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class SoulLanternHeatImmunityTick {
    private SoulLanternHeatImmunityTick() {
    }

    private static final int CHECK_INTERVAL_TICKS = 20;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;
        if (player.level().dimension() != net.minecraft.world.level.Level.NETHER) return;

        ColdSweatCompat.syncResistance(player, ClassManager.chosenSchool(player));
    }
}
