package dev.qur1s.spellclasses.mixin;

import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.cyberware.ImplantColors;
import dev.qur1s.spellclasses.client.ColorValueEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The R/G/B numbers on Chromeware's implant colour page can be clicked and typed over (0-255, Enter to apply). */
@Pseudo
@Mixin(targets = "com.chromeware.client.cyberware.CustomizePage", remap = false)
public abstract class CustomizeColorValueMixin {
    private static final int NUM_X = 384;
    private static final int HEX_X = 334;
    private static final int HEX_W = 76;

    @Shadow(remap = false)
    private int selectedImplant;
    @Shadow(remap = false)
    private int selectedPart;
    @Shadow(remap = false)
    private boolean custom;

    @Shadow(remap = false)
    abstract int editorY();

    @Shadow(remap = false)
    abstract String part();

    @Shadow(remap = false)
    abstract int currentColor(String implant, String part);

    @Shadow(remap = false)
    abstract void setColor(String implant, String part, int rgb, boolean send);

    private static boolean spellclasses$inBox(double mx, double my, int y) {
        return mx >= NUM_X - 2 && mx <= NUM_X + 28 && my >= y - 5 && my <= y + 8;
    }

    @Inject(method = "init()V", at = @At("HEAD"), remap = false, require = 0)
    private void spellclasses$resetEdit(CallbackInfo ci) {
        ColorValueEdit.cancel();
        org.slf4j.LoggerFactory.getLogger("spellclasses").info("Colour page: value fields active");
    }

    @Inject(method = "clickControls(DD)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$clickValue(double mx, double my, CallbackInfoReturnable<Boolean> cir) {
        try {
            spellclasses$clickValue0(mx, my, cir);
        } catch (Throwable t) {
            ColorValueEdit.cancel();
            org.slf4j.LoggerFactory.getLogger("spellclasses").error("Colour value click failed", t);
        }
    }

    private void spellclasses$clickValue0(double mx, double my, CallbackInfoReturnable<Boolean> cir) {
        String part = this.part();
        if (part != null && this.custom) {
            int ey = this.editorY();
            if (mx >= HEX_X && mx <= HEX_X + HEX_W && my >= ey + 75 && my <= ey + 89) {
                String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
                ColorValueEdit.start(ColorValueEdit.HEX, this.currentColor(implant, part) & 0xFFFFFF, this.selectedImplant, this.selectedPart);
                ColorValueEdit.buffer = String.format("%06X", this.currentColor(implant, part) & 0xFFFFFF);
                cir.setReturnValue(true);
                return;
            }
            for (int k = 0; k < 3; k++) {
                int y = ey + 26 + k * 16;
                if (spellclasses$inBox(mx, my, y)) {
                    String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
                    int current = this.currentColor(implant, part) >> (16 - k * 8) & 0xFF;
                    ColorValueEdit.start(k, current, this.selectedImplant, this.selectedPart);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        if (ColorValueEdit.active) ColorValueEdit.commit(); // a click anywhere else applies what was typed
    }

    /** Applies a typed-in value (see ColorValueEdit.commit) to the colour of the part it was typed for, before the page is drawn. */
    @Inject(method = "draw(Lnet/minecraft/client/gui/GuiGraphics;DD)Ljava/util/List;", at = @At("HEAD"), remap = false, require = 0)
    private void spellclasses$applyValue(GuiGraphics g, double mx, double my, CallbackInfoReturnable<?> cir) {
        try {
            spellclasses$applyValue0();
        } catch (Throwable t) {
            ColorValueEdit.pendingChannel = -1;
            org.slf4j.LoggerFactory.getLogger("spellclasses").error("Colour value apply failed", t);
        }
    }

    private void spellclasses$applyValue0() {
        if (ColorValueEdit.pendingChannel < 0) return;
        int channel = ColorValueEdit.pendingChannel;
        int value = ColorValueEdit.pendingValue;
        ColorValueEdit.pendingChannel = -1;
        java.util.List<String> parts = ImplantTint.parts(ImplantColors.IMPLANTS.get(ColorValueEdit.implantIndex));
        if (parts.isEmpty()) return;
        String implant = ImplantColors.IMPLANTS.get(ColorValueEdit.implantIndex);
        String part = parts.get(Math.min(ColorValueEdit.partIndex, parts.size() - 1));
        if (channel == ColorValueEdit.HEX) {
            this.setColor(implant, part, value & 0xFFFFFF, true);
        } else {
            int shift = 16 - channel * 8;
            int color = this.currentColor(implant, part);
            this.setColor(implant, part, color & ~(255 << shift) | value << shift, true);
        }
        org.slf4j.LoggerFactory.getLogger("spellclasses").info("Colour value typed in: {}:{} channel {} = {}", implant, part, channel, value);
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/GuiGraphics;DD)Ljava/util/List;", at = @At("RETURN"), remap = false, require = 0)
    private void spellclasses$drawValue(GuiGraphics g, double mx, double my, CallbackInfoReturnable<?> cir) {
        try {
            spellclasses$drawValue0(g);
        } catch (Throwable t) {
            ColorValueEdit.cancel();
            org.slf4j.LoggerFactory.getLogger("spellclasses").error("Colour value draw failed", t);
        }
    }

    private void spellclasses$drawValue0(GuiGraphics g) {
        if (!this.custom || this.part() == null) {
            ColorValueEdit.cancel();
            return;
        }
        int ey = this.editorY();
        var font = Minecraft.getInstance().font;
        // the hex field: always shown in the custom mode, click to type a value
        boolean editHex = ColorValueEdit.active && ColorValueEdit.channel == ColorValueEdit.HEX;
        String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
        String hexText = editHex
                ? "#" + ColorValueEdit.buffer + ((System.currentTimeMillis() / 500 & 1) == 0 ? "_" : "")
                : String.format("#%06X", this.currentColor(implant, this.part()) & 0xFFFFFF);
        g.fill(HEX_X, ey + 76, HEX_X + HEX_W, ey + 88, editHex ? 0xF0101018 : 0x66000000);
        g.renderOutline(HEX_X, ey + 76, HEX_W, 12, editHex ? 0xFFE0B040 : 0x66FFFFFF);
        g.drawString(font, hexText, HEX_X + 4, ey + 78, 0xFFFFFFFF, false);
        if (!ColorValueEdit.active || editHex) return;
        int y = ey + 26 + ColorValueEdit.channel * 16;
        g.fill(NUM_X - 2, y - 4, NUM_X + 26, y + 8, 0xF0101018);
        g.renderOutline(NUM_X - 2, y - 4, 28, 12, 0xFFE0B040);
        String text = ColorValueEdit.buffer + ((System.currentTimeMillis() / 500 & 1) == 0 ? "_" : "");
        g.drawString(font, text, NUM_X, y - 3, 0xFFFFFFFF, false);
    }
}
