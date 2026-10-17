package dev.qur1s.atlas.client;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** A one-line text field whose text is plain ink with no drop shadow (the game's field always adds one). */
public class InkField extends EditBox {
    private static final int INK = 0xFF3B2410;
    private final Font font;

    public InkField(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.font = font;
        setBordered(false);
        setTextColor(INK);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        String text = getValue();
        int cursor = Math.min(getCursorPosition(), text.length());
        int start = 0;
        while (start < cursor && font.width(text.substring(start, cursor)) > width - 2) start++;
        String shown = font.plainSubstrByWidth(text.substring(start), width);
        int y = getY() + (height - 8) / 2;
        graphics.drawString(font, shown, getX(), y, INK, false);
        if (isFocused() && (Util.getMillis() / 300L) % 2L == 0L) {
            int cx = getX() + font.width(text.substring(start, cursor));
            graphics.fill(cx, y - 1, cx + 1, y + 9, INK);
        }
    }
}
