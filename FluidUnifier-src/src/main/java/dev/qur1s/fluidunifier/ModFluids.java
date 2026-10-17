package dev.qur1s.fluidunifier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    private ModFluids() {}

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, FluidUnifier.MODID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, FluidUnifier.MODID);

    public static final DeferredHolder<FluidType, FluidType> OXYGEN_TYPE = FLUID_TYPES.register("oxygen",
            () -> new FluidType(FluidType.Properties.create().density(-1).viscosity(0).temperature(0)
                    .canSwim(false).canDrown(false).supportsBoating(false)
                    .sound(SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> OXYGEN =
            FLUIDS.register("oxygen", () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> OXYGEN_FLOWING =
            FLUIDS.register("oxygen_flowing", () -> new BaseFlowingFluid.Flowing(properties()));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(OXYGEN_TYPE, OXYGEN, OXYGEN_FLOWING);
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        modEventBus.addListener(RegisterClientExtensionsEvent.class, event ->
                event.registerFluidType(new IClientFluidTypeExtensions() {
                    // Reuses Northstar's gas textures so the merged Oxygen looks like the original.
                    private static final ResourceLocation STILL =
                            ResourceLocation.fromNamespaceAndPath("northstar", "fluid/oxygen_still");
                    private static final ResourceLocation FLOW =
                            ResourceLocation.fromNamespaceAndPath("northstar", "fluid/oxygen_flow");

                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOW;
                    }
                }, OXYGEN_TYPE.get()));
    }
}
