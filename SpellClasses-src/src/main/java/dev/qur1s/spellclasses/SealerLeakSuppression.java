package dev.qur1s.spellclasses;

/**
 * Server tick is single-threaded per level, so a plain static flag is enough to signal, for the
 * duration of one {@code ProgressiveBlockSealer.addToGoggleTooltip} call, that the caller (a
 * regulator sealed via a SphereShields field - see {@code TemperatureRegulatorGoggleLeakMixin}) wants
 * {@code hasLeak()} to read as false just for that call, so the tooltip falls into its normal
 * "blocks filled" branch instead of the "area too big / unsealed" warning branch. Scoped to a single
 * call (set immediately before, cleared immediately after) so other sealer users (oxygen sealers,
 * combustion engines) are never affected.
 */
public final class SealerLeakSuppression {
    private SealerLeakSuppression() {
    }

    public static boolean active = false;
}
