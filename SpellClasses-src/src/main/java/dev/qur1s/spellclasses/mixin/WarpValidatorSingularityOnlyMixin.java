package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.CrossFix;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.co.iceconchy.aerowarptics.anchor.WarpAnchor;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveTier;
import uk.co.iceconchy.aerowarptics.warp.WarpFailure;
import uk.co.iceconchy.aerowarptics.warp.WarpValidator;

/**
 * Cross-dimension support is registered globally ({@code allowCrossDimensionWarp}), but only the
 * Singularity (and Creative) drive may actually target anchors in other dimensions - the lower tiers keep
 * getting DIMENSION_UNSUPPORTED at target-selection time.
 */
@Mixin(value = WarpValidator.class, remap = false)
public abstract class WarpValidatorSingularityOnlyMixin {
    private static final ThreadLocal<Boolean> SPELLCLASSES$TIER_OK = ThreadLocal.withInitial(() -> false);

    @Inject(method = "validateDestination", at = @At("HEAD"))
    private static void spellclasses$captureTier(Airship airship, WarpAnchor anchor, RiftDriveTier tier, double charge,
                                                 CallbackInfoReturnable<WarpFailure> cir) {
        SPELLCLASSES$TIER_OK.set(tier == RiftDriveTier.SINGULARITY || tier.creative());
    }

    @ModifyArg(method = "validateDestination",
            at = @At(value = "INVOKE", target = "Luk/co/iceconchy/aerowarptics/warp/WarpRules;checkDestination(ZZZDDDLuk/co/iceconchy/aerowarptics/warp/WarpCost$Formula;)Luk/co/iceconchy/aerowarptics/warp/WarpFailure;"),
            index = 2)
    private static boolean spellclasses$singularityOnly(boolean supported) {
        return supported && SPELLCLASSES$TIER_OK.get();
    }

    @Inject(method = "validateDestination", at = @At("RETURN"))
    private static void spellclasses$clearTier(Airship airship, WarpAnchor anchor, RiftDriveTier tier, double charge,
                                               CallbackInfoReturnable<WarpFailure> cir) {
        SPELLCLASSES$TIER_OK.remove();
    }

    /**
     * A Rift Beacon summoning across dimensions: the fix is in another dimension, so distance/range checks make
     * no sense. Like cross-dimension anchors, it needs a full charge (a jump of that size costs everything).
     */
    @Inject(method = "validateFix", at = @At("HEAD"), cancellable = true)
    private static void spellclasses$crossFix(Airship airship, BlockPos fix, RiftDriveTier tier, double charge,
                                              CallbackInfoReturnable<WarpFailure> cir) {
        if (CrossFix.DIMENSION.get() != null) {
            cir.setReturnValue(charge + 1.0E-4 < 1.0 ? WarpFailure.INSUFFICIENT_CHARGE : WarpFailure.NONE);
        }
    }
}
