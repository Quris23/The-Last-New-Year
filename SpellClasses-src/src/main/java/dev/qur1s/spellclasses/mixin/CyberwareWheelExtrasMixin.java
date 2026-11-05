package dev.qur1s.spellclasses.mixin;

import com.chromeware.client.cyberware.CyberwareWheelRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qur1s.spellclasses.implant.FaceImplants;
import dev.qur1s.spellclasses.network.NightVisionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * An extra wedge in Chromeware's implant ability wheel: the implanted Night Vision Goggles (on/off). The wheel only knows its own
 * abilities, so the wedge is added after them: the wheel is one wedge longer, the wedge is labelled and drawn here, and
 * releasing the key on it sends our own packet.
 */
@Pseudo
@Mixin(targets = "com.chromeware.client.cyberware.CyberwareWheelScreen", remap = false)
public abstract class CyberwareWheelExtrasMixin extends Screen {
    @Shadow(remap = false)
    private List<?> entries;
    @Shadow(remap = false)
    private boolean finished;

    @Shadow(remap = false)
    private int selection(double mouseX, double mouseY) {
        return -1;
    }

    @Shadow(remap = false)
    private boolean eligible() {
        return false;
    }

    protected CyberwareWheelExtrasMixin() {
        super(Component.empty());
    }

    /** The switches added after the Chromeware abilities, in wedge order: goggles (id 0), then the Holographic Apple (id 1). */
    private static List<ItemStack> spellclasses$extras() {
        var player = Minecraft.getInstance().player;
        if (player == null) return List.of();
        var list = new java.util.ArrayList<ItemStack>();
        ItemStack goggles = FaceImplants.find(player, FaceImplants::isGoggles);
        if (!goggles.isEmpty()) list.add(goggles);
        ItemStack apple = FaceImplants.find(player, FaceImplants::isApple);
        if (!apple.isEmpty()) list.add(apple);
        return list;
    }

    private static boolean spellclasses$on(ItemStack stack) {
        return FaceImplants.isApple(stack) ? FaceImplants.appleOn(stack) : FaceImplants.on(stack);
    }

    @Inject(method = "count()I", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void spellclasses$count(CallbackInfoReturnable<Integer> cir) {
        int extras = spellclasses$extras().size();
        if (extras > 0) cir.setReturnValue(Math.max(2, this.entries.size() + extras + 1));
    }

    /** The wedge right after the Chromeware abilities is ours: its label, hint and icon. */
    @WrapOperation(method = "render", remap = false, require = 0,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/chromeware/client/cyberware/CyberwareWheelRenderer;drawWedge(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIDDIZLnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V"))
    private void spellclasses$wedge(GuiGraphics g, Font font, int cx, int cy, double start, double end, int count, boolean selected,
                                    Component label, Component detail, Operation<Void> original) {
        List<ItemStack> extras = spellclasses$extras();
        int mine = this.entries.size();
        for (int k = 0; k < extras.size(); k++) {
            if (Math.abs(start - CyberwareWheelRenderer.wedgeStartAngle(mine + k, count)) >= 1.0E-6) continue;
            ItemStack stack = extras.get(k);
            boolean on = spellclasses$on(stack);
            original.call(g, font, cx, cy, start, end, count, selected,
                    FaceImplants.isApple(stack) ? Component.literal("Проекция") : stack.getHoverName(),
                    Component.literal(on ? "Выключить" : "Включить"));
            double angle = Math.toRadians((start + end) / 2.0);
            double radius = (CyberwareWheelRenderer.innerRadius(count) + CyberwareWheelRenderer.outerRadius(count)) / 2.0;
            double offset = selected ? 6.0 : 0.0;
            g.renderItem(stack, (int) Math.round(Math.cos(angle) * (radius + offset)) - 8,
                    (int) Math.round(Math.sin(angle) * (radius + offset)) - 30);
            return;
        }
        original.call(g, font, cx, cy, start, end, count, selected, label, detail);
    }

    @Inject(method = "finishSelection()V", at = @At("HEAD"), remap = false, require = 0)
    private void spellclasses$choose(CallbackInfo ci) {
        List<ItemStack> extras = spellclasses$extras();
        if (this.finished || extras.isEmpty()) return;
        int selected = this.selection(
                this.minecraft.mouseHandler.xpos() * (double) this.width / (double) this.minecraft.getWindow().getScreenWidth(),
                this.minecraft.mouseHandler.ypos() * (double) this.height / (double) this.minecraft.getWindow().getScreenHeight());
        int k = selected - this.entries.size();
        if (this.eligible() && k >= 0 && k < extras.size()) {
            PacketDistributor.sendToServer(new NightVisionPayload(FaceImplants.isApple(extras.get(k)) ? 1 : 0));
        }
    }
}
