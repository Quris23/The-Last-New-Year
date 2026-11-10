package lny.octotilt.mixin.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import lny.octotilt.rig.RigShaderCompat;
import lny.octotilt.rig.RigColors;
import lny.octotilt.rig.RigImplant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.world.phys.Vec3;
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
// Octo Arms' tentacles on a rig implanted through Spell Classes (on the back, no tank) in the implant colours chosen in
// Chromeware, in third and first person alike; the tank's matrix is taken out of Iris's shadow view for the reach
public class TentacleLayerMixin {
   private static boolean octotilt$implanted;
   private static Vec3 octotilt$camera = Vec3.ZERO;
   private static float octotilt$partialTick;

   @ModifyVariable(
      method = {"lambda$drawOnBody$0"},
      require = 0,
      at = @At("STORE"),
      ordinal = 0,
      remap = false
   )
   private static Matrix4f octotilt$shadowPass_tank(Matrix4f tank) {
      Matrix4f shadowView = RigShaderCompat.shadowPassView();
      return shadowView != null ? new Matrix4f(shadowView).invert().mul(tank) : tank;
   }

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V"},
      at = {@At("HEAD")},
      remap = false
   )
   private void octotilt$begin_layer(
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
      octotilt$implanted = RigImplant.isImplanted(player);
      RigColors.begin(buffers, player);
   }

   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V"},
      at = {@At("RETURN")},
      remap = false
   )
   private void octotilt$end_layer(
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
      RigColors.end();
   }

   @Inject(
      method = {"onFirstPerson"},
      at = {@At("HEAD")},
      remap = false
   )
   private static void octotilt$implanted_firstPerson(RenderLevelStageEvent event, CallbackInfo ci) {
      octotilt$camera = event.getCamera().getPosition();
      octotilt$partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
      octotilt$implanted = RigImplant.isImplanted(Minecraft.getInstance().player);
   }

   @ModifyVariable(
      method = {"lambda$onFirstPerson$1", "lambda$onFirstPerson$2"},
      require = 0,
      at = @At("STORE"),
      remap = false
   )
   private static BufferSource octotilt$begin_firstPerson(BufferSource buffers) {
      RigColors.begin(buffers, Minecraft.getInstance().player);
      return buffers;
   }

   @Inject(
      method = {"onFirstPerson"},
      at = {@At("RETURN")},
      remap = false
   )
   private static void octotilt$end_firstPerson(RenderLevelStageEvent event, CallbackInfo ci) {
      RigColors.end();
   }

   @Redirect(
      method = {"lambda$drawOnBody$0"},
      require = 0,
      at = @At(
         value = "INVOKE",
         target = "Ldev/tentaworks/client/TentacleRenderer;toTankSpace(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      ),
      remap = false
   )
   private static void octotilt$tankSpace_layer(PoseStack poseStack) {
      RigImplant.toTankSpace(poseStack, octotilt$implanted);
   }

   @Redirect(
      method = {"lambda$onFirstPerson$1", "lambda$onFirstPerson$2"},
      require = 0,
      at = @At(
         value = "INVOKE",
         target = "Ldev/tentaworks/client/TentacleRenderer;toTankSpace(Lcom/mojang/blaze3d/vertex/PoseStack;)V"
      ),
      remap = false
   )
   private static void octotilt$tankSpace_firstPerson(PoseStack poseStack) {
      RigImplant.firstPerson(poseStack, Minecraft.getInstance().player, octotilt$camera, octotilt$partialTick);
      RigImplant.toTankSpace(poseStack, octotilt$implanted);
   }
}
