package dev.qur1s.reviveraltar.mixin.optional;

import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables "Born in Chaos"'s own death-totem-in-hand save mechanic entirely: the Death
 * Totem should only work as fuel for the Ritual Altar, never by itself. A soft/optional
 * target - this mixin config simply doesn't apply if that mod isn't installed.
 */
@Mixin(targets = "net.mcreator.borninchaosv.procedures.DeathTotemKoghdaPriedmietNakhoditsiaVRukieProcedure", remap = false)
public class DeathTotemStandaloneBanMixin {

    @Inject(method = "onEntityDeath", at = @At("HEAD"), cancellable = true)
    private static void reviveraltar$disableStandaloneUse(LivingDeathEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
