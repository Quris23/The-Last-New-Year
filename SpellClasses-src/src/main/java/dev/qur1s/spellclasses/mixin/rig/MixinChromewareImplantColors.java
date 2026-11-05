package dev.qur1s.spellclasses.mixin.rig;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
   targets = {"com.chromeware.cyberware.ImplantColors"},
   remap = false
)
public class MixinChromewareImplantColors {
   private static final List<String> spellclasses$PARTS = List.of("brass", "copper", "steel", "dark", "lights", "wire");

   @Inject(
      method = {"valid"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void spellclasses$tentacleRig(String key, CallbackInfoReturnable<Boolean> cir) {
      if (key.startsWith("tentacle_rig:") && spellclasses$PARTS.contains(key.substring("tentacle_rig:".length()))) {
         cir.setReturnValue(true);
      }
   }
}
