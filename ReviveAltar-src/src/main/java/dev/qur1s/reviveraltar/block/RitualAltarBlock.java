package dev.qur1s.reviveraltar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RitualAltarBlock extends BaseEntityBlock {
    public static final com.mojang.serialization.MapCodec<RitualAltarBlock> CODEC = simpleCodec(RitualAltarBlock::new);

    /** The two candle wicks on the altar model, in block-local space — where the flame lives when lit. */
    private static final List<Vec3> CANDLE_WICK_OFFSETS = List.of(
            new Vec3(11.0 / 16.0, 20.5 / 16.0, 14.0 / 16.0),
            new Vec3(14.0 / 16.0, 19.5 / 16.0, 12.0 / 16.0));

    public RitualAltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.LIT, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT, BlockStateProperties.HORIZONTAL_FACING);
    }

    /** The skull banner faces whoever placed it, like a furnace's front. */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return tryInteract(level, pos, player, ItemStack.EMPTY, true);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = tryInteract(level, pos, player, stack, false);
        return result == InteractionResult.SUCCESS
                ? net.minecraft.world.ItemInteractionResult.SUCCESS
                : net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private InteractionResult tryInteract(Level level, BlockPos pos, Player player, ItemStack heldStack, boolean emptyHand) {
        if (!(level.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        if (emptyHand || heldStack.isEmpty()) {
            ItemStack taken = altar.takeHead(level.isClientSide);
            if (!taken.isEmpty()) {
                if (!level.isClientSide) {
                    player.getInventory().placeItemBackInInventory(taken);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (heldStack.is(Items.PLAYER_HEAD)) {
            if (!level.isClientSide) {
                java.util.UUID owner = RitualAltarBlockEntity.getHeadOwner(heldStack);
                // A ghost's own head, a living player pre-loading their own head, or an op.
                boolean valid = (owner != null && level instanceof ServerLevel serverLevel
                        && (dev.qur1s.reviveraltar.RevivalManager.isAwaitingRevival(serverLevel, owner)
                                || owner.equals(player.getUUID())))
                        || player.hasPermissions(2);
                if (!valid) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.reviveraltar.not_a_ghost_head"), true);
                    return InteractionResult.FAIL;
                }
            }
            if (altar.insertHead(heldStack, level.isClientSide)) {
                if (!level.isClientSide) {
                    heldStack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        // Anything holding a fluid (a bucket, Create's copper backtank, etc.) tries to empty
        // itself into the altar's tanks via the standard fluid-handler item interaction.
        var filled = FluidUtil.getFluidHandler(heldStack);
        if (filled.isPresent()) {
            var result = FluidUtil.tryEmptyContainerAndStow(heldStack, altar.getFluidHandler(), new net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
            if (result.isSuccess()) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** While lit, the two candle wicks flicker — same particle/sound pattern as vanilla candles. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Vec3 offset : CANDLE_WICK_OFFSETS) {
            Vec3 rotated = rotateForFacing(offset, facing);
            double x = pos.getX() + rotated.x();
            double y = pos.getY() + rotated.y();
            double z = pos.getZ() + rotated.z();
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
            float f = random.nextFloat();
            if (f < 0.3F) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
                if (f < 0.17F) {
                    level.playLocalSound(x + 0.5, y + 0.5, z + 0.5, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS,
                            1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F, false);
                }
            }
        }
    }

    /**
     * The model is authored facing north; the blockstate rotates it clockwise (viewed from above) by
     * 0/90/180/270 to match {@code facing}. This applies the same rotation to a block-local offset so
     * particles line up with the candles wherever the altar was actually placed.
     */
    private static Vec3 rotateForFacing(Vec3 offset, Direction facing) {
        double dx = offset.x() - 0.5;
        double dz = offset.z() - 0.5;
        double rx;
        double rz;
        switch (facing) {
            case EAST -> {
                rx = -dz;
                rz = dx;
            }
            case SOUTH -> {
                rx = -dx;
                rz = -dz;
            }
            case WEST -> {
                rx = dz;
                rz = -dx;
            }
            default -> {
                rx = dx;
                rz = dz;
            }
        }
        return new Vec3(rx + 0.5, offset.y(), rz + 0.5);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RITUAL_ALTAR.get(), RitualAltarBlockEntity::serverTick);
    }
}
