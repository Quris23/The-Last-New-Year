package dev.qur1s.spellclasses.mixin.rig;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Method;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"punchy.client.render.PunchyArmRenderer"},
   remap = false
)
public class MixinPunchyChromewareArms {
   private static Method spellclasses$ownsArm;
   private static boolean spellclasses$noChromeware;

   @Inject(
      method = {"renderArm"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void spellclasses$skipImplantArm(
      PlayerModel<?> model,
      AbstractClientPlayer player,
      HumanoidArm arm,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      ResourceLocation texture,
      boolean slim,
      float partialTicks,
      CallbackInfo ci
   ) {
      if (spellclasses$implantOwns(player, arm)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderArmArmor"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void spellclasses$skipImplantSleeve(
      PlayerModel<?> model,
      AbstractClientPlayer player,
      HumanoidArm arm,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      boolean slim,
      float partialTicks,
      CallbackInfo ci
   ) {
      if (spellclasses$implantOwns(player, arm)) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderItemInHand"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void spellclasses$skipImplantItem(
      ItemInHandRenderer handRenderer,
      PlayerModel<?> model,
      LocalPlayer player,
      HumanoidArm arm,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      Matrix4f rootWorld,
      ModelPart geoArm,
      ModelPart geoItem,
      ModelPart geoGrip,
      float partialTicks,
      CallbackInfo ci
   ) {
      if (spellclasses$implantOwns(player, arm)) {
         ci.cancel();
      }
   }

   private static boolean spellclasses$implantOwns(AbstractClientPlayer player, HumanoidArm arm) {
      if (spellclasses$noChromeware) {
         return false;
      } else {
         try {
            if (spellclasses$ownsArm == null) {
               spellclasses$ownsArm = Class.forName("com.chromeware.client.cyberware.ArmFirstPerson", false, MixinPunchyChromewareArms.class.getClassLoader())
                  .getMethod("ownsArm", AbstractClientPlayer.class, HumanoidArm.class);
            }

            return (Boolean)spellclasses$ownsArm.invoke(null, player, arm);
         } catch (LinkageError | ReflectiveOperationException var3) {
            spellclasses$noChromeware = true;
            return false;
         }
      }
   }
}
