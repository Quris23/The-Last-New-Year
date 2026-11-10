package lny.cinematicoptions.mixin;

import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import lny.cinematicoptions.LoadingScreenVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The loading screen trailer's volume slider goes last among the sound categories (in the free half of their last row)
@Mixin(SoundOptionsScreen.class)
public class MixinSoundOptionsScreen {
   @Inject(method = "getAllSoundOptionsExceptMaster", at = @At("RETURN"), cancellable = true)
   private void lnycinematic$loadingScreenVolume(CallbackInfoReturnable<OptionInstance<?>[]> cir) {
      OptionInstance<?>[] options = Arrays.copyOf(cir.getReturnValue(), cir.getReturnValue().length + 1);
      options[options.length - 1] = LoadingScreenVolume.OPTION;
      cir.setReturnValue(options);
   }
}
