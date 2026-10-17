package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.entity.armor.pumpkin.PumpkinArmorRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Makes the carved-out eyes and mouth of the Scarecrow armor glow: GeckoLib's auto-glow layer draws
 * {@code textures/models/armor/pumpkin_glowmask.png} at full brightness on top of the normal texture.
 */
@Mixin(value = PumpkinArmorRenderer.class, remap = false)
public abstract class PumpkinArmorGlowMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void spellclasses$glowingFace(CallbackInfo ci) {
        GeoArmorRenderer renderer = (GeoArmorRenderer) (Object) this;
        renderer.addRenderLayer(new AutoGlowingGeoLayer(renderer));
    }
}
