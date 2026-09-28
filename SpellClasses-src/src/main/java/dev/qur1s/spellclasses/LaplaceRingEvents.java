package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.item.ClassEffects;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Whenever the Laplace Factor buff leaves an entity — naturally or cut short (milk, /effect clear, ...) — it leaves a burnout debuff behind. */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class LaplaceRingEvents {
    private LaplaceRingEvents() {
    }

    private static final int DEBUFF_DURATION_TICKS = 60 * 20;
    private static final float MANA_COST_MULTIPLIER = 0.8f;

    @SubscribeEvent
    static void onExpired(MobEffectEvent.Expired event) {
        applyBurnout(event.getEffectInstance(), event.getEntity());
    }

    @SubscribeEvent
    static void onRemoved(MobEffectEvent.Remove event) {
        applyBurnout(event.getEffectInstance(), event.getEntity());
    }

    @SubscribeEvent
    static void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player.hasEffect(ClassEffects.LAPLACE_FACTOR)) {
            event.setManaCost((int) (event.getManaCost() * MANA_COST_MULTIPLIER));
        }
    }

    private static void applyBurnout(MobEffectInstance instance, LivingEntity entity) {
        if (instance == null || !instance.getEffect().is(ClassEffects.LAPLACE_FACTOR)) return;

        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION_TICKS, 0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DEBUFF_DURATION_TICKS, 0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, DEBUFF_DURATION_TICKS, 0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION_TICKS, 0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, DEBUFF_DURATION_TICKS, 0, false, true, true));
    }
}
