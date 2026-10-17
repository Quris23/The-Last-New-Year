package dev.qur1s.spellclasses.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.Map;
import java.util.Optional;

/**
 * Shows the completion of an FTB Quests quest as an Advancement Plaques plaque: the mod draws plaques for vanilla advancement
 * toasts, so the quest is wrapped into a fake advancement toast (no real advancement exists behind it).
 */
public final class FtbPlaque {
    private FtbPlaque() {
    }

    /** True if the plaque was queued (the caller then drops FTB's own toast). */
    public static boolean show(QuestObject object) {
        if (!ModList.get().isLoaded("advancementplaques")) return false;
        try {
            Component[] title = new Component[1];
            ItemStack[] icon = new ItemStack[1];
            QuestReveal.run(() -> {
                title[0] = object.getTitle();
                Icon questIcon = object.getIcon();
                icon[0] = questIcon instanceof ItemIcon itemIcon ? itemIcon.getStack() : new ItemStack(Items.ENDER_EYE);
            });
            DisplayInfo display = new DisplayInfo(icon[0], title[0], Component.empty(), Optional.empty(),
                    AdvancementType.GOAL, true, false, false);
            var advancement = new net.minecraft.advancements.Advancement(Optional.empty(), Optional.of(display),
                    AdvancementRewards.EMPTY, Map.of(), AdvancementRequirements.EMPTY, false, Optional.empty());
            var holder = new AdvancementHolder(ResourceLocation.fromNamespaceAndPath("spellclasses", "ftb_quest_" + object.id), advancement);
            Minecraft.getInstance().getToasts().addToast(new AdvancementToast(holder));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
