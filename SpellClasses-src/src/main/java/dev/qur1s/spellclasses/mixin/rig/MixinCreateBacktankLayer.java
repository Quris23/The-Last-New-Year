package dev.qur1s.spellclasses.mixin.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import dev.qur1s.spellclasses.client.rig.TentacleRigImplant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"com.simibubi.create.content.equipment.armor.BacktankArmorLayer"},
   remap = false
)
public class MixinCreateBacktankLayer {
   @Inject(
      method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void spellclasses$noImplantTank(
      PoseStack poseStack,
      MultiBufferSource buffers,
      int light,
      LivingEntity entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo ci
   ) {
      if (TentacleRigImplant.isImplanted(entity)) {
         ci.cancel();
      }
   }
}
