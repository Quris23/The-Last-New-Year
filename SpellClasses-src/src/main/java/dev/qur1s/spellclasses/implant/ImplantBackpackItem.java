package dev.qur1s.spellclasses.implant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;

/**
 * The hidden backpack every cyber-upgraded player carries "inside": it only exists as the holder of the implanted upgrades
 * (twelve upgrade slots, a small storage the upgrades work on). Never given out as a normal item.
 */
public class ImplantBackpackItem extends BackpackItem {
    public static final int UPGRADE_SLOTS = 12;
    public static final int STORAGE_SLOTS = 27;

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "spellclasses");
    public static final DeferredHolder<Item, ImplantBackpackItem> INSTANCE = ITEMS.register("implant_backpack",
            () -> new ImplantBackpackItem());

    /** Implant-only upgrades: fill the tank of the implanted Tank upgrade (see ImplantPumps). */
    public static final DeferredHolder<Item, Item> BLOOD_PUMP = ITEMS.register("blood_pump_upgrade",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> MANA_PUMP = ITEMS.register("mana_pump_upgrade",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public ImplantBackpackItem() {
        super(() -> STORAGE_SLOTS, () -> UPGRADE_SLOTS, ModBlocks.BACKPACK, p -> p.fireResistant());
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
