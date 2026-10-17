package dev.qur1s.spellclasses.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Animation of the Engineer's armor skin: now and then a single small blue electric arc (a piece of the lightning sprites of Iron's
 * Ascension effect) flickers on one spot of the armor, then another one somewhere else - they never all fire at once. The arcs are
 * painted into the skin itself: {@code engineer_armor_s1..s31.png} are copies of the normal skin with different arcs (each with its own
 * {@code _glowmask.png}, so the arcs glow), and the game time picks which one the armor wears. Most of the time that is the plain skin; in a few frames of the cycle the "reactor" gauge on the breastplate turns electric blue.
 */
public final class EngineerSkinSparks {
    private static final int VARIANTS = 31;
    private static final ResourceLocation[] SKINS = new ResourceLocation[VARIANTS + 1];
    static {
        for (int i = 1; i <= VARIANTS; i++)
            SKINS[i] = ResourceLocation.fromNamespaceAndPath("spellclasses", "textures/models/armor/engineer_armor_s" + i + ".png");
    }

    /** which skin variant to wear in each timeline frame (0 = the plain skin); generated together with the textures */
    private static final int[] TIMELINE = {0, 0, 0, 0, 1, 2, 3, 4, 0, 0, 0, 5, 6, 7, 8, 9, 10, 0, 0, 0, 11, 12, 13, 0, 0, 0, 0, 0, 14, 15, 16, 0, 0, 0, 17, 18, 0, 0, 19, 0, 0, 0, 0, 0, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 0, 31, 0, 0, 0};
    private static final int TICKS_PER_FRAME = 2;
    private static final int CYCLE = TIMELINE.length * TICKS_PER_FRAME;

    /** the plain skin of the armor (its glow never changes, whatever spark variant is worn) */
    public static final ResourceLocation PLAIN = ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "textures/models/armor/engineer_armor.png");

    private EngineerSkinSparks() {
    }

    /** the spark variant worn right now, or null while the plain skin is worn */
    public static ResourceLocation variantNow() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        int variant = TIMELINE[(int) (Math.floorMod(level.getGameTime(), CYCLE) / TICKS_PER_FRAME)];
        return variant == 0 ? null : SKINS[variant];
    }

    /** the skin the armor should wear right now: {@code plain} or one of the spark variants */
    public static ResourceLocation current(ResourceLocation plain) {
        var level = Minecraft.getInstance().level;
        if (level == null) return plain;
        int variant = TIMELINE[(int) (Math.floorMod(level.getGameTime(), CYCLE) / TICKS_PER_FRAME)];
        return variant == 0 ? plain : SKINS[variant];
    }
}
