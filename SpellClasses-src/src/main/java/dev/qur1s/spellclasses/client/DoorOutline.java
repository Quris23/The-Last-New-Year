package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/**
 * The block selection outline of a door covers both of its halves instead of only the one being looked at. Vanilla draws the
 * outline of a single block position, so a door (two blocks tall) is only half outlined. Drawn with the same line render type,
 * so outline resource packs (rainbow outline etc.) still apply.
 */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class DoorOutline {
    private DoorOutline() {
    }

    @SubscribeEvent
    static void onHighlight(RenderHighlightEvent.Block event) {
        var level = Minecraft.getInstance().level;
        var entity = event.getCamera().getEntity();
        if (level == null || entity == null) return;
        BlockPos pos = event.getTarget().getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock) || !state.hasProperty(DoorBlock.HALF)) return;

        boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
        BlockPos otherPos = lower ? pos.above() : pos.below();
        BlockState other = level.getBlockState(otherPos);
        if (!(other.getBlock() instanceof DoorBlock)) return;

        CollisionContext context = CollisionContext.of(entity);
        VoxelShape shape = Shapes.or(state.getShape(level, pos, context),
                other.getShape(level, otherPos, context).move(0.0, lower ? 1.0 : -1.0, 0.0));

        event.setCanceled(true);
        PoseStack poseStack = event.getPoseStack();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        var camera = event.getCamera().getPosition();
        double ox = pos.getX() - camera.x, oy = pos.getY() - camera.y, oz = pos.getZ() - camera.z;
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            if (length == 0f) return;
            dx /= length;
            dy /= length;
            dz /= length;
            consumer.addVertex(pose, (float) (x1 + ox), (float) (y1 + oy), (float) (z1 + oz))
                    .setColor(0f, 0f, 0f, 0.4f).setNormal(pose, dx, dy, dz);
            consumer.addVertex(pose, (float) (x2 + ox), (float) (y2 + oy), (float) (z2 + oz))
                    .setColor(0f, 0f, 0f, 0.4f).setNormal(pose, dx, dy, dz);
        });
    }
}
