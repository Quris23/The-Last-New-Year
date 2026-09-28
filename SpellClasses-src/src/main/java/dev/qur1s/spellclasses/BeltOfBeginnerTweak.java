package dev.qur1s.spellclasses;

import com.github.L_Ender.cataclysm.init.ModItems;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * Cataclysm's Belt of Beginner grants +2 talisman slots out of the box; retuned down to +1 here so
 * it's the true tier-1 step below the Brass Belt (+2) and Black Steel Belt (+3), without touching
 * Cataclysm's own item registration.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class BeltOfBeginnerTweak {
    private BeltOfBeginnerTweak() {
    }

    private static final double NEW_TALISMAN_BONUS = 1.0;

    @SubscribeEvent
    static void onAttributeModifier(CurioAttributeModifierEvent event) {
        if (!event.getItemStack().is(ModItems.BELT_OF_BEGINNER.get())) return;

        Holder<Attribute> talisman = SlotAttribute.getOrCreate("talisman");
        var existing = ImmutableList.copyOf(event.getModifiers().get(talisman));
        if (existing.isEmpty()) return;

        event.removeAttribute(talisman);
        for (AttributeModifier modifier : existing) {
            event.addModifier(talisman, new AttributeModifier(modifier.id(), NEW_TALISMAN_BONUS, modifier.operation()));
        }
    }
}
