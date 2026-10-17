package dev.qur1s.spellclasses.mixin;

import java.util.List;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Warp Drive (Artifacts): the glowing overlay on the belt used to flash on one random frame in about 1 tick out of 10, which read as
 * flicker. Now the overlay is always drawn and walks through its frames in order, a new one every 4 ticks - a steady animation.
 */
@Mixin(targets = "artifacts.client.item.renderer.WarpDriveRenderer", remap = false)
public abstract class WarpDriveSmoothMixin {
    /** the "1 chance in 10" bound (nextInt(10) % 10 != 0 skips the draw): with 1 it always draws */
    @ModifyConstant(method = "render", constant = @Constant(intValue = 10), require = 0)
    private int spellclasses$alwaysDraw(int original) {
        return 1;
    }

    /** the random overlay frame becomes the next frame of a slow cycle */
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"), require = 0)
    private Object spellclasses$cycleFrames(List<?> frames, int randomIndex) {
        var level = Minecraft.getInstance().level;
        long time = level == null ? 0 : level.getGameTime();
        return frames.get((int) ((time / 4) % frames.size()));
    }
}
