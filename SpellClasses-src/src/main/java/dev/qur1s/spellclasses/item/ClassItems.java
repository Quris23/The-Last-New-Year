package dev.qur1s.spellclasses.item;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.SlotAttribute;

public final class ClassItems {
    private ClassItems() {
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ITEM, "spellclasses");

    public static final DeferredHolder<Item, ClassSelectionItem> CLASS_BOOK = ITEMS.register("class_book",
            () -> new ClassSelectionItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    /** Grimoire with 10 spell slots (matches the lightning school's spell count) — no school restriction, the class system already gates which spells cast. */
    public static final DeferredHolder<Item, Item> STORM_ATLAS = ITEMS.register("storm_atlas",
            () -> (Item) new SpellBook(10, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON))
                    .withSpellbookAttributes(
                            new AttributeContainer((Holder<Attribute>) (Holder<?>) AttributeRegistry.LIGHTNING_SPELL_POWER, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            new AttributeContainer((Holder<Attribute>) (Holder<?>) AttributeRegistry.MAX_MANA, 200.0, AttributeModifier.Operation.ADD_VALUE)));

    public static final DeferredHolder<Item, LaplaceRingItem> LAPLACE_RING = ITEMS.register("laplace_ring",
            () -> new LaplaceRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /**
     * Waist-slot belts, upgraded one into the next. Tier 1 is Cataclysm's own {@code belt_of_beginner}
     * (its bonus is retuned from +2 to +1 talisman slots elsewhere); these are tiers 2 and 3.
     */
    public static final DeferredHolder<Item, Item> BRASS_BELT = ITEMS.register("brass_belt",
            () -> (Item) new CurioBaseItem(new Item.Properties().stacksTo(1))
                    .withAttributes("waist",
                            new AttributeContainer(SlotAttribute.getOrCreate("talisman"), 2.0, AttributeModifier.Operation.ADD_VALUE),
                            new AttributeContainer(Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADD_VALUE)));

    public static final DeferredHolder<Item, Item> BLACK_STEEL_BELT = ITEMS.register("black_steel_belt",
            () -> (Item) new CurioBaseItem(new Item.Properties().stacksTo(1))
                    .withAttributes("waist",
                            new AttributeContainer(SlotAttribute.getOrCreate("talisman"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            new AttributeContainer(Attributes.ARMOR, 2.0, AttributeModifier.Operation.ADD_VALUE)));

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
