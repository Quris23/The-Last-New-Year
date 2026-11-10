package lny.octotilt.mixin;

import java.util.UUID;
import lny.octotilt.OctoTilt;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// aero_player_tilt leans the local player's body by its body tilt, worked out each frame from the Sable deck underfoot; it
// syncs it to the server, which leans the hitbox too, and to the other players. While the tentacle rig leans the player, the
// body tilt is the rig's lean instead, the same rotation the camera gets (applies only when aero_player_tilt is installed)
@Pseudo
@Mixin(targets = "com.mlh.aero_player_tilt.client.tilt.BodyTiltController", remap = false)
public class BodyTiltMixin {
   @Shadow(remap = false)
   @Final
   private static Quaternionf bodyTilt;

   @Inject(
      method = "updateBodyTilt(Lorg/joml/Vector3f;FZLorg/joml/Quaternionf;Ljava/util/UUID;Lcom/mlh/aero_player_tilt/client/tilt/TiltPrediction$Landing;Z)V",
      at = @At("TAIL"),
      remap = false
   )
   private static void lny_octo_tilt$rigLean(
      Vector3f surfaceNormal, float deltaTime, boolean hold, Quaternionf shipRotation, UUID shipId,
      @Coerce Object landing, boolean airborneOverDeck, CallbackInfo ci
   ) {
      if (OctoTilt.engaged()) {
         bodyTilt.set(OctoTilt.current());
      }
   }
}
