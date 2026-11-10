package lny.cinematicoptions.mixin;

import net.minecraft.client.Options;
import lny.cinematicoptions.LoadingScreenVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The loading screen trailer's volume is saved to and read from options.txt with Minecraft's own options (in NeoForge's part
// of them, which is also the part read again once the mods are loaded)
@Mixin(Options.class)
public class MixinOptionsLoadingScreenVolume {
   @Inject(method = "processOptionsForge", at = @At("TAIL"), remap = false)
   private void lnycinematic$loadingScreenVolume(Options.FieldAccess access, CallbackInfo ci) {
      access.process(LoadingScreenVolume.KEY, LoadingScreenVolume.OPTION);
   }
}
