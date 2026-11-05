package dev.qur1s.spellclasses.mixin;

import java.lang.reflect.Method;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fresh Animations (via EMF) sets the legs to the seated pose whenever is_riding is true, which overrides
 * Chromeware's lying animation: the patient lies down with raised legs. While a surgery seat is not idle,
 * hide the vehicle from EMF so the legs follow the Chromeware animation instead.
 */
@Pseudo
@Mixin(targets = "traben.entity_model_features.models.animation.state.EMFEntityRenderStateViaReference", remap = false)
public abstract class EmfSurgerySeatRidingMixin {
    private static final String SEAT = "com.chromeware.surgery.SurgerySeat";
    private static Method spellclasses$emfEntity;
    private static Method spellclasses$stage;
    private static boolean spellclasses$broken;

    @Inject(method = "hasVehicle()Z", at = @At("RETURN"), cancellable = true, require = 0)
    private void spellclasses$lyingOnSurgerySeat(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || spellclasses$broken) return;
        try {
            if (spellclasses$emfEntity == null) spellclasses$emfEntity = getClass().getMethod("emfEntity");
            if (!(spellclasses$emfEntity.invoke(this) instanceof Entity entity)) return;
            Entity vehicle = entity.getVehicle();
            if (vehicle == null || !SEAT.equals(vehicle.getClass().getName())) return;
            if (spellclasses$stage == null) spellclasses$stage = vehicle.getClass().getMethod("stage");
            Object stage = spellclasses$stage.invoke(vehicle);
            if (stage instanceof Enum<?> e && !"IDLE".equals(e.name())) cir.setReturnValue(false);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            spellclasses$broken = true;
        }
    }
}
