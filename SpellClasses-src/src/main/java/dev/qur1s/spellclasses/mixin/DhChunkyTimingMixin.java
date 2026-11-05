package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;

/**
 * Distant Horizons binds to Chunky when the first chunk loads, and Chunky is not ready that early in a world load: DH then
 * crashes the server with "Chunky is not loaded". Wait with the binding until Chunky answers, and try again on a later chunk.
 */
@Mixin(targets = "com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.AbstractChunkyAccessor", remap = false)
public abstract class DhChunkyTimingMixin {
    @Shadow
    private boolean listenerBound;

    @Inject(method = "tryRunFirstTimeSetup", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$waitForChunky(CallbackInfo ci) {
        if (!listenerBound && !spellclasses$chunkyReady()) ci.cancel();
    }

    private static boolean spellclasses$chunkyReady() {
        try {
            Class.forName("org.popcraft.chunky.ChunkyProvider").getMethod("get").invoke(null);
            return true;
        } catch (InvocationTargetException e) {
            return !(e.getCause() instanceof IllegalStateException);
        } catch (Throwable t) {
            return true;
        }
    }
}
