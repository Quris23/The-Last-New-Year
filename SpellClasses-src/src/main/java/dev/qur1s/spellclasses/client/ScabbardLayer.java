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
    private static final float UPRIGHT_MODEL_SIZE = 0.8f;
    /**
     * Shift of an upright 3D model along its own long axis, in model units (positive = towards its head). The staff is
     * turned head-up, so its head sits at the right shoulder and the butt points to the left hip.
     */
    private static final float UPRIGHT_MODEL_SLIDE = 0.35f;
    /** Extra shift along the axis for the Graybeard staff (Посох старца), towards its head. */
    private static final float GRAYBEARD_EXTRA_SLIDE = 0.15f;
    /** How far a hand-model item (hammer) is pushed along its own long axis towards the left hip, in model units. */
    private static final float HAND_MODEL_SLIDE = 0.5f;
    /** Where a custom-rendered item's grip sits: out from the neck to the wearer's right, and down from it. */
    private static final float SHOULDER_X = 0.19f;
    private static final float SHOULDER_Y = 0.05f;
    /** Automatic layout: longest side in blocks, share of the hand size, where the shoulder end sits (body space, x right = negative), gap to the back. */
    private static final float AUTO_MAX_LENGTH = 1.9f;
    private static final float AUTO_HAND_FRACTION = 0.8f;
    private static final float AUTO_ANCHOR_X = -0.5f;
    private static final float AUTO_ANCHOR_Y = -0.25f;
    private static final float AUTO_GAP = 0.02f;
    /** Staves already laid out by hand and approved - they keep the old placement instead of the automatic one. */
    private static final java.util.Set<String> HAND_TUNED_STAVES = java.util.Set.of(
            "irons_spellbooks:graybeard_staff", "irons_spellbooks:blood_staff", "irons_spellbooks:dev_staff",
            "irons_spellbooks:staff_of_the_nines", "irons_spellbooks:artificer_cane", "irons_spellbooks:lightning_rod",
            "cataclysm_spellbooks:bloom_stone_staff", "cataclysm_spellbooks:coral_staff",
            "cataclysm_spellbooks:fake_wudjets_staff", "cataclysm_spellbooks:void_staff", "wind_spellbooks:wind_staff");

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

    /**
     * Per-item placement overrides, read live from {@code config/spellclasses_scabbard_tuning.json} (created with defaults on
     * first use, reloaded about once a second when the file changes - no restart needed). Keyed by item id. Fields:
     * roll = turn about the blade's own axis (degrees), size = scale vs. the held item, back = distance from the back
     * (negative = closer), tilt = extra rotation in the back plane (degrees), shoulderX / shoulderY = where the grip sits.
     */
    private static final class Tuning {
        float roll = 135f;
        float size = 1.0f;
        float back = -0.3f;
        float tilt = 0f;
        float shoulderX = SHOULDER_X;
        float shoulderY = -0.75f;
        /** Upright / hand-model items only (staves, greatswords): extra turn about the item's own axis, extra shift along it
         *  (positive = towards the head) and extra rotation in the back plane (180 = turn it end for end). */
        float rollAdd = 0f;
        float slide = 0f;
        float flip = 0f;
        /** Extra drop down the wearer's body in blocks (hand-model items only; positive = lower). */
        float down = 0f;
        /** Extra shift towards the wearer's right side, in blocks (hand-model items only; negative = left). */
        float right = 0f;
    }

    private static final java.nio.file.Path TUNING_FILE =
            net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("spellclasses_scabbard_tuning.json");
    private static java.util.Map<String, Tuning> tuning = new java.util.HashMap<>();
    private static long tuningCheckedAt;
    private static long tuningModified = -1;

    private static Tuning tuningFor(String itemId) {
        long now = System.currentTimeMillis();
        if (now - tuningCheckedAt > 1000) {
            tuningCheckedAt = now;
            try {
                if (!java.nio.file.Files.exists(TUNING_FILE)) {
                    java.util.Map<String, Tuning> defaults = new java.util.LinkedHashMap<>();
                    defaults.put("block_factorys_bosses:knight_sword", new Tuning());
                    java.nio.file.Files.writeString(TUNING_FILE,
                            new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(defaults));
                }
                long modified = java.nio.file.Files.getLastModifiedTime(TUNING_FILE).toMillis();
                if (modified != tuningModified) {
                    tuningModified = modified;
                    java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, Tuning>>() {}.getType();
                    java.util.Map<String, Tuning> read = new com.google.gson.Gson().fromJson(java.nio.file.Files.readString(TUNING_FILE), type);
                    if (read != null) tuning = read;
                }
            } catch (Exception ignored) {
            }
        }
        return tuning.get(itemId);
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

        Tuning tune = tuningFor(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        boolean helvar = tune != null;
        if (fixedModel.isCustomRenderer()) {
            // Items drawn by their own (GeckoLib) renderer - hammers, Helvar's sword - are shown as the real 3D model,
            // exactly as in the hand: the same transforms ItemInHandLayer applies (X -90, Y 180, grip offset), mounted
            // at the right shoulder and turned so the blade, which points forward in the hand, runs down towards the
            // left hip instead (90 degrees about the axis (1, -1, 0)).
            poseStack.translate(helvar ? -tune.shoulderX : -SHOULDER_X, helvar ? tune.shoulderY : SHOULDER_Y,
                    helvar ? back + tune.back : back);
            poseStack.mulPose(Axis.ZP.rotationDegrees(helvar ? tune.tilt : HAND_MODEL_EXTRA_TILT));
            poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), 0.70710677f, -0.70710677f, 0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            poseStack.translate(1.0f / 16.0f, 0.125f, -0.625f);
            if (helvar) poseStack.mulPose(Axis.YP.rotationDegrees(tune.roll));
            float handSize = helvar ? tune.size : HAND_MODEL_SIZE;
            poseStack.scale(handSize, handSize, handSize);
            itemRenderer.renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, player.level(), player.getId());
            poseStack.popPose();
            return;
        }

        // Items with a separate hand model are drawn from that model (its own FIXED display, like a staff), not from
        // the icon model the FIXED view would pick.
        BakedModel drawModel = handModelItem ? handModel : model;
        // Everything that has not been tuned by hand and is not a flat sprite is laid out automatically from its geometry.
        boolean flatSprite = Math.abs(Math.abs(drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).rotation.y()) - 180f) < 1f;
        String fullId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!helvar && !flatSprite && !HAND_TUNED_STAVES.contains(fullId)) {
            String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            boolean staffLike = path.matches(".*(staff|rod|wand|cane|scepter|spear|trident|pike|lance|halberd|glaive|harpoon).*");
            ScabbardAuto.Fit fit = ScabbardAuto.fitFor(drawModel, staffLike);
            if (fit != null) {
                float finalLength = Math.min(AUTO_MAX_LENGTH, AUTO_HAND_FRACTION * fit.handLength());
                float scale = finalLength / fit.length();
                // The shoulder end sits at the anchor; the item runs from there down the diagonal towards the left hip.
                float cx = AUTO_ANCHOR_X + 0.70710678f * finalLength / 2f;
                float cy = AUTO_ANCHOR_Y + 0.70710678f * finalLength / 2f;
                poseStack.translate(cx, cy, back + fit.thickness() * scale / 2f + AUTO_GAP);
                poseStack.mulPose(fit.rotation());
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-fit.center().x(), -fit.center().y(), -fit.center().z());
                itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight,
                        OverlayTexture.NO_OVERLAY, drawModel);
                poseStack.popPose();
                return;
            }
        }
        poseStack.translate(helvar ? -tune.right : 0f, DOWN + (helvar ? tune.down : 0f), helvar ? back + tune.back : back);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        // FIXED display transforms are a fraction of the held ones (vanilla: 0.5 vs 0.85); scale by the model's
        // own ratio so the item is as large on the back as it is in the hand.
        float fixedScale = drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).scale.x();
        float handScale = drawModel.getTransforms().getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).scale.x();
        float ratio = fixedScale > 0.001f && handScale > 0.001f ? handScale / fixedScale : 1.0f;
        float size = ratio * SIZE;
        boolean slideUpright = false;
        if (helvar) size = ratio * tune.size;
        float fixedYawOfModel = Math.abs(drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).rotation.y());
        if (!helvar && Math.abs(fixedYawOfModel - 180f) >= 1f) {
            // Long upright 3D models (staves) are drawn smaller on the back so they do not trail on the ground.
            size *= UPRIGHT_MODEL_SIZE;
            slideUpright = true;
        }
        // Flat sprite items (swords drawn from a texture) are already turned 180 degrees about Y by their FIXED
        // display transform, and their blade already runs along the 45 degree diagonal - so they need no extra
        // tilt. 3D models (staves) stand upright in this view and need the full 45 degree tilt.
        float fixedYaw = Math.abs(drawModel.getTransforms().getTransform(ItemDisplayContext.FIXED).rotation.y());
        float tilt = Math.abs(fixedYaw - 180f) < 1f ? TILT_FLAT_SPRITE : TILT_UPRIGHT_MODEL + 180f;
        if (helvar) tilt += tune.flip;
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
        poseStack.scale(size, size, size);
        if (slideUpright && !handModelItem) {
            // Pull a staff back along its own long axis (towards the right shoulder) so its head ends at the hip.
            boolean graybeard = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("graybeard_staff");
            poseStack.translate(0f, UPRIGHT_MODEL_SLIDE + (graybeard ? GRAYBEARD_EXTRA_SLIDE : 0f), 0f);
        }
        if (handModelItem) {
            // Slide the model along its own long axis (head towards the left hip) so that only part of the handle
            // shows above the shoulder.
            poseStack.translate(0f, HAND_MODEL_SLIDE + (helvar ? tune.slide : 0f), 0f);
            // Turn the model about its own long axis until it is as thin as possible front-to-back, so it lies flat
            // against the body instead of sticking out at an angle.
            float[] roll = flatRoll(drawModel).clone(); // the cached array must not be modified
            if (helvar) roll[0] += tune.rollAdd;
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
