package dev.qur1s.spellclasses.mixin;

import com.chromeware.client.cyberware.CyberwareMenuLayout;
import dev.qur1s.spellclasses.implant.KSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.ArrayList;
import java.util.List;

/** The К sockets in Chromeware's implant menu: contents, the letter on the socket, removal and which items the chair offers for them. */
@Pseudo
@Mixin(targets = "com.chromeware.client.cyberware.CyberwareScreen", remap = false)
public abstract class CyberwareScreenKMixin {
    @Shadow(remap = false)
    private String focus;
    @Shadow(remap = false)
    private int focusIndex;

    @Shadow(remap = false)
    abstract boolean onChair();

    @Shadow(remap = false)
    private void cornerLetter(GuiGraphics g, String letter, int x, int y) {
    }

    @Shadow(remap = false)
    private static float o(double v) {
        throw new AssertionError();
    }

    @Inject(method = "installed(Ljava/lang/String;I)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true,
            remap = false, require = 0)
    private void spellclasses$kInstalled(String category, int index, CallbackInfoReturnable<ItemStack> cir) {
        if ("operating_system".equals(category) && index >= KSlots.FIRST && index <= KSlots.LAST) {
            var player = Minecraft.getInstance().player;
            cir.setReturnValue(player == null ? ItemStack.EMPTY : KSlots.slot(player, index));
        }
    }

    /** The letter К in the corner of both sockets, like Л and П on the arm sockets. */
    @Inject(method = "drawCyberware(Lnet/minecraft/client/gui/GuiGraphics;DD)Ljava/util/List;", at = @At("RETURN"),
            remap = false, require = 0)
    private void spellclasses$kLetters(GuiGraphics g, double mx, double my, CallbackInfoReturnable<List<Component>> cir) {
        for (CyberwareMenuLayout.Group group : CyberwareMenuLayout.GROUPS) {
            if (!group.id().equals("operating_system")) continue;
            int size = Math.round(o(28.0));
            for (int k = KSlots.FIRST; k <= KSlots.LAST && k < group.count(); k++) {
                int sx = Math.round(o(group.x() + k * 38));
                int sy = Math.round(o(group.y()));
                this.cornerLetter(g, "N", sx + (size - 16) / 2 + 15, sy + (size - 16) / 2 + 9);
            }
        }
    }

    @Inject(method = "removeSelected()V", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$kRemove(CallbackInfo ci) {
        if ("operating_system".equals(this.focus) && this.focusIndex >= KSlots.FIRST) {
            var player = Minecraft.getInstance().player;
            if (player != null && !KSlots.slot(player, this.focusIndex).isEmpty() && this.onChair()) {
                PacketDistributor.sendToServer(new com.chromeware.cyberware.ImplantPackets.RemoveInternal(KSlots.CATEGORY, this.focusIndex));
            }
            ci.cancel();
        }
    }

    /** The chair offers Sandevistan-type systems for the first socket and only Altimeter / Compass for the К ones. */
    @Inject(method = "stockFor(Ljava/lang/String;)Ljava/util/List;", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void spellclasses$kStock(String category, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!"operating_system".equals(category)) return;
        boolean k = this.focusIndex >= KSlots.FIRST;
        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack item : cir.getReturnValue()) {
            if (KSlots.isK(item) == k) filtered.add(item);
        }
        cir.setReturnValue(filtered);
    }

    /** Typing over an R/G/B value on the colour page (see CustomizeColorValueMixin) takes every key. */
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void spellclasses$colorValueKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (dev.qur1s.spellclasses.client.ColorValueEdit.key(keyCode, modifiers)) cir.setReturnValue(true);
    }
}
