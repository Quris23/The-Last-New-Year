package dev.qur1s.spellclasses.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Explains the full-set bonuses on every piece of the Scarecrow armor (irons_spellbooks:pumpkin_*). */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class ScarecrowTooltip {
    private ScarecrowTooltip() {
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals("irons_spellbooks") || !id.getPath().startsWith("pumpkin_")
                || id.getPath().equals("pumpkin_spice") ) return;
        if (!(id.getPath().endsWith("helmet") || id.getPath().endsWith("chestplate")
                || id.getPath().endsWith("leggings") || id.getPath().endsWith("boots"))) return;
        var tip = event.getToolTip();
        tip.add(Component.empty());
        tip.add(Component.translatable("tooltip.spellclasses.scarecrow.set").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("tooltip.spellclasses.scarecrow.hunger").withStyle(ChatFormatting.RED));
        tip.add(Component.translatable("tooltip.spellclasses.scarecrow.blood_step").withStyle(ChatFormatting.RED));
    }
}
