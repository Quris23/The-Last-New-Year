package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Cinematic Interactions opens a block's menu for a scene through {@code getMenuProvider}. Cold Sweat's sewing table opens
 * itself directly (it is its own menu provider) and answers nothing to that call.
 */
@Mixin(targets = "com.momosoftworks.coldsweat.common.block.SewingTableBlock", remap = false)
public abstract class SewingTableMenuProviderMixin {
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return (MenuProvider) (Object) this;
    }
}
