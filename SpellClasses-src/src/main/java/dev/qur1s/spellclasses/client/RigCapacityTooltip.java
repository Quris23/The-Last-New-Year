package dev.qur1s.spellclasses.client;

import dev.qur1s.spellclasses.OctoRigBackSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Tentacle rigs and backpack upgrades are installed as Chromeware implants, so they carry the same two aqua lines as every implant: the
 * socket category and the implant capacity. Each is added only when Chromeware's own tooltip did not already add it.
 */
@EventBusSubscriber(modid = "spellclasses", value = Dist.CLIENT)
public final class RigCapacityTooltip {
    private static final String SLOT = "tooltip.chromeware.slot";
    private static final String CAPACITY = "tooltip.chromeware.capacity";

    private RigCapacityTooltip() {
    }

    private static boolean has(List<Component> tip, String key) {
        for (Component line : tip) {
            if (line.getContents() instanceof TranslatableContents tc && key.equals(tc.getKey())) return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (dev.qur1s.spellclasses.implant.GauntletImplants.isGauntlet(stack)) {
            var tip = event.getToolTip();
            // implants only: their own description and the Curios hand-slot line are gone, the implant description is shown instead
            tip.removeIf(line -> line.getContents() instanceof TranslatableContents tc
                    && (tc.getKey().startsWith("item.cataclysm.gauntlet_of_") || "curios.tooltip.slot".equals(tc.getKey())));
            if (!has(tip, SLOT)) {
                tip.add(Component.translatable(SLOT, Component.translatable("screen.chromeware.category.arms")).withStyle(ChatFormatting.AQUA));
            }
            if (!has(tip, CAPACITY)) tip.add(Component.translatable(CAPACITY, 25).withStyle(ChatFormatting.AQUA));
            tip.add(Component.literal("Устанавливается в слот руки.").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("Можно использовать только при активном боевом режиме.").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("ЛКМ — способность правой руки.").withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal("ПКМ — способность левой руки.").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (dev.qur1s.spellclasses.implant.FaceImplants.isMask(stack) || dev.qur1s.spellclasses.implant.FaceImplants.isGoggles(stack)) {
            // implants only: Curios' "Slot: Head" line has no meaning for them
            event.getToolTip().removeIf(line -> line.getContents() instanceof TranslatableContents tc && "curios.tooltip.slot".equals(tc.getKey()));
        }
        if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("somakespells:tide_piercer")) {
            var tip = event.getToolTip();
            tip.add(Component.empty());
            tip.add(Component.literal("Особая способность").withStyle(ChatFormatting.AQUA));
            tip.add(Component.literal("Зажмите ПКМ: ближайший блок воды рядом (до 4 блоков) пропадает, как будто его забрали ведром, и вы восполняете 100 маны.").withStyle(ChatFormatting.GRAY));
        }
        if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("chromeware:zetatech_berserk")) {
            event.getToolTip().add(Component.literal("С каждым убитым противником: −5% человечности.").withStyle(ChatFormatting.RED));
        }
        if (dev.qur1s.spellclasses.implant.FaceImplants.isApple(stack)) {
            event.getToolTip().add(Component.literal("Позволяет видеть все размещённые схемы Create из вашего инвентаря.").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.literal("Схемы показываются одновременно, а не только та, что в руке.").withStyle(ChatFormatting.GRAY));
        }
        boolean rig = OctoRigBackSlot.isTentacleRig(stack);
        var rule = dev.qur1s.spellclasses.implant.ImplantUpgrades.rule(stack);
        if (!rig && rule == null) return;
        String category = rig ? "skeleton" : dev.qur1s.spellclasses.implant.ImplantUpgrades.categoryKey(rule.category());
        int capacity = rig ? OctoRigBackSlot.rigCapacity(stack) : rule.capacity();
        var tip = event.getToolTip();
        if (!has(tip, SLOT)) {
            tip.add(Component.translatable(SLOT, Component.translatable("screen.chromeware.category." + category))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (!has(tip, CAPACITY)) {
            tip.add(Component.translatable(CAPACITY, capacity).withStyle(ChatFormatting.AQUA));
        }
    }
}
