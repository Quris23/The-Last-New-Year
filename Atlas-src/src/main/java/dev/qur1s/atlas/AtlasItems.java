package dev.qur1s.atlas;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AtlasItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AtlasMod.MODID);

    public static final DeferredItem<AtlasItem> ATLAS =
            ITEMS.registerItem("atlas", AtlasItem::new, new Item.Properties().stacksTo(1));

    private AtlasItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(AtlasItems::onBuildTabs);
    }

    private static void onBuildTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ATLAS.get());
        }
    }
}
