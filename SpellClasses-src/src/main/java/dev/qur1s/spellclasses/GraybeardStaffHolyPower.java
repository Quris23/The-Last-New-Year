package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/** The Elder's Staff (graybeard_staff) gives +15% Holy (Light) spell power while held, on top of what it already has. */
@EventBusSubscriber(modid = "spellclasses")
public final class GraybeardStaffHolyPower {
    private static final ResourceLocation STAFF = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "graybeard_staff");
    private static final ResourceLocation MODIFIER = ResourceLocation.fromNamespaceAndPath("spellclasses", "graybeard_holy_power");

    private GraybeardStaffHolyPower() {
    }

    @SubscribeEvent
    static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!STAFF.equals(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) {
            return;
        }
        event.addModifier(AttributeRegistry.HOLY_SPELL_POWER,
                new AttributeModifier(MODIFIER, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                EquipmentSlotGroup.MAINHAND);
    }
}
