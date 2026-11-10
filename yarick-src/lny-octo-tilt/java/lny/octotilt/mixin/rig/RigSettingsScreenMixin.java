package lny.octotilt.mixin.rig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(
   targets = {"dev.tentaworks.client.RigSettingsScreen"},
   remap = false
)
public class RigSettingsScreenMixin {
   @ModifyConstant(
      method = {"renderWindow"},
      constant = {@Constant(
         intValue = 5841956
      )},
      remap = false
   )
   private int octotilt$titleColor(int color) {
      return 16574602;
   }

   @ModifyConstant(
      method = {"renderWindow"},
      constant = {@Constant(
         intValue = 5726074
      )},
      remap = false
   )
   private int octotilt$hintColor(int color) {
      return 12105912;
   }
}
