package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import com.anton.shieldgenerators.ShieldGeneratorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.qur1s.sphereshields.GlyphFace;
import dev.qur1s.sphereshields.GlyphMeshBuilder;
import dev.qur1s.sphereshields.GlyphRenderTypes;
import dev.qur1s.sphereshields.ShieldGlyphs;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.WeakHashMap;

/**
 * Draws a Dragon Script glyph on every face of the ground dome, on top of the vanilla
 * surface/outline rendering, cross-fading between random glyphs over time (glow in, fade out, a
 * different glyph glows in, ...), each face on its own phase so the whole dome doesn't pulse in
 * sync. Kept as a fully separate draw pass with its own mesh cache (GlyphMeshBuilder mirrors
 * SphereGeometryBridge's math but produces our own {@link GlyphFace} type directly - no reflection
 * needed here since we're not touching shield_generators' internals, only reading its already-public
 * {@code isActive()}/{@code shieldColor()} and our own accessor for ground-shield mode/radius).
 */
@Mixin(value = ShieldGeneratorRenderer.class, remap = false)
abstract class GlyphOverlayRenderMixin {
    @Unique
    private static final WeakHashMap<ShieldGeneratorBlockEntity, CachedGlyphMesh> sphereshields$glyphMeshes = new WeakHashMap<>();

    /** How long one glyph's full glow-in/fade-out cycle takes. */
    @Unique
    private static final double CYCLE_TICKS = 70.0;
    /** How far each face's own vertices are pulled in toward the face centroid before texturing, so the glyph doesn't reach the face's actual edges. */
    @Unique
    private static final double INSET = 0.62;
    @Unique
    private static final int MAX_ALPHA = 150;

    @Unique
    private record CachedGlyphMesh(double radius, List<GlyphFace> faces) {
    }

    @Inject(method = "render(Lcom/anton/shieldgenerators/ShieldGeneratorBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("TAIL"))
    private void sphereshields$renderGlyphs(ShieldGeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (!blockEntity.isActive()) return;
        ShieldGeneratorBlockEntityAccessor accessor = (ShieldGeneratorBlockEntityAccessor) (Object) blockEntity;
        if (!accessor.sphereshields$isGroundShieldMode()) return;

        // The GUI-configured radius is only the ceiling now - the actual sphere shrinks with the
        // generator's live redstone signal (see StaticSphereMixin). Reading it back off the block's
        // own already-synced render bounds keeps the glyphs glued to the real, current sphere
        // instead of a stale/incorrect signal-less radius.
        double radius = blockEntity.getRenderBounds().maxX - 0.5;
        CachedGlyphMesh cached = sphereshields$glyphMeshes.get(blockEntity);
        if (cached == null || Math.abs(cached.radius() - radius) > 1.0E-6) {
            cached = new CachedGlyphMesh(radius, GlyphMeshBuilder.build(radius, 24, 32));
            sphereshields$glyphMeshes.put(blockEntity, cached);
        }

        Level level = blockEntity.getLevel();
        double renderTick = (level == null ? 0L : level.getGameTime()) + partialTick;

        int color = blockEntity.shieldColor();
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        int fullBright = 0xF000F0;

        PoseStack.Pose pose = poseStack.last();
        List<GlyphFace> faces = cached.faces();
        for (int i = 0; i < faces.size(); i++) {
            GlyphFace face = faces.get(i);

            double phase = (i * 0.6180339887) % 1.0;
            double t = renderTick / CYCLE_TICKS + phase;
            long cycleIndex = (long) Math.floor(t);
            double frac = t - cycleIndex;
            float glow = (float) Math.sin(Math.PI * frac);
            if (glow <= 0.001f) continue;

            int glyphIndex = Math.floorMod((int) (cycleIndex * 2654435761L) ^ (face.glyphIndex() * 40503), ShieldGlyphs.COUNT);
            int alpha = Math.round(MAX_ALPHA * glow);

            Vec3 centroid = new Vec3((face.a().x + face.b().x + face.c().x) / 3.0, (face.a().y + face.b().y + face.c().y) / 3.0, (face.a().z + face.b().z + face.c().z) / 3.0);
            Vec3 a = centroid.add(face.a().subtract(centroid).scale(INSET));
            Vec3 b = centroid.add(face.b().subtract(centroid).scale(INSET));
            Vec3 c = centroid.add(face.c().subtract(centroid).scale(INSET));

            RenderType type = GlyphRenderTypes.get(glyphIndex);
            VertexConsumer buffer = bufferSource.getBuffer(type);
            sphereshields$vertex(buffer, pose, a, 0.5f, 0.0f, red, green, blue, alpha, fullBright);
            sphereshields$vertex(buffer, pose, b, 0.0f, 1.0f, red, green, blue, alpha, fullBright);
            sphereshields$vertex(buffer, pose, c, 1.0f, 1.0f, red, green, blue, alpha, fullBright);
        }
    }

    @Unique
    private static void sphereshields$vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 pos, float u, float v, float red, float green, float blue, int alpha, int light) {
        buffer.addVertex(pose, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(red, green, blue, alpha / 255.0f)
                .setUv(u, v)
                .setLight(light);
    }
}
