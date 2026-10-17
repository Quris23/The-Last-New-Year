package dev.qur1s.atlas.mixin;

import dev.qur1s.atlas.EyeSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Remembers what a searching eye found while the Atlas runs it (every such eye asks the level for the nearest structure). */
@Mixin(ServerLevel.class)
public abstract class ServerLevelEyeMixin {
    @Inject(method = "findNearestMapStructure", at = @At("RETURN"))
    private void atlas$remember(TagKey<Structure> tag, BlockPos from, int radius, boolean skipKnown,
                                CallbackInfoReturnable<BlockPos> cir) {
        if (EyeSearch.capturing && cir.getReturnValue() != null) EyeSearch.captured = cir.getReturnValue();
    }
}
