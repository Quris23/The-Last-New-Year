package lny.octotilt.mixin.rig;

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
public class ImplantColorsMixin {
   private static final List<String> octotilt$PARTS = List.of("brass", "copper", "steel", "dark", "lights", "wire");

   @Inject(
      method = {"valid"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void octotilt$tentacleRig(String key, CallbackInfoReturnable<Boolean> cir) {
      for (String prefix : new String[]{"tentacle_rig:"}) {
         if (key.startsWith(prefix) && octotilt$PARTS.contains(key.substring(prefix.length()))) {
            cir.setReturnValue(true);
            return;
         }
      }
   }
}
