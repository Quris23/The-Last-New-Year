package dev.qur1s.spellclasses.block;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceLeverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A Supplementaries Sconce Lever that pops back out by itself after a moment, like a button. */
public class SconceButtonBlock extends SconceLeverBlock {
    public static final int PRESS_TICKS = 40;

    public SconceButtonBlock(Properties properties) {
        super(properties, () -> ParticleTypes.FLAME);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(POWERED)) return InteractionResult.CONSUME;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        setPowered(state, level, pos);
        level.scheduleTick(pos, this, PRESS_TICKS);
        level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            setPowered(state, level, pos);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.5f);
        } else {
            super.tick(state, level, pos, random);
        }
    }
}
