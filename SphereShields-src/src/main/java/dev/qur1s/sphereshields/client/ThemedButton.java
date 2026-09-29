package dev.qur1s.sphereshields.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** A {@link Button} that draws itself with {@link SphereShieldsGuiStyle} colors instead of the vanilla sprite. */
class ThemedButton extends Button {
    private boolean highlighted;

    ThemedButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int border = this.highlighted ? SphereShieldsGuiStyle.ACCENT
                : this.isHovered() ? SphereShieldsGuiStyle.PANEL_EDGE
                : SphereShieldsGuiStyle.PANEL_DARK;
        int fill = this.highlighted ? SphereShieldsGuiStyle.PANEL_COLOR : SphereShieldsGuiStyle.PANEL_DARK;

        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), border);
        graphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, fill);

        int textColor = !this.active ? 0xFF556170
                : this.highlighted ? SphereShieldsGuiStyle.TEXT_COLOR
                : SphereShieldsGuiStyle.MUTED_TEXT_COLOR;
        graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(),
                getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, textColor);
    }
}
