package dev.qur1s.spellclasses;

import com.github.L_Ender.cataclysm.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * Cataclysm's Blazing Grips ships with no gameplay effect of its own (just an equip sound and a
 * tooltip line). This grants +4 attack damage while worn and ignites whatever the wearer hits.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class BlazingGripsTweak {
    private BlazingGripsTweak() {
    }

    private static final double ATTACK_DAMAGE_BONUS = 2.0;
    private static final int IGNITE_SECONDS = 5;

    @SubscribeEvent
    static void onAttributeModifier(CurioAttributeModifierEvent event) {
        if (!event.getItemStack().is(ModItems.BLAZING_GRIPS.get())) return;

        // One modifier per curio slot index - hands has 2 slots, and a shared id here would let the
        // second glove's modifier silently overwrite the first's in the entity's attribute map.
        event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("spellclasses", "blazing_grips_damage_" + event.getSlotContext().index()),
                ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE));
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        boolean hasGrips = CuriosApi.getCuriosInventory(attacker)
                .map(handler -> handler.findFirstCurio(ModItems.BLAZING_GRIPS.get()).isPresent())
                .orElse(false);
        if (!hasGrips) return;

        event.getEntity().igniteForSeconds(IGNITE_SECONDS);
    }
}
