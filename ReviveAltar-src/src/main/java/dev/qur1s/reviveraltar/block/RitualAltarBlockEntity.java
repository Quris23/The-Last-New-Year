package dev.qur1s.reviveraltar.block;

import dev.qur1s.reviveraltar.RevivalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class RitualAltarBlockEntity extends BlockEntity {
    /** How long the beacon-beam finish effect is shown for, in ticks. */
    public static final int BEAM_DURATION = 20; // 1 second
    public static final int FLUID_CAPACITY = 1000;

    private ItemStack headSlot = ItemStack.EMPTY;
    /** Both tanks are full (1000/1000 Mana + 1000/1000 Blood) — glowing, ready to revive as soon as a valid head is on it. */
    private boolean activated = false;

    /** Server game time the ritual finished at; used client-side to show the beacon beam briefly. -1 = inactive. */
    private long beamStartGameTime = -1;

    private final FluidTank manaTank = new FluidTank(FLUID_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            var mana = AltarFluids.mana();
            return mana != null && stack.getFluid().isSame(mana);
        }

        @Override
        protected void onContentsChanged() {
            RitualAltarBlockEntity.this.onTankChanged();
        }
    };

    private final FluidTank bloodTank = new FluidTank(FLUID_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            var blood = AltarFluids.blood();
            return blood != null && stack.getFluid().isSame(blood);
        }

        @Override
        protected void onContentsChanged() {
            RitualAltarBlockEntity.this.onTankChanged();
        }
    };

    private final TwoTankFluidHandler fluidHandler = new TwoTankFluidHandler(manaTank, bloodTank);

    public RitualAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RITUAL_ALTAR.get(), pos, state);
    }

    private void onTankChanged() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            refreshState(serverLevel, worldPosition, getBlockState());
        }
    }

    /** setChanged() alone only marks this for saving - it does not notify nearby clients. */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidTank getManaTank() {
        return manaTank;
    }

    public FluidTank getBloodTank() {
        return bloodTank;
    }

    public boolean insertHead(ItemStack stack, boolean clientSide) {
        if (!headSlot.isEmpty()) {
            return false;
        }
        if (!clientSide) {
            headSlot = stack.copyWithCount(1);
            setChanged();
            if (level instanceof ServerLevel serverLevel) {
                // A still-living owner: nothing to revive yet, just remember this altar so their
                // death re-checks it automatically.
                UUID owner = getHeadOwner(headSlot);
                if (owner != null && !RevivalManager.isAwaitingRevival(serverLevel, owner)) {
                    dev.qur1s.reviveraltar.data.RevivalSavedData.get(serverLevel)
                            .setPrimedAltar(owner, level.dimension(), worldPosition);
                }
                refreshState(serverLevel, worldPosition, getBlockState());
            }
        }
        return true;
    }

    public ItemStack takeHead(boolean clientSide) {
        if (headSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!clientSide) {
            ItemStack out = headSlot;
            headSlot = ItemStack.EMPTY;
            setChanged();
            return out;
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    public static UUID getHeadOwner(ItemStack head) {
        ResolvableProfile profile = head.get(DataComponents.PROFILE);
        if (profile == null) return null;
        Optional<UUID> id = profile.id();
        return id.orElse(null);
    }

    /** Re-derives the activated (glowing) state from the tanks, and revives if everything now lines up. */
    public void refreshState(ServerLevel level, BlockPos pos, BlockState state) {
        boolean full = manaTank.getFluidAmount() >= FLUID_CAPACITY && bloodTank.getFluidAmount() >= FLUID_CAPACITY;
        if (full != activated) {
            activated = full;
            level.setBlock(pos, level.getBlockState(pos).setValue(BlockStateProperties.LIT, activated), 3);
            if (activated) {
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            setChanged();
        }

        if (!activated || headSlot.isEmpty()) {
            return;
        }
        UUID owner = getHeadOwner(headSlot);
        if (owner == null || !RevivalManager.isAwaitingRevival(level, owner)) {
            return;
        }

        String name = RevivalManager.getName(level, owner);
        headSlot = ItemStack.EMPTY;
        manaTank.drain(FLUID_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
        bloodTank.drain(FLUID_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
        activated = false;
        beamStartGameTime = level.getGameTime();
        level.setBlock(pos, level.getBlockState(pos).setValue(BlockStateProperties.LIT, false), 3);
        setChanged();

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.9;
        double cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 60, 0.4, 0.6, 0.4, 0.4);

        RevivalManager.revive(level, owner, name, pos);
    }

    /** Called when a primed owner dies; re-checks whether this altar can now revive them. */
    public void triggerAutoCharge() {
        if (level instanceof ServerLevel serverLevel) {
            refreshState(serverLevel, worldPosition, getBlockState());
        }
    }

    /** Same cadence as a powered beacon's hum (every 80 ticks) while charged and waiting to revive. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, RitualAltarBlockEntity be) {
        if (be.activated && level.getGameTime() % 80L == 0L) {
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!headSlot.isEmpty()) {
            tag.put("Head", headSlot.save(registries, new CompoundTag()));
        }
        tag.putBoolean("Activated", activated);
        tag.putLong("BeamStart", beamStartGameTime);
        tag.put("ManaTank", manaTank.writeToNBT(registries, new CompoundTag()));
        tag.put("BloodTank", bloodTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        headSlot = tag.contains("Head") ? ItemStack.parse(registries, tag.getCompound("Head")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        activated = tag.getBoolean("Activated");
        beamStartGameTime = tag.getLong("BeamStart");
        if (tag.contains("ManaTank")) {
            manaTank.readFromNBT(registries, tag.getCompound("ManaTank"));
        }
        if (tag.contains("BloodTank")) {
            bloodTank.readFromNBT(registries, tag.getCompound("BloodTank"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getHeadSlot() {
        return headSlot;
    }

    public long getBeamStartGameTime() {
        return beamStartGameTime;
    }
}
