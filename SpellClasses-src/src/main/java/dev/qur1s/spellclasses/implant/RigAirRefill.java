package dev.qur1s.spellclasses.implant;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import dev.qur1s.spellclasses.OctoRigBackSlot;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * A copper or netherite Tentacle Rig worn as an implant runs on the Air of a Create Backtank. Stand next to a tank (any block with a fluid handler
 * on one of the six blocks around the player's block, the same places the implant Reservoir pours into) and the rig fills its Air from the Oxygen in
 * it: 1 mB of Oxygen = 1 unit of Air, up to 25 mB every half second (the air is the NorthStar oxygen stored in the item). Only Oxygen is taken from the tank, nothing else.
 */
public final class RigAirRefill {
    private static final TagKey<Fluid> OXYGEN = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "oxygen"));
    private static final ResourceLocation NORTHSTAR_OXYGEN = ResourceLocation.fromNamespaceAndPath("northstar", "oxygen");
    private static final int PERIOD_TICKS = 10;
    private static final int MAX_PER_STEP = 25;

    private RigAirRefill() {
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % PERIOD_TICKS != 0) return;
        ItemStack rig = OctoRigBackSlot.installedRig(player);
        if (rig.isEmpty()) return;
        int missing = BacktankUtil.maxAir(rig) - BacktankUtil.getAir(rig);
        if (missing <= 0) return;
        int want = Math.min(missing, MAX_PER_STEP);
        var level = player.level();
        var origin = player.blockPosition();
        int taken = 0;
        for (Direction dir : Direction.values()) {
            var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, origin.relative(dir), dir.getOpposite());
            if (handler == null) continue;
            for (int i = 0; i < handler.getTanks() && taken < want; i++) {
                FluidStack inTank = handler.getFluidInTank(i);
                if (inTank.isEmpty() || !inTank.is(OXYGEN)) continue;
                FluidStack drained = handler.drain(new FluidStack(inTank.getFluid(), Math.min(want - taken, inTank.getAmount())), FluidAction.EXECUTE);
                taken += drained.getAmount();
            }
            if (taken >= want) break;
        }
        if (taken > 0) {
            // CosmeticSuit makes the rigs run on NorthStar oxygen (the "northstar:oxygen" component of the item); Create's own air is ignored
            @SuppressWarnings("unchecked")
            DataComponentType<Integer> oxygen = (DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(NORTHSTAR_OXYGEN);
            DataComponentType<Integer> store = oxygen != null ? oxygen : AllDataComponents.BACKTANK_AIR;
            ItemStack updated = rig.copy();
            updated.set(store, Math.min(BacktankUtil.maxAir(rig), updated.getOrDefault(store, 0) + taken));
            OctoRigBackSlot.replace(player, updated);
        }
    }
}
