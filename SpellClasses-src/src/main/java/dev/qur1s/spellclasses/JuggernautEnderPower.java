package dev.qur1s.spellclasses;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;

/**
 * Cosmic spells are cut from the pack, and the Reinforced Juggernaut armor (Echoing Magic) is an Ender-class piece:
 * its Cosmic spell power and its all-spells spell power both become Ender spell power - same amount, same operation,
 * same slot.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class JuggernautEnderPower {
    private static final ResourceLocation COSMIC = ResourceLocation.fromNamespaceAndPath("hazentouvelib", "cosmic_spell_power");
    private static final ResourceLocation ALL_SPELLS = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spell_power");
    private static final ResourceLocation ENDER = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ender_spell_power");

    private JuggernautEnderPower() {
    }

    @SubscribeEvent
    static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("echoing_magic") || !id.getPath().startsWith("reinforced_juggernaut_")) {
            return;
        }
        Holder<Attribute> ender = BuiltInRegistries.ATTRIBUTE.getHolder(ENDER).orElse(null);
        if (ender == null) {
            return;
        }
        for (var entry : new ArrayList<>(event.getModifiers())) {
            if (entry.attribute().unwrapKey().map(k -> k.location().equals(COSMIC) || k.location().equals(ALL_SPELLS)).orElse(false)) {
                event.removeModifier(entry.attribute(), entry.modifier().id());
                event.addModifier(ender,
                        new AttributeModifier(entry.modifier().id(), entry.modifier().amount(), entry.modifier().operation()),
                        entry.slot());
            }
        }
    }
}
