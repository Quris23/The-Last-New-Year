package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.implant.FaceImplants;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The implanted Hazmat Mask is drawn without the hood and the mask plate: only the two animated breathers (the black respirator)
 * stay. The parts' own boxes are skipped while the implant is drawn (their children, the breathers, are still drawn), and drawn
 * again for a real worn mask.
 */
@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.model.layered.HazmatArmorModel", remap = false)
@SuppressWarnings("rawtypes")
public abstract class HazmatMaskRespiratorMixin extends HumanoidModel {
    @Shadow(remap = false)
    public ModelPart mask;

    protected HazmatMaskRespiratorMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "withAnimations(Lnet/minecraft/world/entity/LivingEntity;)Lcom/github/alexmodguy/alexscaves/client/model/layered/HazmatArmorModel;",
            at = @At("RETURN"), remap = false, require = 0)
    private void spellclasses$respiratorOnly(LivingEntity entity, CallbackInfoReturnable<Object> cir) {
        boolean implant = FaceImplants.RENDER_HEAD.get() != null;
        this.head.skipDraw = implant;
        this.hat.skipDraw = implant;
        this.mask.skipDraw = implant;
    }
}
