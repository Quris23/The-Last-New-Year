package dev.qur1s.spellclasses.mixin.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import dev.qur1s.spellclasses.client.rig.RigShaderCompat;
import dev.qur1s.spellclasses.client.rig.TentacleRigColors;
import dev.qur1s.spellclasses.client.rig.TentacleRigImplant;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"dev.tentaworks.client.TentacleLayer"},
   remap = false
)
public class MixinOctoArmsTentacleLayer {
   private static boolean spellclasses$implanted;

   @ModifyVariable(
      method = {"lambda$render$0"},
      at = @At("STORE"),
      ordinal = 0,
      remap = false
   )
   private Matrix4f spellclasses$shadowPass_tank(Matrix4f tank) {
      Matrix4f shadowView = RigShaderCompat.shadowPassView();
      return shadowView != null ? new Matrix4f(shadowView).invert().mul(tank) : tank;
   }

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V"},
      at = {@At("HEAD")},
      remap = false
   )
   private void spellclasses$begin_layer(
      PoseStack poseStack,
      MultiBufferSource buffers,
      int light,
      AbstractClientPlayer player,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo ci
   ) {
      spellclasses$implanted = TentacleRigImplant.isImplanted(player);
      TentacleRigColors.begin(buffers, player);
   }

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V"},
      at = {@At("RETURN")},
      remap = false
   )
   private void spellclasses$end_layer(
      PoseStack poseStack,
      MultiBufferSource buffers,
      int light,
      AbstractClientPlayer player,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo ci
   ) {
      TentacleRigColors.end();
   }

   @Inject(
      method = {"onFirstPerson"},
      at = {@At("HEAD")},
      remap = false
   )
   private static void spellclasses$implanted_firstPerson(RenderLevelStageEvent event, CallbackInfo ci) {
      spellclasses$implanted = TentacleRigImplant.isImplanted(Minecraft.getInstance().player);
   }

   @ModifyVariable(
      method = {"lambda$onFirstPerson$1"},
      at = @At("STORE"),
      remap = false
   )
   private static BufferSource spellclasses$begin_firstPerson(BufferSource buffers) {
      TentacleRigColors.begin(buffers, Minecraft.getInstance().player);
      return buffers;
   }

   @Inject(
      method = {"onFirstPerson"},
      at = {@At("RETURN")},
      remap = false
   )
   private static void spellclasses$end_firstPerson(RenderLevelStageEvent event, CallbackInfo ci) {
      TentacleRigColors.end();
   }

   @Redirect(
      method = {"lambda$render$0"},
      at = @At(
         value = "INVOKE",
         target = "Ldev/tentaworks/client/TentacleRenderer;toTankSpace(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      ),
      remap = false
   )
   private void spellclasses$tankSpace_layer(PoseStack poseStack) {
      TentacleRigImplant.toTankSpace(poseStack, spellclasses$implanted);
   }

   @Redirect(
      method = {"lambda$onFirstPerson$1"},
      at = @At(
         value = "INVOKE",
         target = "Ldev/tentaworks/client/TentacleRenderer;toTankSpace(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      ),
      remap = false
   )
   private static void spellclasses$tankSpace_firstPerson(PoseStack poseStack) {
      TentacleRigImplant.toTankSpace(poseStack, spellclasses$implanted);
   }
}
