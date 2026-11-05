package dev.qur1s.spellclasses.implant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Sophisticated Backpacks upgrades that can be installed as Chromeware implants: which category (index of the Chromeware
 * ImplantCategory) and how much implant capacity each of them takes.
 */
public final class ImplantUpgrades {
    public static final int CIRCULATORY = 0;
    public static final int LEGS = 5;

    public record Rule(int category, int capacity) {
    }

    private static final Map<String, Rule> RULES = Map.ofEntries(
            Map.entry("tank_upgrade", new Rule(LEGS, 5)),
            Map.entry("xp_pump_upgrade", new Rule(CIRCULATORY, 2)),
            Map.entry("blood_pump_upgrade", new Rule(CIRCULATORY, 2)),
            Map.entry("mana_pump_upgrade", new Rule(CIRCULATORY, 2)),
            Map.entry("holographic_apple", new Rule(FaceImplants.CATEGORY, 12)));

    private ImplantUpgrades() {
    }

    /** The rule of an upgrade item, or null when the stack is not an implantable upgrade. */
    public static Rule rule(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (KSlots.isK(stack)) return new Rule(KSlots.CATEGORY, 4);
        if (FaceImplants.isMask(stack)) return new Rule(FaceImplants.CATEGORY, 7);
        if (FaceImplants.isGoggles(stack)) return new Rule(FaceImplants.CATEGORY, 5);
        if (!id.getNamespace().equals("sophisticatedbackpacks") && !id.getNamespace().equals("spellclasses")) return null;
        return RULES.get(id.getPath());
    }

    /** Chromeware's category key (as in its language file) of a category index. */
    public static String categoryKey(int category) {
        return category == LEGS ? "legs" : category == KSlots.CATEGORY ? "operating_system"
                : category == FaceImplants.CATEGORY ? "face" : "circulatory_system";
    }

    /** Real Sophisticated upgrades that work through the hidden backpack (not our blood and mana pumps, not the XP pump). */
    public static boolean forBackpack(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return rule(stack) != null && id.getNamespace().equals("sophisticatedbackpacks") && !isXpPump(stack);
    }

    /** The XP pump is not a backpack upgrade as an implant: it mends the held item with the player's own experience. */
    public static boolean isXpPump(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("sophisticatedbackpacks") && id.getPath().equals("xp_pump_upgrade");
    }

    /** Categories the upgrades are read from, in the order they fill the hidden backpack. */
    public static int[] categories() {
        return new int[]{CIRCULATORY, LEGS};
    }

    public static boolean installable(ItemStack stack) {
        return rule(stack) != null;
    }
}
