package dev.qur1s.spellclasses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * The Great Frost (Legendary Monsters) takes its attributes from its own getDefaultAttributeModifiers(ItemStack)
 * - attack damage 6 (shown as 7) plus, by what looks like a mistake in the mod, a second ATTACK_DAMAGE modifier of
 * -2.6 that carries the attack-speed id. NeoForge asks every item for its effective modifiers through this event,
 * so the change is made here whatever way the defaults were produced: damage 18 as displayed (modifier 17 plus
 * the wielder's base 1), and the bogus -2.6 damage entry removed so the sword really hits for 18.
 */
@EventBusSubscriber(modid = "spellclasses")
public final class GreatFrostDamage {
    private static final ResourceLocation GREAT_FROST =
            ResourceLocation.fromNamespaceAndPath("legendary_monsters", "the_great_frost");
    private static Item item;

    private GreatFrostDamage() {
    }

    private static Item greatFrost() {
        if (item == null) {
            item = BuiltInRegistries.ITEM.get(GREAT_FROST);
        }
        return item;
    }

    @SubscribeEvent
    static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        Item frost = greatFrost();
        if (frost == Items.AIR || !stack.is(frost)) {
            return;
        }
        event.removeModifier(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_SPEED_ID);
        event.replaceModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 18.0 - 1.0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
}
