package dev.qur1s.spellclasses.item;

import io.redspace.ironsspellbooks.registries.CreativeTabRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class ClassCreativeTab {
    private ClassCreativeTab() {
    }

    @SubscribeEvent
    static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeTabRegistry.EQUIPMENT_TAB.getKey())) {
            event.accept(ClassItems.STORM_ATLAS.get());
            event.accept(ClassItems.CLASS_BOOK.get());
            event.accept(ClassItems.LAPLACE_RING.get());
            event.accept(ClassItems.BRASS_BELT.get());
            event.accept(ClassItems.BLACK_STEEL_BELT.get());
        }
    }
}
