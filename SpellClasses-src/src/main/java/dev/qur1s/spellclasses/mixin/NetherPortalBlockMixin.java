package dev.qur1s.spellclasses.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla Nether portal blocks that already exist no longer teleport anyone. */
@Mixin(value = NetherPortalBlock.class, remap = false)
public abstract class NetherPortalBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void spellclasses$noTeleport(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
