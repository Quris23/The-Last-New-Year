package lny.stockticker.client;

import com.chromeware.client.cyberware.CyberwareRig;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
import lny.stockticker.StockTickerImplant;
import lny.stockticker.StockTickerMod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * The portable stock ticker drawn on Chromeware's mantis rig in place of the blade (StockTickerArm): fixed to the blade
 * bone, so it swings out of the opened wrist on the strut and lies along the forearm as the blade would, the screen to the
 * front of the arm (up to the eyes while the arm is raised) and the antenna toward the hand. Drawn from a copy of the item
 * model with a texture of its own (lny_stock_ticker:item/stock_ticker_arm), so the one on the arm can be repainted apart from
 * the item and takes the player's implant colours (StockTickerColors); without it the item itself is drawn.
 */
public final class StockTickerArmRenderer {
   // Size of the item model against the rig: its 14 px body about as long as the forearm
   private static final float SCALE = 0.4F;
   // Its size folded in the wrist, before the strut swings out: small enough to stay under the skin
   private static final float FOLDED = 0.2F;
   // Across the blade bone, the middle of the blade's width (mirrored on the left rig)
   private static final float ACROSS = 0.048F;
   // How far the strut swings out when deployed (52 degrees in the mantis animations)
   private static final float SWING = 52.0F * Mth.DEG_TO_RAD;
   // Item model's up (the screen) to the front of the forearm, its -z (the antenna) along the blade toward the hand
   private static final Quaternionf TURN = new Quaternionf(0.0F, Mth.SQRT_OF_TWO / 2.0F, -Mth.SQRT_OF_TWO / 2.0F, 0.0F);
   private static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(StockTickerMod.id("item/stock_ticker_arm"));
   private static ItemStack ticker;

   private StockTickerArmRenderer() {
   }

   public static void registerModel(ModelEvent.RegisterAdditional event) {
      event.register(MODEL);
   }

   /** The bones of the blade, drawn by the ticker instead */
   public static boolean blade(String bone) {
      return bone.endsWith("_blade") || bone.endsWith("_tip") || bone.endsWith("_extension");
   }

   /** Same placement as CyberwareRig.render, then the blade bone's pose */
   public static void render(
      CyberwareRig rig,
      Player player,
      HumanoidArm side,
      boolean left,
      boolean slim,
      ModelPart arm,
      float center,
      PoseStack stack,
      Function<String, CyberwareRig.Transform> animation,
      int light,
      float along,
      MultiBufferSource buffers
   ) {
      String prefix = side == HumanoidArm.LEFT ? "mantis_left_" : "mantis_right_";
      Map<String, Matrix4f> poses = rig.poses(animation);
      Matrix4f blade = poses.get(prefix + "blade");
      if (blade == null) {
         return;
      }
      float swing = Mth.clamp(Math.abs(animation.apply(prefix + "strut").rotation().z()) / SWING, 0.0F, 1.0F);
      float scale = SCALE * Mth.lerp(swing * swing * (3.0F - 2.0F * swing), FOLDED, 1.0F);
      stack.pushPose();
      arm.translateAndRotate(stack);
      stack.translate(center, 0.0F, 0.0F);
      stack.scale((left ? -1.0F : 1.0F) * (slim ? 0.75F : 1.0F), 1.0F, 1.0F);
      stack.translate(0.0F, along, 0.0F);
      stack.mulPose(blade);
      // The body's back end at the blade root
      stack.translate(side == HumanoidArm.LEFT ? -ACROSS : ACROSS, 7.0F / 16.0F * scale, 0.0F);
      stack.mulPose(TURN);
      stack.scale(scale, scale, scale);
      // The middle of the body (8, 1, 8 px) onto the origin, the item renderer shifting the model by half a block
      stack.translate(0.0F, 7.0F / 16.0F, 0.0F);
      BakedModel model = model();
      if (model == null) {
         Minecraft.getInstance().getItemRenderer().renderStatic(stack(), ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, stack, buffers, player.level(), 0);
      } else {
         StockTickerColors.draw(model, stack, buffers, StockTickerColors.texture(player), light);
      }
      stack.popPose();
   }

   /** The ticker's own model, null while it is missing */
   public static BakedModel model() {
      Minecraft mc = Minecraft.getInstance();
      BakedModel model = mc.getModelManager().getModel(MODEL);
      return model == mc.getModelManager().getMissingModel() ? null : model;
   }

   private static ItemStack stack() {
      if (ticker == null) {
         ticker = new ItemStack(BuiltInRegistries.ITEM.get(StockTickerImplant.TICKER));
      }
      return ticker;
   }
}
