package dev.qur1s.spellclasses.mixin.rig;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.model.geom.ModelPart.Cube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinChromewareLimbCutouts {
   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.body.ClippedLimbRenderer"},
      remap = false
   )
   public static class Clipped {
      private static boolean spellclasses$finished(VertexConsumer buffer) {
         if (buffer instanceof BufferBuilder builder && !((IMixinBufferBuilder)builder).spellclasses$isBuilding()) {
            return true;
         }

         return false;
      }

      @Inject(
         method = {"render"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$skipFinishedRender(
         Cube cube, float length, Pose pose, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci
      ) {
         if (spellclasses$finished(buffer)) {
            ci.cancel();
         }
      }

      @Inject(
         method = {"stretched"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$skipFinishedStretched(
         Cube cube, float scale, Pose pose, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci
      ) {
         if (spellclasses$finished(buffer)) {
            ci.cancel();
         }
      }
   }

   @Pseudo
   @Mixin(
      targets = {"com.chromeware.client.monowire.MonowireWristCutout"},
      remap = false
   )
   public static class Wrist {
      private static boolean spellclasses$finished(VertexConsumer buffer) {
         if (buffer instanceof BufferBuilder builder && !((IMixinBufferBuilder)builder).spellclasses$isBuilding()) {
            return true;
         }

         return false;
      }

      @Inject(
         method = {"render"},
         at = {@At("HEAD")},
         cancellable = true,
         remap = false
      )
      private static void spellclasses$skipFinished(
         Cube cube, boolean left, Pose pose, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci
      ) {
         if (spellclasses$finished(buffer)) {
            ci.cancel();
         }
      }
   }
}
