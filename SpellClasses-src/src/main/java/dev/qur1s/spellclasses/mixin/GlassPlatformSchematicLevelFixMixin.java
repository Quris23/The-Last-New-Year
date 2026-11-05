package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GLASS casts the level of an unloaded platform block entity to ClientLevel. Litematica builds its schematic world out
 * of a different Level class (WorldSchematic), so using Litematica's easy place with a GLASS platform crashed the game
 * (ClassCastException in PlatformBlockEntity$ClientHooks.unloaded). Only real client levels need the hook.
 */
@Mixin(targets = "dev.imb11.platform.PlatformBlockEntity$ClientHooks", remap = false)
public abstract class GlassPlatformSchematicLevelFixMixin {
    @Inject(method = "unloaded", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$skipNonClientLevels(BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity.getLevel() instanceof ClientLevel)) {
            ci.cancel();
        }
    }
}
