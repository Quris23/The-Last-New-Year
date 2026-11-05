package dev.qur1s.spellclasses.mixin;

import com.chromeware.humanity.Suppression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chromeware halves the time every next immunosuppressant dose adds (1/2, 1/4, 1/8...). Here every dose in a row adds the full time of
 * its grade. The rest of Chromeware's dose() (what is taken from the permanent and temporary loss, switching Standard to Military) is kept as is.
 */
@Pseudo
@Mixin(value = Suppression.class, remap = false)
public abstract class SuppressionRedoseMixin {
    @Inject(method = "dose(DDLcom/chromeware/humanity/Suppression$Grade;)Lcom/chromeware/humanity/Suppression$Dose;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$fullRedose(double permanent, double temporary, Suppression.Grade dose,
                                         CallbackInfoReturnable<Suppression.Dose> cir) {
        Suppression self = (Suppression) (Object) this;
        if (dose == null || dose == Suppression.Grade.NONE) {
            cir.setReturnValue(new Suppression.Dose(self, 0.0, 0.0));
            return;
        }
        double remaining = dose.removal;
        double fromTemporary = Math.min(Math.max(0.0, temporary), remaining);
        double fromPermanent = Math.min(Math.max(0.0, permanent), remaining - fromTemporary);
        double heldPermanent = self.storedPermanent() + fromPermanent;
        double heldTemporary = self.storedTemporary() + fromTemporary;
        if (heldPermanent <= 1.0E-9 && heldTemporary <= 1.0E-9) {
            cir.setReturnValue(new Suppression.Dose(Suppression.NONE, fromPermanent, fromTemporary));
            return;
        }
        Suppression.Grade active = self.grade();
        int window = self.protectionTicks();
        int count = self.doses();
        if (!self.protecting()) {
            active = dose;
            count = 0;
            window = dose.protection;
        } else if (self.grade() == Suppression.Grade.STANDARD && dose == Suppression.Grade.MILITARY) {
            active = Suppression.Grade.MILITARY;
            count = 0;
            window = Suppression.Grade.MILITARY.protection;
        } else {
            count = Math.min(30, count + 1);
            Suppression.Grade timing = self.grade() == Suppression.Grade.MILITARY ? Suppression.Grade.MILITARY : Suppression.Grade.STANDARD;
            window = (int) Math.min(Integer.MAX_VALUE, (long) window + (long) timing.protection); // the full time, not a half, a quarter...
        }
        cir.setReturnValue(new Suppression.Dose(
                new Suppression(heldPermanent, heldTemporary, window, active.returnInterval, active.returnInterval, active, count),
                fromPermanent, fromTemporary));
    }
}
