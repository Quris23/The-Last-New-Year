package dev.qur1s.spellclasses.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import uk.co.iceconchy.aerowarptics.warp.CrossDimensionWarp;
import uk.co.iceconchy.aerowarptics.warp.WarpFailure;

/**
 * Lets the Singularity Rift Drive (AeroWarptics) jump to Warp Anchors in other dimensions. AeroWarptics
 * ships the hook ({@link CrossDimensionWarp}) but no implementation; the actual ship transfer is done by
 * {@link ShipDimensionTransfer} (a block-by-block Sable assembly into the other dimension).
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class RiftCrossDimension {
    private RiftCrossDimension() {
    }

    public static boolean available() {
        return ModList.get().isLoaded("aerowarptics");
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!available()) {
            return;
        }
        // Pre-flight only: the real transfer happens when the drive leaves the corridor.
        CrossDimensionWarp.setHandler((airship, destination, anchorPos) -> preflight(destination, anchorPos));
    }

    private static WarpFailure preflight(ServerLevel destination, BlockPos anchorPos) {
        if (destination == null || !destination.isLoaded(anchorPos)) {
            return WarpFailure.ANCHOR_UNAVAILABLE;
        }
        return WarpFailure.NONE;
    }
}
