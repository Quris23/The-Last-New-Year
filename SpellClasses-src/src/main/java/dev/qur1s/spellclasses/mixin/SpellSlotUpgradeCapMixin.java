package dev.qur1s.spellclasses.mixin;

import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Raises the Arcane Anvil's spell-slot upgrade item's cap from its registered 12 to 16, tooltip included. */
@Mixin(value = SpellSlotUpgradeItem.class, remap = false)
public abstract class SpellSlotUpgradeCapMixin {
    private static final int NEW_CAP = 16;

    @Shadow
    @Mutable
    @SuppressWarnings("unused")
    private int maxSlots;

    @Shadow
    @Mutable
    @SuppressWarnings("unused")
    private Component description;

    @Inject(method = "<init>(ILnet/minecraft/world/item/Item$Properties;)V", at = @At("TAIL"))
    private void spellclasses$raiseCap(int maxSlotsToUpgradeTo, Item.Properties properties, CallbackInfo ci) {
        this.maxSlots = NEW_CAP;
        this.description = Component.translatable("item.irons_spellbooks.spell_slot_upgrade_desc", NEW_CAP).withStyle(ChatFormatting.GRAY);
    }
}
