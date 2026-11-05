package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.ArrayList;
import java.util.Set;

/**
 * Scrolls of the cut Somake spells (the pack's "Hex Crystals") are taken out of the creative tabs and JEI. The spells
 * are also switched off in config/irons_spellbooks_spell_config/somakespells, but the creative list is built on the
 * client before that config reaches it, so the scrolls would still be listed without this.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class BannedScrolls {
    private BannedScrolls() {
    }

    private static final Set<String> BANNED_SPELLS = Set.of(
            "somakespells:winged_ruin", "somakespells:crimson_reflection",
            "somakespells:incinerator_slash", "somakespells:ritual_flame",
            // Щит Игниса: заклинание есть только как высеченное на Оплоте Пламени, свитка в игре быть не должно
            "somakespells:ignis_shield");

    @SubscribeEvent
    static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        for (var entries : new Iterable[]{event.getParentEntries(), event.getSearchEntries()}) {
            var toRemove = new ArrayList<ItemStack>();
            for (Object o : entries) {
                ItemStack stack = (ItemStack) o;
                if (isBanned(stack)) toRemove.add(stack);
            }
            for (ItemStack stack : toRemove) {
                event.remove(stack, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    private static boolean isBanned(ItemStack stack) {
        if (!stack.is(ItemRegistry.SCROLL.get())) return false;
        try {
            if (!ISpellContainer.isSpellContainer(stack)) return false;
            var data = ISpellContainer.get(stack).getSpellAtIndex(0);
            return data != null && BANNED_SPELLS.contains(data.getSpell().getSpellId());
        } catch (RuntimeException e) {
            return false;
        }
    }
}
