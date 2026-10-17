package dev.qur1s.atlas.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** A small name tag on the map: little text on a parchment slip with a brown edge. */
public final class AtlasLabels {
    public static final int INK = 0xFF3B2410;
    private static final float SCALE = 0.75f;

    private AtlasLabels() {
    }

    public static void draw(GuiGraphics graphics, Font font, String text, int centreX, int top) {
        if (text.isEmpty()) return;
        int textWidth = font.width(text);
        int width = (int) Math.ceil(textWidth * SCALE) + 6;
        int left = centreX - width / 2;
        graphics.fill(left, top, left + width, top + 10, 0xC8F3E3B5);
        graphics.renderOutline(left, top, width, 10, 0xFF6B4423);
        graphics.pose().pushPose();
        graphics.pose().translate(left + 3, top + 2, 0);
        graphics.pose().scale(SCALE, SCALE, 1f);
        graphics.drawString(font, text, 0, 0, INK, false);
        graphics.pose().popPose();
    }
}
