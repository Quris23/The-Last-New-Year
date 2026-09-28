package dev.qur1s.spellclasses;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Weak passive regen (Beacon-tier) for a class standing in its matching environment. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class ColdSweatHealEvents {
    private ColdSweatHealEvents() {
    }

    private static final int CHECK_INTERVAL_TICKS = 40;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ColdSweatCompat.isLoaded()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        ClassManager.chosenSchool(player).ifPresent(school -> {
            boolean active = ColdSweatCompat.matchesEnvironment(player, school);
            if (active) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, CHECK_INTERVAL_TICKS + 20, 0, true, false, true));
            }
            ColdSweatCompat.syncPowerBuff(player, school, active);
        });
    }
}
