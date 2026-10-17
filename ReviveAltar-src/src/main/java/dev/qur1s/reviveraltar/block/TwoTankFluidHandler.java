package dev.qur1s.reviveraltar.block;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/** Routes fills/drains to whichever of the two tanks matches the fluid involved. */
public class TwoTankFluidHandler implements IFluidHandler {
    private final FluidTank tankA;
    private final FluidTank tankB;

    public TwoTankFluidHandler(FluidTank tankA, FluidTank tankB) {
        this.tankA = tankA;
        this.tankB = tankB;
    }

    private FluidTank tankFor(FluidStack stack) {
        if (tankA.isFluidValid(stack)) return tankA;
        if (tankB.isFluidValid(stack)) return tankB;
        return null;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank == 0 ? tankA.getFluid() : tankB.getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? tankA.getCapacity() : tankB.getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 ? tankA.isFluidValid(stack) : tankB.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        FluidTank target = tankFor(resource);
        return target == null ? 0 : target.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidTank target = tankFor(resource);
        return target == null ? FluidStack.EMPTY : target.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!tankA.isEmpty()) return tankA.drain(maxDrain, action);
        return tankB.drain(maxDrain, action);
    }
}
