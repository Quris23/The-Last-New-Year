package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/**
 * While Heartstop (Остановка Сердца) is active, Iron's Spellbooks banks 50% of every blocked hit and deals the sum
 * when the effect ends. Any healing received in the meantime (potions, food, regeneration, healing spells) pays
 * that bank down instead of being wasted on a full health bar.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class HeartstopHealing {
    private HeartstopHealing() {
    }

    @SubscribeEvent
    static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        if (!player.hasEffect(MobEffectRegistry.HEARTSTOP)) return;
        var data = MagicData.getPlayerMagicData(player).getSyncedData();
        float banked = data.getHeartstopAccumulatedDamage();
        if (banked <= 0) return;
        data.setHeartstopAccumulatedDamage(Math.max(0f, banked - event.getAmount()));
    }
}
