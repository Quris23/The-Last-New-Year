package lny.stockticker.mixin;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.mantis.MantisState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import lny.stockticker.client.StockTickerArmRenderer;
import lny.stockticker.StockTickerArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The portable stock ticker sliding out of a wrist (StockTickerArm) on Chromeware's mantis blade rig, client only
public class TickerArmMixins {
   // The wrist counts as fitted with blades, deployed while the ticker is out: Chromeware then animates it, opens the
   // skin and draws it, in first and third person. On the client only; what the blades do in play goes by the server.
   @Pseudo
   @Mixin(targets = "com.chromeware.mantis.MantisImplant", remap = false)
   public static class Hardware {
      @Inject(method = "hasHardware", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$ticker(Player player, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
         if (!cir.getReturnValueZ() && player.level().isClientSide() && StockTickerArm.shown(player, side)) {
            cir.setReturnValue(true);
         }
      }

      @Inject(method = "state", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$state(Player player, HumanoidArm side, CallbackInfoReturnable<MantisState> cir) {
         if (player.level().isClientSide() && StockTickerArm.shown(player, side)) {
            cir.setReturnValue(new MantisState(StockTickerArm.out(player, side), MantisState.IDLE, cir.getReturnValue().sequence()));
         }
      }
   }

   // Other hardware of that arm, gone back in before the ticker came out (StockTickerDeploy), is not drawn over it
   @Pseudo
   @Mixin(targets = "com.chromeware.launcher.ProjectileLauncher", remap = false)
   public static class Launcher {
      @Inject(
         method = "active(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/HumanoidArm;)Z",
         at = @At("RETURN"),
         cancellable = true,
         remap = false
      )
      private static void lny_stock_ticker$ticker(Player player, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
         if (cir.getReturnValueZ() && player.level().isClientSide() && StockTickerArm.shown(player, side)) {
            cir.setReturnValue(false);
         }
      }
   }

   @Pseudo
   @Mixin(targets = "com.chromeware.monowire.MonowireImplant", remap = false)
   public static class Monowire {
      @Inject(method = "hasHardware", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$ticker(Player player, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
         if (cir.getReturnValueZ() && player.level().isClientSide() && StockTickerArm.shown(player, side)) {
            cir.setReturnValue(false);
         }
      }
   }

   @Pseudo
   @Mixin(targets = "com.chromeware.extendo.ExtendoGrip", remap = false)
   public static class Extendo {
      @Inject(method = "active", at = @At("RETURN"), cancellable = true, remap = false)
      private static void lny_stock_ticker$ticker(Player player, HumanoidArm side, CallbackInfoReturnable<Boolean> cir) {
         if (cir.getReturnValueZ() && player.level().isClientSide() && StockTickerArm.shown(player, side)) {
            cir.setReturnValue(false);
         }
      }
   }

   // The blade, its tip and extension give way to the ticker's model
   @Pseudo
   @Mixin(targets = "com.chromeware.client.mantis.MantisRenderer", remap = false)
   public static class Blade {
      @WrapOperation(
         method = "drawArm",
         at = @At(
            value = "INVOKE",
            target = "Lcom/chromeware/client/cyberware/CyberwareRig;render(ZZLnet/minecraft/client/model/geom/ModelPart;FLcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILjava/util/function/Predicate;F)Ljava/util/Map;",
            remap = false
         ),
         remap = false
      )
      private static Map<String, Vec3> lny_stock_ticker$ticker(
         CyberwareRig rig,
         boolean left,
         boolean slim,
         ModelPart arm,
         float center,
         PoseStack stack,
         Function<String, CyberwareRig.Transform> animation,
         VertexConsumer buffer,
         int light,
         int overlay,
         Predicate<String> draw,
         float along,
         Operation<Map<String, Vec3>> original,
         AbstractClientPlayer player,
         HumanoidArm side,
         ModelPart armPart,
         PoseStack poseStack,
         MultiBufferSource buffers,
         int packedLight
      ) {
         if (!StockTickerArm.shown(player, side)) {
            return original.call(rig, left, slim, arm, center, stack, animation, buffer, light, overlay, draw, along);
         }
         Map<String, Vec3> result = original.call(
            rig, left, slim, arm, center, stack, animation, buffer, light, overlay, draw.and(bone -> !StockTickerArmRenderer.blade(bone)), along
         );
         StockTickerArmRenderer.render(rig, player, side, left, slim, arm, center, stack, animation, light, along, buffers);
         return result;
      }
   }
}
