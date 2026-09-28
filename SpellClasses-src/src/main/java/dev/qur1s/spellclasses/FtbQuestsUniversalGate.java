package dev.qur1s.spellclasses;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each universal (not class-gated) FTB Quests chapter to the item that unlocks its
 * visibility just by being obtained - no class pick required. Read by
 * {@link dev.qur1s.spellclasses.mixin.FtbQuestsChapterVisibilityMixin} (team-wide, same as class
 * chapters: visible if any online team member has obtained the item) and
 * {@link UniversalItemPickupEvents} (which marks the item obtained once a player holds it).
 */
public final class FtbQuestsUniversalGate {
    private FtbQuestsUniversalGate() {
    }

    private static final Map<Long, ResourceLocation> UNIVERSAL_CHAPTERS = new HashMap<>();

    static {
        // Abyssal - Book o' R'lyeh
        UNIVERSAL_CHAPTERS.put(Long.parseUnsignedLong("1664C24717D25B6D", 16), item("cataclysm_spellbooks", "abyss_spell_book"));
        // Sand - Desert Spellbook
        UNIVERSAL_CHAPTERS.put(Long.parseUnsignedLong("38F2814B10280935", 16), item("cataclysm_spellbooks", "desert_spell_book"));
    }

    private static ResourceLocation item(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /** Null if {@code chapterId} isn't one of the universal item-gated chapters. */
    public static ResourceLocation requiredItemFor(long chapterId) {
        return UNIVERSAL_CHAPTERS.get(chapterId);
    }

    public static Iterable<ResourceLocation> allRequiredItems() {
        return UNIVERSAL_CHAPTERS.values();
    }
}
