package lny.octotilt.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lny.octotilt.ServerLagGrace;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Create: Octo Arms stows a tentacle rig that moved faster than its top speed for over 10 ticks, measured by how far the
// player really got each tick. After a server stall the queued movement of the whole stall lands in one tick, and the rig
// folds mid-climb with nothing wrong; right after a stall (ServerLagGrace) the check counts nothing (applies only when Octo
// Arms is installed)
@Pseudo
@Mixin(targets = {"dev.tentaworks.rig.RigServer"}, remap = false)
public class RigServerMixin {
   @ModifyExpressionValue(
      method = {"tick"},
      at = @At(value = "FIELD", target = "Ldev/tentaworks/rig/RigServer$Wearer;overSpeed:I", opcode = Opcodes.GETFIELD),
      remap = false
   )
   private static int lny_octo_tilt$stallGrace(int overSpeed) {
      return ServerLagGrace.active() ? 0 : overSpeed;
   }
}
