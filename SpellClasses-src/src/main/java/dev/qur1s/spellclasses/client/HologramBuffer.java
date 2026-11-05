package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

/**
 * Draws the hologram: everything is tinted blue and every block face gets a thin cyan outline. The outlines are made from the quads that
 * go through the consumer (a quad is four vertices), so only the visible faces of the schematic are outlined.
 */
final class HologramBuffer implements SuperRenderTypeBuffer {
    /** Filter strength: the share of the cyan tint mixed into the blocks (0.15 = 15%). */
    private static final float STRENGTH = 0.15F;
    /** The tint of the whole flush (blocks and block entities alike) is the shader colour set by HologramSchematics; the vertices keep their colours. */
    static final float RED = 1.0F - STRENGTH * (1.0F - 0.35F), GREEN = 1.0F - STRENGTH * (1.0F - 0.85F), BLUE = 1.0F;
    private static final int LINE_R = 90, LINE_G = 215, LINE_B = 255, LINE_A = 190;

    private final SuperRenderTypeBuffer base;

    HologramBuffer(SuperRenderTypeBuffer base) {
        this.base = base;
    }

    /** A cyan glow over every face: a quad drawn additively (ONE+ONE blending: the strength is in the vertex colour, not in the alpha), so the filter does not depend on the block's own vertex colours. */
    private static RenderType fillType;
    private static final float[][] UVS = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};

    /** Outline lines: always 0.6 pixel of the screen, whatever the distance. */
    private static final float LINE_WIDTH = 0.6F;
    private static RenderType lineType;

    private static final class Shards extends RenderStateShard {
        private Shards() {
            super("spellclasses_holo", () -> {
            }, () -> {
            });
        }

        /** Like the game's "eyes" type (additive, no light), but pulled a hair towards the viewer by the layering state, the same at any distance. */
        static RenderType fill() {
            return RenderType.create("spellclasses_holo_fill", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEyesShader))
                            .setTextureState(new RenderStateShard.TextureStateShard(net.minecraft.resources.ResourceLocation
                                    .fromNamespaceAndPath("spellclasses", "textures/misc/hologram_fill.png"), false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                            .createCompositeState(false));
        }

        static RenderType lines() {
            return RenderType.create("spellclasses_holo_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 1536,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(LINE_WIDTH)))
                            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false));
        }
    }

    private static RenderType fill() {
        if (fillType == null) fillType = Shards.fill();
        return fillType;
    }

    private static RenderType lines() {
        if (lineType == null) lineType = Shards.lines();
        return lineType;
    }

    private VertexConsumer wrap(VertexConsumer consumer, RenderType type) {
        return new Outliner(consumer, this);
    }

    @Override
    public VertexConsumer getEarlyBuffer(RenderType type) {
        return wrap(base.getEarlyBuffer(type), type);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return wrap(base.getBuffer(type), type);
    }

    @Override
    public VertexConsumer getLateBuffer(RenderType type) {
        return wrap(base.getLateBuffer(type), type);
    }

    @Override
    public void draw() {
        base.draw();
    }

    @Override
    public void draw(RenderType type) {
        base.draw(type);
    }

    private static final class Outliner implements VertexConsumer {
        private final VertexConsumer target;
        private final HologramBuffer owner;
        private final float[] qx = new float[4], qy = new float[4], qz = new float[4];
        private int count;

        Outliner(VertexConsumer target, HologramBuffer owner) {
            this.target = target;
            this.owner = owner;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            qx[count] = x;
            qy[count] = y;
            qz[count] = z;
            if (++count == 4) {
                count = 0;
                outline();
            }
            return target.addVertex(x, y, z);
        }

        /** A second quad over the face with the frame texture, pushed a little towards the viewer (the camera is at the origin). */
        private void outline() {
            float ax = qx[1] - qx[0], ay = qy[1] - qy[0], az = qz[1] - qz[0];
            float bx = qx[2] - qx[0], by = qy[2] - qy[0], bz = qz[2] - qz[0];
            float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1.0E-6F) return;
            nx /= len;
            ny /= len;
            nz /= len;
            float cx = (qx[0] + qx[2]) * 0.5F, cy = (qy[0] + qy[2]) * 0.5F, cz = (qz[0] + qz[2]) * 0.5F;
            float distance = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
            if (nx * cx + ny * cy + nz * cz > 0) {
                nx = -nx;
                ny = -ny;
                nz = -nz;
            }
            float eps = 0.0F; // the depth offset comes from the render type (view-space layering), not from moving the quad
            VertexConsumer edge = owner.base.getLateBuffer(fill());
            for (int i = 0; i < 4; i++) {
                edge.addVertex(qx[i] + nx * eps, qy[i] + ny * eps, qz[i] + nz * eps)
                        .setColor(Math.round(255 * STRENGTH), Math.round(255 * STRENGTH), Math.round(255 * STRENGTH), 255).setUv(UVS[i][0], UVS[i][1])
                        .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                        .setLight(0xF000F0).setNormal(nx, ny, nz);
            }
            VertexConsumer lineBuffer = owner.base.getLateBuffer(lines());
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) & 3;
                float dx = qx[j] - qx[i], dy = qy[j] - qy[i], dz = qz[j] - qz[i];
                float l = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (l < 1.0E-4F) continue;
                dx /= l;
                dy /= l;
                dz /= l;
                lineBuffer.addVertex(qx[i] + nx * eps, qy[i] + ny * eps, qz[i] + nz * eps).setColor(LINE_R, LINE_G, LINE_B, LINE_A).setNormal(dx, dy, dz);
                lineBuffer.addVertex(qx[j] + nx * eps, qy[j] + ny * eps, qz[j] + nz * eps).setColor(LINE_R, LINE_G, LINE_B, LINE_A).setNormal(dx, dy, dz);
            }
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            // Create passes the colour bytes as signed ints: they must be read as 0..255 first
            return target.setColor(r, g, b, a);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return target.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return target.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return target.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return target.setNormal(x, y, z);
        }
    }
}
