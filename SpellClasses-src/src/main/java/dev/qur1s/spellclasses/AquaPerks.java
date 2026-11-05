package dev.qur1s.spellclasses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The Aqua class breathes under water, swims fast, regenerates health and recovers mana twice as fast while in water. */
@EventBusSubscriber(modid = "spellclasses")
public final class AquaPerks {
    private AquaPerks() {
    }

    private static final ResourceLocation AQUA_CLASS = ResourceLocation.fromNamespaceAndPath("somakespells", "aqua");
    private static final ResourceLocation SWIM_ID = ResourceLocation.fromNamespaceAndPath("spellclasses", "aqua_swim_speed");
    private static final double SWIM_BONUS = 1.0;
    private static final ResourceLocation MANA_ID = ResourceLocation.fromNamespaceAndPath("spellclasses", "aqua_water_mana_regen");
    /** Mana regeneration of the Aqua class in water: +100% (twice as fast). */
    private static final double WATER_MANA_REGEN_BONUS = 1.0;

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        boolean aqua = ClassManager.chosenSchool(player).filter(AQUA_CLASS::equals).isPresent();
        AttributeInstance swim = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (swim != null) {
            boolean has = swim.getModifier(SWIM_ID) != null;
            if (aqua && !has) {
                swim.addTransientModifier(new AttributeModifier(SWIM_ID, SWIM_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (!aqua && has) {
                swim.removeModifier(SWIM_ID);
            }
        }
        AttributeInstance mana = player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MANA_REGEN);
        if (mana != null) {
            boolean has = mana.getModifier(MANA_ID) != null;
            boolean wet = aqua && player.isInWater();
            if (wet && !has) {
                mana.addTransientModifier(new AttributeModifier(MANA_ID, WATER_MANA_REGEN_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (!wet && has) {
                mana.removeModifier(MANA_ID);
            }
        }
        if (aqua && player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, false));
        }
        if (aqua && player.isInWater()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false, false));
        }
    }
}
