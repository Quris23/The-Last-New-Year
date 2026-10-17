package dev.qur1s.spellclasses.mixin;

import java.util.function.Function;

import dev.qur1s.spellclasses.client.EngineerGlowLayers;
import dev.qur1s.spellclasses.client.EngineerSkinSparks;
import mod.azure.azurelib.common.render.armor.AzArmorRendererConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the glowing parts of the Engineer's armor (Cataclysm Spellbooks) shine: this armor is rendered by AzureLib, which draws
 * {@code textures/models/armor/engineer_armor_glowmask.png} at full brightness on top of the normal texture, but only when the
 * renderer was built with the auto-glowing layer - the original renderer doesn't add it, so it is added to its config here, and the skin is made animated (see EngineerSkinSparks).
 */
@Mixin(targets = "net.acetheeldritchking.cataclysm_spellbooks.entity.render.armor.EngineerMageArmorRenderer", remap = false)
public abstract class EngineerArmorGlowMixin {
    /** the armor's skin comes from a function of the game time, so the painted-on electric arcs can flicker (see EngineerSkinSparks) */
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lmod/azure/azurelib/common/render/armor/AzArmorRendererConfig;builder(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)Lmod/azure/azurelib/common/render/armor/AzArmorRendererConfig$Builder;"),
            require = 0)
    private static AzArmorRendererConfig.Builder spellclasses$animatedSkin(ResourceLocation model, ResourceLocation texture) {
        return AzArmorRendererConfig.builder((entity, stack) -> model, (entity, stack) -> EngineerSkinSparks.current(texture));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lmod/azure/azurelib/common/render/armor/AzArmorRendererConfig$Builder;setPipelineContext(Ljava/util/function/Function;)Lmod/azure/azurelib/common/render/armor/AzArmorRendererConfig$Builder;"),
            require = 0)
    private static AzArmorRendererConfig.Builder spellclasses$glowLayer(AzArmorRendererConfig.Builder builder, Function context) {
        org.apache.logging.log4j.LogManager.getLogger("spellclasses").info("Engineer armor: glow layer added to the renderer");
        builder.addRenderLayer(new EngineerGlowLayers.Steady());
        builder.addRenderLayer(new EngineerGlowLayers.Sparks());
        return builder.setPipelineContext(context);
    }
}
