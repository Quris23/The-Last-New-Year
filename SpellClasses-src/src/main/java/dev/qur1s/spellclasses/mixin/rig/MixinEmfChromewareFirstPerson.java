package dev.qur1s.spellclasses.mixin.rig;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"traben.entity_model_features.models.parts.EMFModelPartRoot"},
   remap = false
)
public class MixinEmfChromewareFirstPerson {
   private static Method spellclasses$applies;
   private static boolean spellclasses$noChromeware;

   @Inject(
      method = {"animate"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void spellclasses$skipInImplantPass(CallbackInfo ci) {
      if (spellclasses$implantPass()) {
         ci.cancel();
      }
   }

   private static boolean spellclasses$implantPass() {
      AbstractClientPlayer player = Minecraft.getInstance().player;
      if (!spellclasses$noChromeware && player != null) {
         try {
            if (spellclasses$applies == null) {
               spellclasses$applies = Class.forName(
                     "com.chromeware.client.cyberware.ArmFirstPerson", false, MixinEmfChromewareFirstPerson.class.getClassLoader()
                  )
                  .getMethod("applies", AbstractClientPlayer.class);
            }

            return (Boolean)spellclasses$applies.invoke(null, player);
         } catch (LinkageError | ReflectiveOperationException var2) {
            spellclasses$noChromeware = true;
            return false;
         }
      } else {
         return false;
      }
   }
}
