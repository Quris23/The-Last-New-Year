package dev.qur1s.sphereshields;

import net.minecraft.resources.ResourceLocation;

/** The 34 "Dragon Script" glyph textures, in a fixed order used to index into them. */
public final class ShieldGlyphs {
    private ShieldGlyphs() {
    }

    public static final String[] NAMES = {
            "a", "b", "d", "e", "f", "g",
            "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y",
            "z", "aa", "ah", "ei", "ey", "ii",
            "ir", "oo", "uu", "ur"
    };

    public static final int COUNT = NAMES.length;

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[COUNT];

    static {
        for (int i = 0; i < COUNT; i++) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath("sphereshields", "textures/shield_glyphs/" + NAMES[i] + ".png");
        }
    }

    public static ResourceLocation texture(int index) {
        return TEXTURES[Math.floorMod(index, COUNT)];
    }
}
