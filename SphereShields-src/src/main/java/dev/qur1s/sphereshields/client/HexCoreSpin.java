package dev.qur1s.sphereshields.client;

import java.util.Random;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Draws the Hex Core's inner cube spinning around the block center on all three axes. Every block gets its
 * own spin, derived from its position: a steady speed per axis plus a slow sine wobble, so each axis
 * speeds up, slows down and reverses over time and the cube never settles into a repeating pattern.
 * <p>
 * The redstone signal (0-15) scales the whole spin: 0 = stopped, {@link #REFERENCE_SIGNAL} = the base speed,
 * 15 = about 4x that. The scale is applied to a running phase (not to the clock directly) so changing the
 * signal speeds the cube up or down smoothly instead of snapping it to a new angle.
 */
public final class HexCoreSpin {
    /** The signal at which the cube spins at its base speed (3-4 on the 0-15 scale). */
    private static final float REFERENCE_SIGNAL = 3.5f;

    private static final WeakHashMap<BlockEntity, State> STATES = new WeakHashMap<>();

    private static final class State {
        // per axis i: [3i] steady deg/s, [3i+1] wobble amplitude deg, [3i+2] wobble rad/s; [9+i] wobble phase
        final float[] spin = new float[12];
        float phase;      // accumulated spin time in seconds, advanced at the current speed
        float speed;      // smoothed speed multiplier
        float lastTime = Float.NaN;

        State(long seed) {
            Random rnd = new Random(seed * 0x9E3779B97F4A7C15L);
            for (int i = 0; i < 3; i++) {
                spin[3 * i] = (rnd.nextBoolean() ? 1f : -1f) * (20f + rnd.nextFloat() * 50f);
                spin[3 * i + 1] = 30f + rnd.nextFloat() * 60f;
                spin[3 * i + 2] = 0.4f + rnd.nextFloat() * 0.9f;
                spin[9 + i] = rnd.nextFloat() * (float) (Math.PI * 2);
            }
        }
    }

    private HexCoreSpin() {}

    public static void render(BlockEntity be, int signal, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        if (be.getLevel() == null) return;
        State st = STATES.computeIfAbsent(be, b -> new State(b.getBlockPos().asLong()));

        float now = (be.getLevel().getGameTime() + partialTick) / 20f;
        float dt = Float.isNaN(st.lastTime) ? 0f : now - st.lastTime;
        if (dt < 0f || dt > 0.5f) dt = 0f;          // paused, teleported or just came into view: don't jump
        st.lastTime = now;

        float target = Math.clamp(signal, 0, 15) / REFERENCE_SIGNAL;
        st.speed += (target - st.speed) * (1f - (float) Math.exp(-dt * 3f));
        st.phase += dt * st.speed;

        float[] s = st.spin;
        float[] a = new float[3];
        for (int i = 0; i < 3; i++) {
            a[i] = s[3 * i] * st.phase + s[3 * i + 1] * (float) Math.sin(s[3 * i + 2] * st.phase + s[9 + i]);
        }

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(HexCoreClient.CORE_MODEL);
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.XP.rotationDegrees(a[0]));
        pose.mulPose(Axis.YP.rotationDegrees(a[1]));
        pose.mulPose(Axis.ZP.rotationDegrees(a[2]));
        pose.translate(-0.5, -0.5, -0.5);
        VertexConsumer consumer = buffers.getBuffer(RenderType.cutout());
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                pose.last(), consumer, null, model, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
