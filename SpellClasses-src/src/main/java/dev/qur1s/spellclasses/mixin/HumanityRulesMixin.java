package dev.qur1s.spellclasses.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chromeware starts cyberpsychosis episodes at 60% humanity loss, i.e. below 40% Humanity. Here it starts at 60% Humanity
 * (40% loss). The interval between episodes is replaced (120 s at the threshold down to 5 s at zero Humanity); the
 * berserk chance and the visual intensity get a stretched loss (40..100 mapped onto 60..100), so they begin gently at the
 * new threshold and still reach the original worst settings at zero Humanity.
 */
@Pseudo
@Mixin(targets = "com.chromeware.humanity.HumanityRules", remap = false)
public abstract class HumanityRulesMixin {
    private static final double NEW_START = 40.0;
    private static final double OLD_START = 60.0;
    private static final double SCALE = (100.0 - OLD_START) / (100.0 - NEW_START);

    private static double spellclasses$stretch(double loss) {
        return loss >= NEW_START ? OLD_START + (loss - NEW_START) * SCALE : loss;
    }

    /** Seconds between episodes: 120 s at the new threshold, shortening to 5 s at zero Humanity. */
    @Inject(method = "episodeInterval(D)I", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void spellclasses$interval(double loss, CallbackInfoReturnable<Integer> cir) {
        double clamped = Math.clamp(loss, 0.0, 100.0);
        if (clamped < NEW_START) {
            cir.setReturnValue(0);
            return;
        }
        double t = (clamped - NEW_START) / (100.0 - NEW_START);
        cir.setReturnValue((int) Math.round(120.0 * Math.pow(5.0 / 120.0, t)));
    }

    @ModifyVariable(method = "berserkChance(D)D", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
    private static double spellclasses$berserk(double loss) {
        return spellclasses$stretch(loss);
    }

    @ModifyVariable(method = "intensity(D)F", at = @At("HEAD"), argsOnly = true, remap = false, require = 0)
    private static double spellclasses$intensity(double loss) {
        return spellclasses$stretch(loss);
    }
}
