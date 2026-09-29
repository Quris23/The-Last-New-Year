package dev.qur1s.sphereshields;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * One translucent, textured, lit RenderType per glyph - {@code POSITION_COLOR_TEX_LIGHTMAP} is the
 * same format/shader pairing particles use, which is exactly what we need: a texture sampled and
 * multiplied by a per-vertex tint (so the glyphs pick up the shield's dynamic color).
 */
public final class GlyphRenderTypes {
    private GlyphRenderTypes() {
    }

    private static final RenderType[] TYPES = new RenderType[ShieldGlyphs.COUNT];

    public static RenderType get(int glyphIndex) {
        int index = Math.floorMod(glyphIndex, ShieldGlyphs.COUNT);
        RenderType existing = TYPES[index];
        if (existing != null) return existing;

        RenderType created = RenderType.create(
                "sphereshields_glyph_" + index,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.TRIANGLES,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(ShieldGlyphs.texture(index), false, false))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false)
        );
        TYPES[index] = created;
        return created;
    }
}
