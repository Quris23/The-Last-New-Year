package dev.qur1s.atlas.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.lang.reflect.Field;

/** The description box of the marker editor, drawn in ink on parchment instead of white on black. */
public class ParchmentTextArea extends MultiLineEditBox {
    private static Field textFieldField;
    private final Font font;
    private final MultilineTextField field;

    public ParchmentTextArea(Font font, int x, int y, int width, int height, Component placeholder, Component message) {
        super(font, x, y, width, height, placeholder, message);
        this.font = font;
        this.field = textField(this);
    }

    private static MultilineTextField textField(MultiLineEditBox box) {
        try {
            if (textFieldField == null) {
                textFieldField = MultiLineEditBox.class.getDeclaredField("textField");
                textFieldField.setAccessible(true);
            }
            return (MultilineTextField) textFieldField.get(box);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static java.lang.reflect.Method beginMethod, endMethod;

    /** begin and end index of a line view (the record is not accessible from here, hence reflection). */
    private static int[] range(Object view) {
        try {
            if (beginMethod == null) {
                beginMethod = view.getClass().getDeclaredMethod("beginIndex");
                endMethod = view.getClass().getDeclaredMethod("endIndex");
                beginMethod.setAccessible(true);
                endMethod.setAccessible(true);
            }
            return new int[]{(int) beginMethod.invoke(view), (int) endMethod.invoke(view)};
        } catch (ReflectiveOperationException e) {
            return new int[]{0, 0};
        }
    }

    @Override
    protected void renderBackground(GuiGraphics graphics) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        graphics.fill(x, y, x + w, y + h, isFocused() ? 0xFF8A5A22 : 0xFF6B4423);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFF6E7BC);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (field == null) {
            super.renderContents(graphics, mouseX, mouseY, partialTick);
            return;
        }
        String value = field.value();
        int ink = 0xFF3B2410;
        if (value.isEmpty() && !isFocused()) return;
        int cursor = field.cursor();
        boolean blink = isFocused() && (Util.getMillis() / 300L) % 2L == 0L;
        int lineY = getY() + innerPadding();
        int[] selection = field.hasSelection() ? range(field.getSelected()) : new int[]{-1, -1};
        int selectBegin = selection[0], selectEnd = selection[1];
        boolean cursorDrawn = false;
        for (int lineIndex = 0; lineIndex < field.getLineCount(); lineIndex++) {
            int[] line = range(field.getLineView(lineIndex));
            boolean visible = withinContentAreaTopBottom(lineY, lineY + 9);
            int left = getX() + innerPadding();
            if (visible) {
                String text = value.substring(line[0], line[1]);
                if (selectBegin >= 0 && selectEnd > line[0] && selectBegin < line[1]) {
                    int from = Math.max(selectBegin, line[0]) - line[0];
                    int to = Math.min(selectEnd, line[1]) - line[0];
                    graphics.fill(left + font.width(text.substring(0, from)), lineY,
                            left + font.width(text.substring(0, to)), lineY + 9, 0x66A87A3A);
                }
                graphics.drawString(font, text, left, lineY, ink, false);
                if (blink && !cursorDrawn && cursor >= line[0] && cursor <= line[1]
                        && (cursor < line[1] || line[1] == value.length() || value.charAt(line[1] - 1) != ' ')) {
                    int cx = left + font.width(text.substring(0, cursor - line[0]));
                    graphics.fill(cx, lineY - 1, cx + 1, lineY + 9, ink);
                    cursorDrawn = true;
                }
            }
            lineY += 9;
        }
    }
}
