package dev.qur1s.spellclasses.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * A held item's tooltip already shows its own "Высеченное заклинание" (inscribed/preset spell)
 * section when it has one. Iron's Spellbooks separately appends a "Выбранное заклинание" (active
 * selection) line to whatever item currently provides the selected cast - for single-spell preset
 * weapons those are always the same spell, so it showed up twice. Suppressed entirely: either the
 * inscribed section shows, or nothing does.
 */
@Mixin(value = io.redspace.ironsspellbooks.player.ClientPlayerEvents.class, remap = false)
public abstract class HideSelectedSpellTooltipMixin {
    @Inject(method = "handleCastingImplementTooltip", at = @At("HEAD"), cancellable = true)
    private static void spellclasses$hideSelectedSpell(ItemStack stack, LocalPlayer player, List<Component> tooltip, boolean isAdvanced, CallbackInfo ci) {
        ci.cancel();
    }
}
