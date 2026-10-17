package dev.qur1s.atlas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

/** Moves a moderator to a spot picked on the Atlas. */
public final class AtlasTeleport {
    private AtlasTeleport() {
    }

    public static void teleport(ServerPlayer player, String dimension, int x, int z) {
        if (!player.hasPermissions(2)) {
            player.displayClientMessage(Component.translatable("message.atlas.teleport_denied"), true);
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(dimension);
        if (id == null) return;
        ServerLevel level = player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level == null) return;
        if (!level.getWorldBorder().isWithinBounds(x, z)) return;
        level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
        int y;
        if (level.dimensionType().hasCeiling()) {
            y = -1;
            int top = Math.min(level.getMinBuildHeight() + level.dimensionType().logicalHeight() - 2, 120);
            for (int cy = top; cy > level.getMinBuildHeight() + 1; cy--) {
                BlockPos p = new BlockPos(x, cy, z);
                if (level.getBlockState(p.below()).isSolid() && level.getBlockState(p).isAir()
                        && level.getBlockState(p.above()).isAir()) {
                    y = cy;
                    break;
                }
            }
            if (y < 0) {
                player.displayClientMessage(Component.translatable("message.atlas.teleport_no_spot"), true);
                return;
            }
        } else {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        }
        player.teleportTo(level, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
    }
}
