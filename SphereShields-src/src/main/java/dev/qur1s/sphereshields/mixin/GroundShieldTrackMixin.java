package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.AllowedPlayersHolder;
import dev.qur1s.sphereshields.GroundShieldTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Feeds {@link GroundShieldTracker} from the one tick-driven method that always runs regardless of
 * shield state ({@code tickFuel}), rather than {@code refreshShield} (whose signature drags in
 * Sable's ServerSubLevel type, which this mod doesn't otherwise need to depend on).
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
abstract class GroundShieldTrackMixin {
    @Inject(method = "tickFuel(ZDI)V", at = @At("TAIL"))
    private void sphereshields$trackGroundShield(boolean shouldRun, double surfaceArea, int redstoneSignal, CallbackInfo ci) {
        BlockEntity asBlockEntity = (BlockEntity) (Object) this;
        ShieldGeneratorBlockEntityAccessor self = (ShieldGeneratorBlockEntityAccessor) (Object) this;
        Level level = asBlockEntity.getLevel();
        if (level == null) return;
        BlockPos pos = asBlockEntity.getBlockPos();
        if (self.sphereshields$isActive() && self.sphereshields$isGroundShieldMode()) {
            double effectiveRadius = self.sphereshields$getGroundShieldRadius() * Math.clamp(redstoneSignal / 15.0, 0.0, 1.0);
            AllowedPlayersHolder accessControl = (AllowedPlayersHolder) (Object) this;
            GroundShieldTracker.update(level.dimension(), pos, effectiveRadius, accessControl.sphereshields$getAllowedPlayers());
        } else {
            GroundShieldTracker.remove(level.dimension(), pos);
        }
    }
}
