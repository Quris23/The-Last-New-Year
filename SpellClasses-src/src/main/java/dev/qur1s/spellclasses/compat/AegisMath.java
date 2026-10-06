package dev.qur1s.spellclasses.compat;

/**
 * The reworked Sentinel Aegis scale (a plain class, because mixin classes cannot be called directly): measured
 * against at least the vanilla maximum of 20 health, with the full +20 armor reached at 2 health (one heart).
 */
public final class AegisMath {
    public static final float REFERENCE_MAX = 20.0f;
    public static final float FULL_BONUS_AT = 2.0f;

    private AegisMath() {
    }

    /** 0 at full health, 1 at {@link #FULL_BONUS_AT} health or less. */
    public static float missingFraction(float health, float maxHealth) {
        float reference = Math.max(maxHealth, REFERENCE_MAX);
        return Math.max(0.0f, Math.min(1.0f, (reference - health) / (reference - FULL_BONUS_AT)));
    }
}
