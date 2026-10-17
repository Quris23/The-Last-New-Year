package dev.qur1s.spellclasses.client;

import mod.azure.azurelib.common.cache.texture.AzAbstractTexture;
import mod.azure.azurelib.common.render.AzRendererPipelineContext;
import mod.azure.azurelib.common.render.layer.AzAutoGlowingLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * The two glow layers of the Engineer's armor.
 * <ul>
 *   <li>{@link Steady}: the yellow glowing parts. It always uses the glow of the plain skin, so it can't flicker when the skin is
 *       swapped for a spark variant (or when the variants' glow textures are still being rebuilt after a resource reload).</li>
 *   <li>{@link Sparks}: only while a spark variant is worn, draws that variant's glow on top, so the painted arcs shine too.</li>
 * </ul>
 */
public final class EngineerGlowLayers {
    private EngineerGlowLayers() {
    }

    public static class Steady<K, T> extends AzAutoGlowingLayer<K, T> {
        @Override
        protected RenderType determineRenderType(AzRendererPipelineContext<K, T> context) {
            return AzAbstractTexture.getRenderType(EngineerSkinSparks.PLAIN);
        }
    }

    public static class Sparks<K, T> extends AzAutoGlowingLayer<K, T> {
        @Override
        protected RenderType determineRenderType(AzRendererPipelineContext<K, T> context) {
            ResourceLocation variant = EngineerSkinSparks.variantNow();
            return variant == null ? null : AzAbstractTexture.getRenderType(variant);
        }
    }
}
