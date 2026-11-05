package dev.qur1s.spellclasses.mixin.rig;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import dev.qur1s.spellclasses.client.rig.TentacleRigColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"dev.tentaworks.client.TentacleRenderer"},
   remap = false
)
public class MixinOctoArmsTentacleRenderer {
   @Inject(
      method = {"quad"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void spellclasses$recoloured(
      VertexConsumer consumer,
      Pose pose,
      TextureAtlasSprite sprite,
      int light,
      float nx,
      float ny,
      float nz,
      float uFrom,
      float uTo,
      float vFrom,
      float vTo,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz,
      CallbackInfo ci
   ) {
      if (TentacleRigColors.quad(consumer, pose, sprite, light, nx, ny, nz, uFrom, uTo, vFrom, vTo, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz)) {
         ci.cancel();
      }
   }
}
