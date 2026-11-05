package dev.qur1s.spellclasses;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The Infernal (fire) class does not burn: fire, lava and hot floors deal it no damage, and it is not set
 * ablaze while standing in lava.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class InfernalFireImmunity {
    private InfernalFireImmunity() {
    }

    private static final net.minecraft.resources.ResourceLocation FIRE_CLASS =
            io.redspace.ironsspellbooks.api.registry.SchoolRegistry.FIRE_RESOURCE;

    private static boolean isInfernal(ServerPlayer player) {
        return ClassManager.chosenSchool(player).filter(FIRE_CLASS::equals).isPresent();
    }

    @SubscribeEvent
    static void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !isInfernal(player)) return;
        var source = event.getSource();
        if (source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.CAMPFIRE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isOnFire() || player.tickCount % 5 != 0) return;
        if (player.isInLava() && isInfernal(player)) player.clearFire();
    }
}
