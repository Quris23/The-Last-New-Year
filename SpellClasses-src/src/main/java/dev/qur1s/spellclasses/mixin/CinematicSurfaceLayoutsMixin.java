package dev.qur1s.spellclasses.mixin;

import dev.dyslexic.cinematic.station.StationKind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Tabletop layouts exist per kind of vanilla station. The addon tables run the cartography scene under the "addon" kind (see
 * CinematicCartographyStationMixin), so for the layouts they are cartography tables.
 */
@Mixin(targets = "dev.dyslexic.cinematic.client.SurfaceLayouts", remap = false)
public abstract class CinematicSurfaceLayoutsMixin {
    @ModifyVariable(method = "index", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
    private static StationKind spellclasses$addonIsCartography(StationKind kind) {
        return kind == StationKind.ADDON ? StationKind.CARTOGRAPHY : kind;
    }

    /** The same for the anvil-like tables and the anvil's "spatial" layouts. */
    @Mixin(targets = "dev.dyslexic.cinematic.client.SpatialLayouts", remap = false)
    public abstract static class Spatial {
        @ModifyVariable(method = "index", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
        private static StationKind spellclasses$addonIsAnvil(StationKind kind) {
            return kind == StationKind.ADDON ? StationKind.ANVIL : kind;
        }
    }
}
