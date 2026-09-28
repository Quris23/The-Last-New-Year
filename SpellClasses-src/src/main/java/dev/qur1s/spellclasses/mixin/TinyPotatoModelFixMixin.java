package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.client.event.load.ZModel;

/**
 * Quark's tiny-potato model-baking hook blindly wraps whatever is in the baking-result map for
 * {@code quark:tiny_potato} (via {@code Map.compute}), even when that key isn't present — producing a
 * {@code TinyPotatoModel} whose {@code originalModel} is null. Continuity then crashes the game hard
 * the first time it calls {@code isCustomRenderer()} on that broken model during its own baking pass.
 * Skip Quark's handler entirely when the key isn't already baked, exactly like {@code computeIfPresent}
 * would.
 */
@Mixin(value = TinyPotatoModule.Client.class, remap = false)
public abstract class TinyPotatoModelFixMixin {
    @Inject(method = "modelBake", at = @At("HEAD"), cancellable = true)
    private void spellclasses$skipIfNotBaked(ZModel.ModifyBakingResult event, CallbackInfo ci) {
        ModelResourceLocation tinyPotato = ModelResourceLocation.standalone(Quark.asResource("tiny_potato"));
        if (!event.getModels().containsKey(tinyPotato)) {
            ci.cancel();
        }
    }
}
