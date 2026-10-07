package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;

/** The Netherite Battlemage armor gives +10% spell power per piece instead of +5%. */
@EventBusSubscriber(modid = "spellclasses")
public final class NetheriteMageSpellPower {
    private static final double SPELL_POWER = 0.10;

    private NetheriteMageSpellPower() {
    }

    @SubscribeEvent
    static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("irons_spellbooks") || !id.getPath().startsWith("netherite_mage_")) {
            return;
        }
        for (var entry : new ArrayList<>(event.getModifiers())) {
            if (entry.attribute().equals(AttributeRegistry.SPELL_POWER)) {
                event.removeModifier(entry.attribute(), entry.modifier().id());
                event.addModifier(entry.attribute(),
                        new AttributeModifier(entry.modifier().id(), SPELL_POWER, entry.modifier().operation()),
                        entry.slot());
            }
        }
    }
}
