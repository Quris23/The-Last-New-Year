package dev.qur1s.spellclasses.compat;

import dev.dyslexic.cinematic.api.AddonStations;
import dev.dyslexic.cinematic.api.client.AddonScenes;
import dev.dyslexic.cinematic.client.AnvilStation;
import dev.dyslexic.cinematic.client.CartographyStation;
import dev.qur1s.spellclasses.SpellClasses;
import dev.qur1s.spellclasses.mixin.MenuStationAccessor;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

/**
 * Iron's Arcane Anvil and Cataclysm's Mechanical Fusion Anvil are worked on with Cinematic Interactions' own anvil scene
 * (the hammer, the forging blows), exactly like the vanilla anvil: both are anvil-shaped, with two inputs and a result.
 */
public final class CinematicAnvils {
    private static final ResourceLocation ARCANE = ResourceLocation.fromNamespaceAndPath(SpellClasses.MODID, "arcane_anvil");
    private static final ResourceLocation FUSION = ResourceLocation.fromNamespaceAndPath(SpellClasses.MODID, "mechanical_fusion_anvil");

    private static final ResourceLocation SEWING = ResourceLocation.fromNamespaceAndPath(SpellClasses.MODID, "sewing_table");
    private static final ResourceLocation ASTRONOMY = ResourceLocation.fromNamespaceAndPath(SpellClasses.MODID, "astronomy_table");

    private CinematicAnvils() {
    }

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        if (!ModList.get().isLoaded("cinematic")) return;
        modBus.addListener(CinematicAnvils::onCommonSetup);
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) modBus.addListener(CinematicAnvils::onClientSetup);
    }

    private static List<Direction> sides(BlockState state) {
        Direction facing = state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH);
        return List.of(facing.getClockWise(), facing.getCounterClockWise());
    }

    private static List<Direction> allSides(BlockState state) {
        return List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    /** The astronomy table's board tilts down towards the side the block faces: that is where it is worked from. */
    private static List<Direction> frontSide(BlockState state) {
        return List.of(state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH));
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("cold_sweat")) {
                AddonStations.register(new AddonStations.Definition(SEWING,
                        ResourceLocation.fromNamespaceAndPath("cold_sweat", "sewing_table"),
                        com.momosoftworks.coldsweat.common.container.SewingContainer.class, CinematicAnvils::allSides));
            }
            if (ModList.get().isLoaded("northstar")) {
                AddonStations.register(new AddonStations.Definition(ASTRONOMY,
                        ResourceLocation.fromNamespaceAndPath("northstar", "astronomy_table"),
                        com.lightning.northstar.block.tech.astronomy_table.AstronomyTableMenu.class, CinematicAnvils::frontSide));
            }
            if (ModList.get().isLoaded("irons_spellbooks")) {
                AddonStations.register(new AddonStations.Definition(ARCANE,
                        ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "arcane_anvil"),
                        io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu.class, CinematicAnvils::sides));
            }
            if (ModList.get().isLoaded("cataclysm")) {
                AddonStations.register(new AddonStations.Definition(FUSION,
                        ResourceLocation.fromNamespaceAndPath("cataclysm", "mechanical_fusion_anvil"),
                        com.github.L_Ender.cataclysm.inventory.WeaponfusionMenu.class, CinematicAnvils::sides));
            }
        });
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("cold_sweat")) {
                AddonScenes.register(SEWING, (pos, state, player, side) -> tableFor(new CartographyStation(pos, side),
                        com.momosoftworks.coldsweat.common.container.SewingContainer.class));
            }
            if (ModList.get().isLoaded("northstar")) {
                AddonScenes.register(ASTRONOMY, (pos, state, player, side) -> tableFor(new CartographyStation(pos, side),
                        com.lightning.northstar.block.tech.astronomy_table.AstronomyTableMenu.class));
            }
            if (ModList.get().isLoaded("irons_spellbooks")) {
                AddonScenes.register(ARCANE, (pos, state, player, side) ->
                        anvilFor(new AnvilStation(pos, side), io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu.class));
            }
            if (ModList.get().isLoaded("cataclysm")) {
                AddonScenes.register(FUSION, (pos, state, player, side) ->
                        anvilFor(new AnvilStation(pos, side), com.github.L_Ender.cataclysm.inventory.WeaponfusionMenu.class));
            }
        });
    }

    /** The cartography table scene (two things on the table, a result, a stamp), looking at another menu with the same slots. */
    private static CartographyStation tableFor(CartographyStation station, Class<? extends AbstractContainerMenu> menu) {
        ((MenuStationAccessor) (Object) station).spellclasses$setMenuType(menu);
        return station;
    }

    /** The vanilla anvil scene, looking at another menu: same slots (two inputs, result), no experience cost, no renaming. */
    private static AnvilStation anvilFor(AnvilStation station, Class<? extends AbstractContainerMenu> menu) {
        ((MenuStationAccessor) (Object) station).spellclasses$setMenuType(menu);
        return station;
    }
}
