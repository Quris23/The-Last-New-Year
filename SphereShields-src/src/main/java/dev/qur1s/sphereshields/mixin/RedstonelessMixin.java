package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The generator reads the strongest neighboring redstone signal once per tick and treats 0 as
 * "off" - no redstone, no shield, and the signal strength (0-15) also scales how much heat the
 * shield can reach. Redirects that one read to always report 15 (max signal), so the generator
 * runs at full strength on fuel/energy alone with no redstone hookup needed at all.
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
abstract class RedstonelessMixin {
    @Redirect(
            method = "refreshShield(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBestNeighborSignal(Lnet/minecraft/core/BlockPos;)I")
    )
    private int sphereshields$alwaysMaxSignal(ServerLevel level, BlockPos pos) {
        return 15;
    }
}
