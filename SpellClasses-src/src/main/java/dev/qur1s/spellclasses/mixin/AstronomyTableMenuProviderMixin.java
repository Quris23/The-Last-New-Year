package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/** The same for Northstar's astronomy table, whose block entity is the menu provider. */
@Mixin(targets = "com.lightning.northstar.block.tech.astronomy_table.AstronomyTableBlock", remap = false)
public abstract class AstronomyTableMenuProviderMixin {
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }
}
