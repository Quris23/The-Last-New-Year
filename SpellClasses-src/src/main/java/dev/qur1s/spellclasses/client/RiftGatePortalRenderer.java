package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import uk.co.iceconchy.aerowarptics.gate.RiftGateBlockEntity;
import uk.co.iceconchy.aerowarptics.gate.RiftGateShape;
import uk.co.iceconchy.aerowarptics.gate.RiftGateState;

/**
 * Draws the Rift Gate's portal as one swirl over the whole opening instead of one tile per block.
 *
 * The "better nether portal" texture pack makes its vortex out of OptiFine connected-texture pieces that only fit a
 * 2x3 portal. Here the six pieces are pre-assembled into one 32x48 animated sprite ({@code rift_vortex}), and the
 * opening is covered by a single one, stretched to the opening's size and shape. The per-block portal models
 * are empty in the resource overrides, so nothing else is drawn.
 */
public class RiftGatePortalRenderer implements BlockEntityRenderer<RiftGateBlockEntity> {
    private static final ResourceLocation VORTEX = ResourceLocation.fromNamespaceAndPath("aerowarptics", "block/rift_vortex");
    private static final int FULL_BRIGHT = 0xF000F0;
    /** The vortex sprite is 2 blocks wide and 3 tall. */
    private static final double UNIT_W = 2.0;
    private static final double UNIT_H = 3.0;

    @Override
    public void render(RiftGateBlockEntity gate, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int light, int overlay) {
        RiftGateShape shape = gate.shape();
        RiftGateState state = gate.state();
        if (shape == null || state == null || !state.hasAperture()) {
            return;
        }
        int alpha = switch (state) {
            case OPEN -> 255;
            case DIALLING -> 200;
            default -> 150;
        };
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(VORTEX);
        // A fully open gate is solid: the translucent "emissive" type does not write depth, so anything drawn
        // after it (water, other translucent blocks behind the gate) shows straight through.
        RenderType type = state == RiftGateState.OPEN
                ? RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)
                : RenderType.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer out = buffers.getBuffer(type);
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();

        BlockPos origin = gate.getBlockPos();
        boolean spanX = shape.span() == Direction.Axis.X;
        int width = shape.width();
        int height = shape.height();
        // one swirl over the whole opening, whatever its size
        int cols = 1;
        int rows = 1;
        double unitW = (double) width / cols;
        double unitH = (double) height / rows;

        // the plane is the middle of the opening's row of blocks along the normal axis
        double plane = (spanX ? shape.minZ() + 0.5 - origin.getZ() : shape.minX() + 0.5 - origin.getX());
        double spanOrigin = spanX ? shape.minX() - origin.getX() : shape.minZ() - origin.getZ();
        double yOrigin = shape.minY() - origin.getY();

        for (int up = 0; up < height; up++) {
            for (int across = 0; across < width; across++) {
                if (!shape.openAt(across, up)) {
                    continue;
                }
                // cut the cell where it crosses a vortex-unit boundary
                double[] xs = cuts(across, unitW);
                double[] ys = cuts(up, unitH);
                for (int xi = 0; xi + 1 < xs.length; xi++) {
                    for (int yi = 0; yi + 1 < ys.length; yi++) {
                        double a0 = xs[xi], a1 = xs[xi + 1];
                        double u0 = ys[yi], u1 = ys[yi + 1];
                        double midA = (a0 + a1) / 2;
                        double midU = (u0 + u1) / 2;
                        int unitCol = Math.min(cols - 1, (int) (midA / unitW));
                        int unitRow = Math.min(rows - 1, (int) (midU / unitH));
                        // position inside the unit, 0..1; rows count upwards, the sprite counts downwards
                        float fx0 = (float) ((a0 - unitCol * unitW) / unitW);
                        float fx1 = (float) ((a1 - unitCol * unitW) / unitW);
                        float fy0 = 1f - (float) ((u0 - unitRow * unitH) / unitH);
                        float fy1 = 1f - (float) ((u1 - unitRow * unitH) / unitH);
                        float su0 = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * fx0;
                        float su1 = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * fx1;
                        float sv0 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * fy0;
                        float sv1 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * fy1;
                        float y0 = (float) (yOrigin + u0);
                        float y1 = (float) (yOrigin + u1);
                        float s0 = (float) (spanOrigin + a0);
                        float s1 = (float) (spanOrigin + a1);
                        float p = (float) plane;
                        if (spanX) {
                            vertex(out, matrix, pose, s0, y1, p, su0, sv1, alpha, 0, 0, 1);
                            vertex(out, matrix, pose, s0, y0, p, su0, sv0, alpha, 0, 0, 1);
                            vertex(out, matrix, pose, s1, y0, p, su1, sv0, alpha, 0, 0, 1);
                            vertex(out, matrix, pose, s1, y1, p, su1, sv1, alpha, 0, 0, 1);
                        } else {
                            vertex(out, matrix, pose, p, y1, s0, su0, sv1, alpha, 1, 0, 0);
                            vertex(out, matrix, pose, p, y0, s0, su0, sv0, alpha, 1, 0, 0);
                            vertex(out, matrix, pose, p, y0, s1, su1, sv0, alpha, 1, 0, 0);
                            vertex(out, matrix, pose, p, y1, s1, su1, sv1, alpha, 1, 0, 0);
                        }
                    }
                }
            }
        }
    }

    /** Cell [index, index+1] split wherever it crosses a multiple of {@code unit}. */
    private static double[] cuts(int index, double unit) {
        double lo = index;
        double hi = index + 1;
        java.util.ArrayList<Double> list = new java.util.ArrayList<>();
        list.add(lo);
        for (double edge = Math.ceil(lo / unit - 1e-9) * unit; edge < hi - 1e-9; edge += unit) {
            if (edge > lo + 1e-9) {
                list.add(edge);
            }
        }
        list.add(hi);
        double[] result = new double[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, PoseStack.Pose pose, float x, float y, float z,
                               float u, float v, int alpha, float nx, float ny, float nz) {
        out.addVertex(matrix, x, y, z).setColor(255, 255, 255, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(RiftGateBlockEntity gate) {
        RiftGateShape shape = gate.shape();
        if (shape == null) {
            return new AABB(gate.getBlockPos());
        }
        return new AABB(shape.minX(), shape.minY(), shape.minZ(), shape.maxX() + 1, shape.maxY() + 1, shape.maxZ() + 1)
                .inflate(1.0);
    }

    @Override
    public boolean shouldRenderOffScreen(RiftGateBlockEntity gate) {
        return false;
    }
}
