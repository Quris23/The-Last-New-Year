package dev.qur1s.sphereshields.mixin;

import com.anton.shieldgenerators.ShieldGeneratorBlockEntity;
import dev.qur1s.sphereshields.SignalHolder;
import dev.qur1s.sphereshields.SphereGeometryBridge;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two things merged into one mixin because they share state that only a single mixin class can
 * hold onto (a {@code @Unique} field added by one mixin isn't nameable from a sibling mixin on the
 * same target - its real field name is mangled by Mixin to avoid collisions):
 * <p>
 * 1. Tracks the generator's actual redstone signal (0-15) by tapping the same
 * {@code getBestNeighborSignal} read {@code refreshShield} already does, unchanged (no more
 * inverting it to "no redstone = full power" - signal now has a real analog job instead).
 * <p>
 * 2. Replaces the ground (non-Sable) shield shape with a full sphere whose RADIUS scales with that
 * signal: 0 redstone = 0 radius (the vanilla "signal <= 0 -> deactivate" gate already turns the
 * shield off at that point anyway), 15 = the full radius set in the block's own GUI slider (~3
 * blocks of radius per signal step, e.g. signal 15 with a 50-block GUI setting = the full 50).
 * <p>
 * {@code currentBounds} returns {@code ShieldGeometry.Bounds}, a package-private record with no
 * public factory - can't be named in this mixin's own signature at all. Injecting at HEAD with a
 * raw (unparameterized) {@code CallbackInfoReturnable} sidesteps that: Mixin only needs the target
 * method's OWN return slot to be a reference type to accept whatever
 * {@link SphereGeometryBridge#staticSphereBounds} hands back (built via reflection, since we can't
 * reference the real type to construct it directly either - see that class for why).
 * <p>
 * Cancelling at HEAD skips the vanilla method's own radius-change caching entirely, so this keeps
 * its own equivalent cache - without it, the reflection-built mesh (~1500+ triangles) would be
 * rebuilt from scratch every single tick the shield is active.
 */
@Mixin(value = ShieldGeneratorBlockEntity.class, remap = false)
abstract class StaticSphereMixin implements SignalHolder {
    @Unique
    private int sphereshields$lastSignal;
    @Unique
    private double sphereshields$cachedRadius = Double.NaN;
    @Unique
    private Object sphereshields$cachedBounds;

    @Override
    public int sphereshields$getLastSignal() {
        return this.sphereshields$lastSignal;
    }

    @Redirect(
            method = "refreshShield(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBestNeighborSignal(Lnet/minecraft/core/BlockPos;)I")
    )
    private int sphereshields$trackSignal(ServerLevel level, BlockPos pos) {
        int signal = level.getBestNeighborSignal(pos);
        this.sphereshields$lastSignal = signal;
        return signal;
    }

    @Inject(method = "currentBounds", at = @At("HEAD"), cancellable = true)
    private void sphereshields$staticSphereInstead(ServerLevel level, ServerSubLevel subLevel, CallbackInfoReturnable cir) {
        if (subLevel != null) return;

        double configuredRadius = ((ShieldGeneratorBlockEntityAccessor) (Object) this).sphereshields$getGroundShieldRadius();
        double signalFraction = Math.clamp(this.sphereshields$lastSignal / 15.0, 0.0, 1.0);
        double radius = configuredRadius * signalFraction;

        if (sphereshields$cachedBounds == null || Math.abs(sphereshields$cachedRadius - radius) > 1.0E-6) {
            BlockEntity self = (BlockEntity) (Object) this;
            sphereshields$cachedBounds = SphereGeometryBridge.staticSphereBounds(self.getBlockPos(), radius);
            sphereshields$cachedRadius = radius;
        }
        cir.setReturnValue(sphereshields$cachedBounds);
    }

    // lastSignal only ever gets set server-side (refreshShield is server-only), but the color/
    // strength tier logic that reads it (TieredPowerMixin) runs on the client too - has to ride the
    // same NBT sync the mod's own fields already use.
    private static final String LAST_SIGNAL_KEY = "SphereShields_LastSignal";

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void sphereshields$saveSignal(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt(LAST_SIGNAL_KEY, this.sphereshields$lastSignal);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void sphereshields$loadSignal(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.sphereshields$lastSignal = tag.getInt(LAST_SIGNAL_KEY);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void sphereshields$includeSignalInUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putInt(LAST_SIGNAL_KEY, this.sphereshields$lastSignal);
    }
}
