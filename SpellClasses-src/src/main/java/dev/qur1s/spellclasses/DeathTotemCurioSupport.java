package dev.qur1s.spellclasses;

import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Born in Chaos' Death Totem only ever checks the main/off hand for its death-save, so wearing it
 * in the (now curio-equippable) talisman slot did nothing. Replicates the same save here for the
 * curio case: cancel the death, set health to 3, consume the totem, and apply the same
 * regen/damage-boost/magic-depletion effects the hand version grants.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class DeathTotemCurioSupport {
    private DeathTotemCurioSupport() {
    }

    private static final int DAMAGE_BOOST_DURATION = 200;
    private static final int REGEN_DURATION = 100;
    private static final int MAGIC_DEPLETION_DURATION = 200;

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(BornInChaosV1ModMobEffects.MAGIC_DEPLETION)) return;

        CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(BornInChaosV1ModItems.DEATH_TOTEM.get()))
                .ifPresent(result -> {
                    event.setCanceled(true);
                    entity.setHealth(3.0f);
                    result.stack().shrink(1);

                    Level level = entity.level();
                    var pos = entity.blockPosition();
                    if (!level.isClientSide()) {
                        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                        level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.1f, 1.0f);
                    }

                    entity.removeAllEffects();
                    entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, 4));
                    entity.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, MAGIC_DEPLETION_DURATION, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DAMAGE_BOOST_DURATION, 1));

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 15, 0.8, 0.8, 0.8, 0.1);
                    }

                    boolean fullMantle = entity.getItemBySlot(EquipmentSlot.HEAD).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_HELMET.get())
                            && entity.getItemBySlot(EquipmentSlot.CHEST).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_CHESTPLATE.get())
                            && entity.getItemBySlot(EquipmentSlot.LEGS).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_LEGGINGS.get())
                            && entity.getItemBySlot(EquipmentSlot.FEET).is(BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_BOOTS.get());
                    if (!fullMantle && entity instanceof Player player) {
                        player.getFoodData().setFoodLevel(1);
                        player.getFoodData().setSaturation(0.0f);
                    }
                });
    }
}
