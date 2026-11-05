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

    /** Stored in the obtained set when the player has held an item with an Eldritch-school spell. */
    public static final ResourceLocation ELDRITCH_MARKER = ResourceLocation.fromNamespaceAndPath("spellclasses", "eldritch_spell");

    private static final Map<Long, ResourceLocation> UNIVERSAL_CHAPTERS = new HashMap<>();

    static {
        // Abyssal (Чародей Бездны) is no longer universal: it moved into the Aqua class (see FtbQuestsClassGate)
        // Sand is no longer universal either: its quests moved into the Druid (nature) chapter; Sand spells stay
        // castable by everyone in the full Pharaoh set (see AbstractSpellOverridesMixin)
        // Eldritch - not an item: the chapter opens once the player has held anything carrying an Eldritch spell
        // (see UniversalItemPickupEvents), recorded under this marker id
        UNIVERSAL_CHAPTERS.put(Long.parseUnsignedLong("3F788E6395D396F6", 16), ELDRITCH_MARKER);
    }

    private static ResourceLocation item(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    /** True for the universal chapters (Eldritch): shown only once a quest in them is completed. */
    public static boolean isUniversalChapter(long chapterId) {
        return UNIVERSAL_CHAPTERS.containsKey(chapterId);
    }

    /** Null if {@code chapterId} isn't one of the universal item-gated chapters. */
    public static ResourceLocation requiredItemFor(long chapterId) {
        return UNIVERSAL_CHAPTERS.get(chapterId);
    }

    public static Iterable<ResourceLocation> allRequiredItems() {
        return UNIVERSAL_CHAPTERS.values();
    }
}
