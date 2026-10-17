package dev.qur1s.reviveraltar.event;

import dev.qur1s.reviveraltar.block.ModBlockEntities;
import dev.qur1s.reviveraltar.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Listeners registered on the mod event bus. */
public class ModEventHandlers {

    @SubscribeEvent
    public void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.RITUAL_ALTAR_ITEM.get());
        }
    }

    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.RITUAL_ALTAR.get(),
                (altar, side) -> altar.getFluidHandler());
    }
}
