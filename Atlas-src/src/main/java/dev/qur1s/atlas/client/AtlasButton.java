package dev.qur1s.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** A button in the Atlas's parchment-and-gold style: either an icon (with a tooltip) or a short text. */
public class AtlasButton extends Button {
    private static final ResourceLocation NORMAL = ResourceLocation.fromNamespaceAndPath("atlas", "button");
    private static final ResourceLocation HOVER = ResourceLocation.fromNamespaceAndPath("atlas", "button_hover");
    private static final int INK = 0xFF4B2E13;

    private final Supplier<ResourceLocation> icon;

    public AtlasButton(int x, int y, int width, int height, Component message, Supplier<ResourceLocation> icon,
                       OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.icon = icon;
        if (icon != null) setTooltip(Tooltip.create(message));
    }

    public void setLabel(Component message) {
        setMessage(message);
        if (icon != null) setTooltip(Tooltip.create(message));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blitSprite(isHoveredOrFocused() ? HOVER : NORMAL, getX(), getY(), getWidth(), getHeight());
        if (icon != null) {
            graphics.blit(icon.get(), getX() + (getWidth() - 16) / 2, getY() + (getHeight() - 16) / 2, 0, 0, 16, 16, 16, 16);
        } else {
            var font = Minecraft.getInstance().font;
            graphics.drawString(font, getMessage(), getX() + (getWidth() - font.width(getMessage())) / 2,
                    getY() + (getHeight() - 8) / 2, INK, false);
        }
    }
}
