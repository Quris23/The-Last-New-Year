package dev.qur1s.spellclasses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dyslexic.cinematic.client.Scene;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Northstar's astronomy table menu learns its block entity on the client from a block position that the opening call writes
 * into the packet. Cinematic Interactions opens menus without any extra data, so the position is taken from the table the
 * scene is at (or the one under the crosshair) instead of failing.
 */
@Mixin(targets = "com.lightning.northstar.block.tech.astronomy_table.AstronomyTableMenu", remap = false)
public abstract class AstronomyTableClientMenuMixin {
    @WrapOperation(method = "createOnClient(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lcom/lightning/northstar/block/tech/astronomy_table/AstronomyTableBlockEntity;",
            remap = false, require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;readBlockPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos spellclasses$positionWithoutData(RegistryFriendlyByteBuf buffer, Operation<BlockPos> original) {
        if (buffer != null) return original.call(buffer);
        if (Scene.station != null) return Scene.station.pos;
        HitResult hit = Minecraft.getInstance().hitResult;
        return hit instanceof BlockHitResult block ? block.getBlockPos() : BlockPos.ZERO;
    }
}
