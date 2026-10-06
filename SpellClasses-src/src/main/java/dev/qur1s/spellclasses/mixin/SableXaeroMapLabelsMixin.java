package dev.qur1s.spellclasses.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Sable Sublevels on Xaero's Maps" draws ships as their real blocks and nothing else - no name, no icon. This adds
 * a name label over every ship that is big enough on the Xaero's World Map. It runs inside the mod's own render pass
 * (same transform: map blocks are the unit), just before that pass closes, and sizes the text back to screen pixels.
 */
@Mixin(targets = "com.sablexaeromaps.SublevelMapRenderer", remap = false)
public abstract class SableXaeroMapLabelsMixin {
    /** Ships smaller than this many screen pixels get no label (keeps debris and far-away zoom levels clean). */
    private static final double MIN_LABEL_EDGE_PX = 7.0;
    private static final float TEXT_SCALE = 0.85f;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            shift = At.Shift.BEFORE), require = 0)
    private static void spellclasses$labels(String surface, PoseStack matrixStack,
                                            MultiBufferSource.BufferSource buffers, int cameraBlockX, int cameraBlockZ,
                                            float zOffset, int caveLayer, CallbackInfo ci) {
        if (!"worldmap".equals(surface)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        var container = SubLevelContainer.getContainer(level);
        if (container == null) {
            return;
        }
        Matrix4f matrix = matrixStack.last().pose();
        float pixelsPerBlock = matrix.getScale(new Vector3f()).x();
        if (pixelsPerBlock <= 0.0f) {
            return;
        }
        Font font = mc.font;
        for (ClientSubLevel subLevel : container.getAllSubLevels()) {
            if (subLevel == null || subLevel.isRemoved()) {
                continue;
            }
            BoundingBox3ic bounds = subLevel.getPlot().getBoundingBox();
            int edgeBlocks = Math.max(bounds.maxX() - bounds.minX(), bounds.maxZ() - bounds.minZ()) + 1;
            if (edgeBlocks * pixelsPerBlock < MIN_LABEL_EDGE_PX) {
                continue;
            }
            Pose3dc pose = subLevel.renderPose();
            String name = subLevel.getName();
            Component label = Component.literal(name == null || name.isBlank() ? "Корабль" : name);

            float scale = TEXT_SCALE / pixelsPerBlock;
            float halfWidth = font.width(label) / 2.0f;
            matrixStack.pushPose();
            // above the ship's middle, a little to the north so the text does not cover the blocks
            matrixStack.translate((float) pose.position().x(),
                    (float) pose.position().z() - (edgeBlocks / 2.0f) - 6.0f * scale, 0.5f);
            matrixStack.scale(scale, scale, 1.0f);
            font.drawInBatch(label, -halfWidth, 0.0f, 0xFFFFFFFF, true, matrixStack.last().pose(), buffers,
                    Font.DisplayMode.NORMAL, 0x60000000, 0xF000F0);
            matrixStack.popPose();
        }
        buffers.endBatch();
    }
}
