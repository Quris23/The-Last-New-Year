package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import org.lwjgl.opengl.GL11;

/**
 * Hemodynamics draws blood on a body part with the vanilla "view offset z" layering (the view is scaled by 0.99975, so the
 * blood sits a hair in front of the skin) and, for the skin atlas, a depth test that passes only where the depth is exactly
 * equal. Other layers on the same cubes (Fresh Animations' extra geometry, the animated eyes, 3D skin layers) made that
 * flicker, so outside the first-person hands the blood is pulled closer (0.9995) and the depth test accepts "equal or nearer".
 * The first-person hands keep the original behavior: their blood is drawn on the arm cubes while Chromeware shows its own
 * cyber arm, and "equal or nearer" made the blood spill past the arm.
 */
public final class BloodLayering {
    private static final float SCALE = 0.9995F;
    private static final float VANILLA_SCALE = 0.99975586F;

    /** True while the first-person hands are being rendered (set by GameRendererHandFlagMixin). */
    public static boolean handsActive;

    /** Replaces the vanilla view-offset layering. */
    public static final RenderStateShard.LayeringStateShard NEAR = new RenderStateShard.LayeringStateShard(
            "spellclasses_blood_near_layering",
            () -> {
                float scale = handsActive ? VANILLA_SCALE : SCALE;
                var stack = RenderSystem.getModelViewStack();
                stack.pushMatrix();
                stack.scale(scale, scale, scale);
                RenderSystem.applyModelViewMatrix();
            },
            () -> {
                RenderSystem.getModelViewStack().popMatrix();
                RenderSystem.applyModelViewMatrix();
            });

    /** Added to layers that had no layering at all: nothing in the first-person hands, a small pull elsewhere. */
    public static final RenderStateShard.LayeringStateShard NEAR_EXACT = new RenderStateShard.LayeringStateShard(
            "spellclasses_blood_near_exact_layering",
            () -> {
                var stack = RenderSystem.getModelViewStack();
                stack.pushMatrix();
                if (!handsActive) stack.scale(SCALE, SCALE, SCALE);
                RenderSystem.applyModelViewMatrix();
            },
            () -> {
                RenderSystem.getModelViewStack().popMatrix();
                RenderSystem.applyModelViewMatrix();
            });

    /** "Equal" in the first-person hands (as before), "equal or nearer" elsewhere. */
    public static final RenderStateShard.DepthTestStateShard ADAPTIVE = new RenderStateShard.DepthTestStateShard(
            "spellclasses_blood_adaptive_depth", GL11.GL_LEQUAL) {
        @Override
        public void setupRenderState() {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(handsActive ? GL11.GL_EQUAL : GL11.GL_LEQUAL);
        }

        @Override
        public void clearRenderState() {
            RenderSystem.disableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
        }
    };

    private BloodLayering() {
    }
}
