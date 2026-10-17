package dev.qur1s.spellclasses.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the Sconce Button, only when Supplementaries (which provides the Sconce Lever it builds on) is present. */
public final class SconceButtons {
    private SconceButtons() {
    }

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "spellclasses");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "spellclasses");

    public static final DeferredHolder<Block, Block> SCONCE_BUTTON = BLOCKS.register("sconce_button",
            () -> new SconceButtonBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 14 : 0)));

    public static final DeferredHolder<Item, Item> SCONCE_BUTTON_ITEM = ITEMS.register("sconce_button",
            () -> new BlockItem(SCONCE_BUTTON.get(), new Item.Properties()));

    public static void register(IEventBus modBus) {
        if (!ModList.get().isLoaded("supplementaries")) return;
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(SconceButtons::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(new ItemStack(SCONCE_BUTTON_ITEM.get()));
        }
    }
}
