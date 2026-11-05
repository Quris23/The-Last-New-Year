package dev.qur1s.spellclasses.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Northstar's astronomy table has a board tilted by 22.5 degrees, sloping down towards the side the block faces. The
 * cartography scene lays things flat on a horizontal top; for this table they are laid on the board instead.
 */
public final class AstronomyTilt {
    public static final float ANGLE = (float) Math.toRadians(22.5);
    public static final double SLOPE = Math.tan(ANGLE);
    /** The middle of the board is this far below the block's top (the board turns about a point 14/16 up). */
    public static final double DROP = 0.067;

    private AstronomyTilt() {
    }

    /** The facing of the astronomy table whose board holds {@code near} (a point on or just above it), else null. */
    public static Direction facing(Vec3 near) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        BlockState state = level.getBlockState(BlockPos.containing(near.x, near.y - 0.2, near.z));
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!id.getNamespace().equals("northstar") || !id.getPath().equals("astronomy_table")) return null;
        return state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(null);
    }

    /** Height of the board at {@code point}, which is horizontally as laid out on the flat top at {@code top}. */
    public static double boardY(Vec3 top, Direction facing, Vec3 point) {
        double along = (point.x - top.x) * facing.getStepX() + (point.z - top.z) * facing.getStepZ();
        return top.y - DROP - along * SLOPE;
    }

    /** The orientation of something lying flat, laid on the board: its normal leans towards the low edge. */
    public static Quaternionf lay(Quaternionf flat, Direction facing) {
        // up x facing is the axis that turns the up vector towards the facing
        double ax = facing.getStepZ();
        double az = -facing.getStepX();
        return new Quaternionf().rotationAxis(ANGLE, (float) ax, 0f, (float) az).mul(flat);
    }

    public static Vec3 normal(Direction facing) {
        return new Vec3(facing.getStepX() * Math.sin(ANGLE), Math.cos(ANGLE), facing.getStepZ() * Math.sin(ANGLE));
    }
}
