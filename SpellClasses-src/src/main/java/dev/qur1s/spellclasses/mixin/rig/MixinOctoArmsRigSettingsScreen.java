package dev.qur1s.spellclasses.mixin.rig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(
   targets = {"dev.tentaworks.client.RigSettingsScreen"},
   remap = false
)
public class MixinOctoArmsRigSettingsScreen {
   @ModifyConstant(
      method = {"renderWindow"},
      constant = {@Constant(
         intValue = 5841956
      )},
      remap = false
   )
   private int spellclasses$titleColor(int color) {
      return 16574602;
   }

   @ModifyConstant(
      method = {"renderWindow"},
      constant = {@Constant(
         intValue = 5726074
      )},
      remap = false
   )
   private int spellclasses$hintColor(int color) {
      return 12105912;
   }
}
