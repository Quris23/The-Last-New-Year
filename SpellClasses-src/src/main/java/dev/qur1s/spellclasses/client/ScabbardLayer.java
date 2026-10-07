package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import dev.qur1s.spellclasses.Scabbard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws the item sitting in the scabbard slot on the wearer's back, running diagonally from the right shoulder
 * down to the left hip, hilt up. The numbers below are the knobs for position, angle and size.
 */
public class ScabbardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    /** Offset from the body pivot (the neck), in blocks: down, and out to the back. */
    private static final float DOWN = 0.40f;
    private static final float BACK = 0.17f;
    /** Extra backwards offset when a chestplate is worn so the item does not sink into it. */
    private static final float ARMOR_EXTRA = 0.06f;
    /**
     * Rotation about the item's own Z axis, degrees, giving the shoulder-to-hip diagonal (right shoulder, hilt up) for
     * each kind of item: flat sprites, upright 3D models, and flat sprites of items with a custom renderer.
     */
    private static final float TILT_FLAT_SPRITE = 0f;
    private static final float TILT_UPRIGHT_MODEL = 45f;
    /** Extra size on top of "same as in hand" (1.0 = exactly as large as when held). */
    private static final float SIZE = 1.0f;
    /**
     * Fine-tuning for items drawn with their hand model (hammers): extra rotation in the back plane, degrees (negative
     * leans the head further towards the left hip), and their size relative to how large they are in the hand.
     */
    private static final float HAND_MODEL_EXTRA_TILT = -22f;
    private static final float HAND_MODEL_SIZE = 0.7f;
    /** Size of upright 3D models (staves) on the back relative to their size in the hand. */
    private static final float UPRIGHT_MODEL_SIZE = 0.6f;
    /** Shift of an upright 3D model along its own long axis, in model units (negative = towards the right shoulder). */
    private static final float UPRIGHT_MODEL_SLIDE = -0.35f;
    /** How far a hand-model item (hammer) is pushed along its own long axis towards the left hip, in model units. */
    private static final float HAND_MODEL_SLIDE = 0.5f;
    /** Where a custom-rendered item's grip sits: out from the neck to the wearer's right, and down from it. */
    private static final float SHOULDER_X = 0.19f;
    private static final float SHOULDER_Y = 0.05f;

    private static final Map<BakedModel, float[]> ROLL_CACHE = new WeakHashMap<>();

    /**
     * {roll degrees about the vertical axis, centre x, centre z (both relative to the model centre)} that make the model
     * thinnest in depth: every vertex is rotated about its own centre in 5 degree steps and the smallest depth wins.
     */
    private static float[] flatRoll(BakedModel model) {
        return ROLL_CACHE.computeIfAbsent(model, m -> {
            List<float[]> points = new ArrayList<>();
            RandomSource random = RandomSource.create(42L);
            for (Direction side : new Direction[]{null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH,
                    Direction.EAST, Direction.WEST}) {
                for (BakedQuad quad : m.getQuads(null, side, random)) {
                    int[] v = quad.getVertices();
                    for (int i = 0; i + 2 < v.length; i += 8) {
                        points.add(new float[]{Float.intBitsToFloat(v[i]), Float.intBitsToFloat(v[i + 2])});
                    }
                }
            }
            if (points.isEmpty()) return new float[]{0f, 0f, 0f};
            float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            for (float[] p : points) {
                minX = Math.min(minX, p[0]);
                maxX = Math.max(maxX, p[0]);
                minZ = Math.min(minZ, p[1]);
                maxZ = Math.max(maxZ, p[1]);
            }
            float cx = (minX + maxX) / 2f, cz = (minZ + maxZ) / 2f;
            float best = 0f, bestDepth = Float.MAX_VALUE;
            for (int deg = 0; deg < 180; deg += 5) {
                double r = Math.toRadians(deg), sin = Math.sin(r), cos = Math.cos(r);
                double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
                for (float[] p : points) {
                    double depth = -(p[0] - cx) * sin + (p[1] - cz) * cos;
                    lo = Math.min(lo, depth);
                    hi = Math.max(hi, depth);
                }
                if (hi - lo < bestDepth - 1e-4) {
                    bestDepth = (float) (hi - lo);
                    best = deg;
                }
            }
            // The model is drawn about its centre (0.5, 0.5) in block units.
            return new float[]{best, cx - 0.5f, cz - 0.5f};
        });
    }

    public ScabbardLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler(Scabbard.SLOT))
                .map(handler -> handler.getStacks().getSlots() > 0 ? handler.getStacks().getStackInSlot(0) : ItemStack.EMPTY)
                .orElse(ItemStack.EMPTY);
        if (stack.isEmpty() || player.isInvisible()) return;
        // Not drawn while it is the item actually held (a sword just drawn leaves the slot, but be safe).
        if (stack == player.getMainHandItem()) return;

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, player.level(), player, player.getId());
        BakedModel fixedModel = model.applyTransform(ItemDisplayContext.FIXED, new PoseStack(), false);
        BakedModel handModel = model.applyTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new PoseStack(), false);
        // Items whose model changes with the view - a flat "_gui" icon for inventory/frame views and a separate 3D
        // "_handled" model in the hand (Iron's weapon models, GeckoLib items) - would show their icon on the back in
        // the FIXED view; they are drawn with the hand model instead.
        boolean handModelItem = fixedModel.isCustomRenderer() || handModel != fixedModel;

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        float back = BACK + (player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? 0f : ARMOR_EXTRA);

        if (fixedModel.isCustomRenderer()) {
            // Items drawn by their own (GeckoLib) renderer - hammers, Helvar's sword - are shown as the real 3D model,
            // exactly as in the hand: the same transforms ItemInHandLayer applies (X -90, Y 180, grip offset), mounted
            // at the right shoulder and turned so the blade, which points forward in the hand, runs down towards the
            // left hip instead (90 degrees about the axis (1, -1, 0)).
            poseStack.translate(-SHOULDER_X, SHOULDER_Y, back);
            poseStack.mulPose(Axis.ZP.rotationDegrees(HAND_MODEL_EXTRA_TILT));
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.70710677f, -0.70710677f, 0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            poseStack.translate(1.0f / 16.0f, 0.125f, -0.625f);
            poseStack.scale(HAND_MODEL_SIZE, HAND_MODEL_SIZE, HAND_MODEL_SIZE);
            itemRenderer.renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
            poseStack.popPose();
            return;
        }

        // Items with a separate hand model are drawn from that model (its own FIXED display, like a staff), not from
        // the icon model the FIXED view would pick.
        BakedModel drawModel = handModelItem ? handModel : model;
        poseStack.translate(0.0, DOWN, back);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        // FIXED display transforms are a fraction of the held ones (vanilla: 0.5 vs 0.85); scale by the model's
        // own ratio so the item is as large on the back as it is in the hand.
        float fixedScale = drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).scale.x();
        float handScale = drawModel.getTransforms().getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).scale.x();
        float ratio = fixedScale > 0.001f && handScale > 0.001f ? handScale / fixedScale : 1.0f;
        float size = ratio * SIZE;
        boolean slideUpright = false;
        float fixedYawOfModel = Math.abs(drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).rotation.y());
        if (Math.abs(fixedYawOfModel - 180f) >= 1f) {
            // Long upright 3D models (staves) are drawn smaller on the back so they do not trail on the ground.
            size *= UPRIGHT_MODEL_SIZE;
            slideUpright = true;
        }
        // Flat sprite items (swords drawn from a texture) are already turned 180 degrees about Y by their FIXED
        // display transform, and their blade already runs along the 45 degree diagonal - so they need no extra
        // tilt. 3D models (staves) stand upright in this view and need the full 45 degree tilt.
        float fixedYaw = Math.abs(drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).rotation.y());
        float tilt = Math.abs(fixedYaw - 180f) < 1f ? TILT_FLAT_SPRITE : TILT_UPRIGHT_MODEL;
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
        poseStack.scale(size, size, size);
        if (slideUpright && !handModelItem) {
            // Pull a staff back along its own long axis (towards the right shoulder) so its head ends at the hip.
            poseStack.translate(0f, UPRIGHT_MODEL_SLIDE, 0f);
        }
        if (handModelItem) {
            // Slide the model along its own long axis (head towards the left hip) so that only part of the handle
            // shows above the shoulder.
            poseStack.translate(0f, HAND_MODEL_SLIDE, 0f);
            // Turn the model about its own long axis until it is as thin as possible front-to-back, so it lies flat
            // against the body instead of sticking out at an angle.
            float[] roll = flatRoll(drawModel);
            poseStack.translate(roll[1], 0f, roll[2]);
            poseStack.mulPose(Axis.YP.rotationDegrees(roll[0]));
            poseStack.translate(-roll[1], 0f, -roll[2]);
            itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY, drawModel);
        } else {
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack,
                    buffer, player.level(), player.getId());
        }
        poseStack.popPose();
    }
}
