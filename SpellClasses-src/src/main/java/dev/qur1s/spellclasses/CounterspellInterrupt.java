package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.CounterSpellEvent;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;

/**
 * Counterspell now reaches every living creature (see CounterspellTargetsMixin). Iron's own casters and players have
 * their cast cancelled by the spell itself; for any other mob (other mods' bosses and monsters) the hit interrupts
 * what it is doing: it stops using an item and stops in its tracks.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class CounterspellInterrupt {
    private CounterspellInterrupt() {
    }

    @SubscribeEvent
    static void onCounter(CounterSpellEvent event) {
        if (event.isCanceled() || !(event.target instanceof LivingEntity target) || target.level().isClientSide) return;
        // dispel spell buffs from the target
        for (MobEffectInstance inst : new ArrayList<>(target.getActiveEffects())) {
            MobEffectCategory cat = inst.getEffect().value().getCategory();
            boolean spellMod = inst.getEffect().unwrapKey()
                    .map(k -> k.location().getNamespace().equals("irons_spellbooks")).orElse(false);
            // only non-harmful effects granted by spells (e.g. Shadow Cloak); potions and beacons are left alone
            if (spellMod && cat != MobEffectCategory.HARMFUL) {
                target.removeEffect(inst.getEffect());
            }
        }
        if (target instanceof Player || target instanceof IMagicEntity || target instanceof AntiMagicSusceptible) return;
        target.stopUsingItem();
        if (target instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.stopInPlace();
        }
    }
}
