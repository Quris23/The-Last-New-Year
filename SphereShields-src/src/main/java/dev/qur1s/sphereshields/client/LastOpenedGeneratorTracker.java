package dev.qur1s.sphereshields.client;

import com.anton.shieldgenerators.ShieldGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Remembers the position of whichever shield generator the player last right-clicked. The client's
 * own copy of {@code ShieldGeneratorMenu} has a null block entity reference (confirmed via logging -
 * its GUI data flows through a separate synced {@code ContainerData}, not a direct block entity
 * pointer), so {@link ShieldAccessButtonInjector} can't read the block entity off the menu the way
 * {@code ShieldGeneratorMenuAccessor} was meant for. Looking the position up in the client level
 * instead gets the real, properly NBT-synced block entity.
 */
@EventBusSubscriber(modid = "sphereshields", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class LastOpenedGeneratorTracker {
    private LastOpenedGeneratorTracker() {
    }

    private static BlockPos lastPos;

    static BlockPos get() {
        return lastPos;
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (state.getBlock() instanceof ShieldGeneratorBlock) {
            lastPos = event.getPos();
        }
    }
}
