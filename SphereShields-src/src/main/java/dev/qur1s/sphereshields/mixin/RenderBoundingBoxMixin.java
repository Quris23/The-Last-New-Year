package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import com.anton.shieldgenerators.ShieldGeneratorRenderer;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * shield_generators never overrides NeoForge's {@code IBlockEntityRendererExtension
 * .getRenderBoundingBox}, so the renderer's visibility for frustum-culling purposes defaults to a
 * plain 1x1x1 box at the generator - even though the dome it draws extends tens of blocks past
 * that. Backing away and looking at the near edge of the dome (while the generator block itself
 * drifts out of the view frustum) makes the whole thing vanish, since the game skips calling
 * render() at all once it decides the "block" is off-screen. This was already present in the
 * un-modified mod (confirmed: no override exists at all in the decompiled class).
 * <p>
 * Adding a concrete override here (rather than injecting into one - there's nothing to inject into,
 * the class never implemented this method, it was only ever using the interface's default) reuses
 * the block entity's own already-correct {@code getWorldRenderBounds()}.
 * <p>
 * That alone wasn't enough in practice (a large translucent AABB vs. frustum-plane test can still
 * miss it at some camera angles/positions - confirmed by testing, not just theory), so this also
 * skips frustum culling for this renderer entirely via {@code shouldRenderOffScreen}, and raises
 * the flat view-distance cutoff past the largest configurable ground dome radius (maxGroundShieldRadius
 * defaults to 50, configurable up to 256) plus some margin - the default 64 would otherwise still
 * cut the shield off if you're near a big dome's edge but far from the generator itself.
 */
@Mixin(value = ShieldGeneratorRenderer.class, remap = false)
abstract class RenderBoundingBoxMixin {
    public AABB getRenderBoundingBox(ShieldGeneratorBlockEntity blockEntity) {
        return blockEntity.getWorldRenderBounds();
    }

    public boolean shouldRenderOffScreen(ShieldGeneratorBlockEntity blockEntity) {
        return true;
    }

    public int getViewDistance() {
        return 320;
    }
}
