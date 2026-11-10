package lny.octotilt.mixin.rig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// In first person Octo Arms leaves out every piece of the rig inside a sphere around the camera: 0.6 block plus the piece's
// thickness for tubes and hubs, 0.5 for decals and pads. With the rig on the back that sphere took the tentacles off well
// short of the body, so they ended in the air as open stumps. The sphere is made 0.4 block smaller (never under 0.15), which
// still keeps the parts right at the eyes out of the view
@Pseudo
@Mixin(targets = "dev.tentaworks.client.TentacleRenderer$Space", remap = false)
public class TentacleCameraCutMixin {
   @Unique
   private static final float octotilt$SMALLER = 0.4F;
   @Unique
   private static final float octotilt$LEAST = 0.15F;

   @ModifyVariable(method = "cut", at = @At("HEAD"), argsOnly = true, ordinal = 4, remap = false, require = 0)
   private float octotilt$smallerCut(float radius) {
      return Math.max(octotilt$LEAST, radius - octotilt$SMALLER);
   }
}
