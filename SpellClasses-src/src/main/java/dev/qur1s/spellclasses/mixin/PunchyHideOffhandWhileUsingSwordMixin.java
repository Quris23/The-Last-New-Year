package dev.qur1s.spellclasses.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While Helvar's Sword or the Great Sword of Souls is being used (right mouse button held), Punchy raises the empty left arm into view. Punchy asks
 * {@code PunchyConfig.isHandBlacklisted} before drawing a hand; answering "blacklisted" for the off hand in exactly that
 * situation hides the left arm and leaves everything else about Punchy (including the sword's right hand) untouched.
 */
@Mixin(targets = "punchy.config.PunchyConfig", remap = false)
public abstract class PunchyHideOffhandWhileUsingSwordMixin {
    @Inject(method = "isHandBlacklisted(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Z",
            at = @At("RETURN"), cancellable = true, require = 0)
    private static void spellclasses$hideOffhandWhileUsingSword(Player player, InteractionHand hand,
                                                                CallbackInfoReturnable<Boolean> cir) {
        if (hand != InteractionHand.OFF_HAND || cir.getReturnValueZ() || player == null) return;
        // Only while the off hand is empty and the main hand holds something: the empty left arm must not pop up and
        // punch whenever the right hand attacks (left click) or uses the item (right click).
        if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) return;
        var main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.isEmpty()) return;
        // Bows and crossbows are drawn with both hands: the left arm pulls the string.
        var useAnim = main.getUseAnimation();
        if (useAnim == net.minecraft.world.item.UseAnim.BOW || useAnim == net.minecraft.world.item.UseAnim.CROSSBOW
                || useAnim == net.minecraft.world.item.UseAnim.SPYGLASS) return;
        if (player.isUsingItem() || player.swinging) {
            cir.setReturnValue(true);
        }
    }
}
