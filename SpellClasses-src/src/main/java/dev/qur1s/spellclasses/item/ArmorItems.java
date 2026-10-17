package dev.qur1s.spellclasses.item;

import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Helmet, leggings and boots of the Lightbringer (Paladin) set. */
public final class ArmorItems {
    private ArmorItems() {
    }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "spellclasses");

    /** Per piece (all four, the chestplate is rewritten in StaffAttributeOverrides): +150 mana, +15% Holy spell power. */
    public static final double MANA = 150.0;
    public static final double HOLY_POWER = 0.15;

    public static final DeferredHolder<Item, Item> PALADIN_HELMET = ITEMS.register("paladin_helmet",
            () -> piece(ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> PALADIN_LEGGINGS = ITEMS.register("paladin_leggings",
            () -> piece(ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> PALADIN_BOOTS = ITEMS.register("paladin_boots",
            () -> piece(ArmorItem.Type.BOOTS));

    private static Item piece(ArmorItem.Type type) {
        return new LightbringerArmorItem(type,
                new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().durability(type.getDurability(37)),
                new io.redspace.ironsspellbooks.item.weapons.AttributeContainer(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA,
                        MANA, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                new io.redspace.ironsspellbooks.item.weapons.AttributeContainer(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.HOLY_SPELL_POWER,
                        HOLY_POWER, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ArmorItems::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeTabRegistry.EQUIPMENT_TAB.getKey())) {
            event.accept(PALADIN_HELMET.get());
            event.accept(PALADIN_LEGGINGS.get());
            event.accept(PALADIN_BOOTS.get());
        }
    }
}
