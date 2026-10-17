package dev.qur1s.spellclasses.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Removes Brontes' "(Also, this is a pickaxe)" tooltip line (cataclysm: item.cataclysm.brontes.desc2). */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class BrontesTooltip {
    private static final String LINE = "item.cataclysm.brontes.desc2";

    private BrontesTooltip() {
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        event.getToolTip().removeIf(line -> line.getContents() instanceof TranslatableContents tc && LINE.equals(tc.getKey()));
    }
}
