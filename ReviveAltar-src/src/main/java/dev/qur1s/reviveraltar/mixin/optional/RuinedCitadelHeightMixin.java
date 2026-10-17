package dev.qur1s.reviveraltar.mixin.optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Cataclysm's Ruined Citadel hardcodes its generation Y to a literal 53 in Java (tuned for
 * the End's floating islands), completely ignoring its own "start_height" JSON field. On a
 * dimension with solid ground all the way down (e.g. the Moon), that buries the whole
 * structure underground. This redirects that fixed Y to the dimension's actual surface
 * height at that position instead. A soft/optional target - does nothing if Cataclysm isn't
 * installed, and harmless (same behavior as before) in the End, where Y=53 already sits at
 * the real surface.
 */
@Mixin(targets = "com.github.L_Ender.cataclysm.structures.RuinedCitadelStructure", remap = false)
public class RuinedCitadelHeightMixin {

    @Redirect(method = "generatePieces", at = @At(value = "NEW", target = "Lnet/minecraft/core/BlockPos;(III)V"))
    private static BlockPos reviveraltar$useRealSurfaceHeight(int x, int y, int z,
            StructurePiecesBuilder builder, Structure.GenerationContext context) {
        int surfaceY = context.chunkGenerator().getFirstFreeHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        return new BlockPos(x, surfaceY, z);
    }
}
