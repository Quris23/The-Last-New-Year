package dev.qur1s.atlas;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Colours a chunk the way a map would at its finest scale (one block per pixel): the top block's map colour, shaded by
 * the slope towards the north neighbour on land and by depth in water. Done here, from the real world, so an atlas
 * shows the terrain itself and not a copy of a map's picture.
 */
public final class AtlasTerrain {
    private AtlasTerrain() {
    }

    /**
     * The top of the column (exclusive): the highest block that is not air, found by looking through the chunk's
     * sections. The saved heightmap is not used, since it can lag behind what stands there (snow layers and the like).
     */
    private static int topOf(LevelChunk chunk, int lx, int lz) {
        var sections = chunk.getSections();
        int min = chunk.getMinBuildHeight();
        // the saved heightmap is a good place to start looking, but it can lag behind (snow layers and the like), so
        // look a few blocks above it too instead of going through the whole column
        int start = Math.min(chunk.getMaxBuildHeight() - 1, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz) + 8);
        for (int y = start; y >= min; y--) {
            int si = (y - min) >> 4;
            var section = sections[si];
            if (section == null || section.hasOnlyAir()) {
                y = min + (si << 4);          // the whole section is empty: skip to the one below
                continue;
            }
            if (!section.getBlockState(lx, y & 15, lz).isAir()) return y + 1;
        }
        return min;
    }

    /** Where the Nether's map is taken: the first open air at this height or above, then down to the floor. */
    private static final int CAVE_LEVEL = 70;

    /** The height of the first block (exclusive top) below the ceiling, looking down from {@link #CAVE_LEVEL}. */
    private static int caveTop(ServerLevel level, LevelChunk chunk, int lx, int lz, BlockPos.MutableBlockPos pos) {
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        int limit = level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 2;
        int y = CAVE_LEVEL;
        // inside rock at that height: climb to the nearest open air above it
        while (y < limit && !chunk.getBlockState(pos.set(x0 + lx, y, z0 + lz)).isAir()
                && chunk.getBlockState(pos).getFluidState().isEmpty()) {
            y++;
        }
        if (y >= limit) return level.getMinBuildHeight();
        return y + 1;
    }

    /** One byte (map colour id) per block of the chunk, x fastest; 0 where nothing could be coloured. */
    public static byte[] sample(ServerLevel level, LevelChunk chunk) {
        byte[] out = new byte[256];
        int x0 = chunk.getPos().getMinBlockX(), z0 = chunk.getPos().getMinBlockZ();
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean ceiling = level.dimensionType().hasCeiling();
        int[] tops = new int[256];
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                tops[lx + lz * 16] = ceiling ? caveTop(level, chunk, lx, lz, pos) : topOf(chunk, lx, lz);
            }
        }
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int top = tops[lx + lz * 16];
                int y = top - 1;
                MapColor color = MapColor.NONE;
                BlockState state = null;
                while (y >= minY) {
                    pos.set(x0 + lx, y, z0 + lz);
                    state = chunk.getBlockState(pos);
                    color = state.getMapColor(level, pos);
                    if (color != MapColor.NONE) break;
                    y--;
                }
                if (color == MapColor.NONE || state == null) continue;

                MapColor.Brightness brightness;
                if (state.getFluidState().is(FluidTags.WATER)) {
                    int depth = 0;
                    int yy = y;
                    while (yy > minY && depth < 40) {
                        pos.set(x0 + lx, yy - 1, z0 + lz);
                        if (!chunk.getBlockState(pos).getFluidState().is(FluidTags.WATER)) break;
                        yy--;
                        depth++;
                    }
                    double d = depth * 0.1 + ((lx + lz) & 1) * 0.2;
                    brightness = d < 0.5 ? MapColor.Brightness.HIGH : d > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                } else {
                    int northY = lz > 0 ? tops[lx + (lz - 1) * 16] - 1 : y;
                    if (ceiling) {
                        // cave floors are rough: shade only real steps, and a little by height, so it does not look like static
                        int step = y - northY;
                        brightness = step >= 3 ? MapColor.Brightness.HIGH : step <= -3 ? MapColor.Brightness.LOW
                                : y >= CAVE_LEVEL - 6 ? MapColor.Brightness.HIGH : y <= 40 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                    } else {
                        double d = (y - northY) + (((lx + lz) & 1) - 0.5) * 0.4;
                        brightness = d > 0.6 ? MapColor.Brightness.HIGH : d < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                    }
                }
                out[lx + lz * 16] = (byte) color.getPackedId(brightness);
            }
        }
        return out;
    }
}
