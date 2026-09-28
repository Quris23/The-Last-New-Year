package dev.qur1s.spellclasses.client;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Per-class presentation data for {@link ClassSelectionScreen}: accent color and grimoire icon. */
public final class ClassMeta {
    private ClassMeta() {
    }

    public record Entry(int color, ResourceLocation icon) {
    }

    /** Display order for the selection grid: 4 in the first row, 3 in the second. */
    public static final Map<String, Entry> BY_SCHOOL_PATH = new LinkedHashMap<>();

    static {
        BY_SCHOOL_PATH.put("fire", new Entry(0xFF6D3F, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "blaze_spell_book")));
        BY_SCHOOL_PATH.put("ice", new Entry(0x4FC3F7, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "ice_spell_book")));
        BY_SCHOOL_PATH.put("lightning", new Entry(0x7C4DFF, ResourceLocation.fromNamespaceAndPath("spellclasses", "storm_atlas")));
        BY_SCHOOL_PATH.put("nature", new Entry(0x66BB6A, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "druidic_spell_book")));
        BY_SCHOOL_PATH.put("ender", new Entry(0x9C27B0, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "dragonskin_spell_book")));
        BY_SCHOOL_PATH.put("blood", new Entry(0xB71C1C, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "cursed_doll_spell_book")));
        BY_SCHOOL_PATH.put("holy", new Entry(0xFFD54F, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "villager_spell_book")));
    }

    public static Entry get(ResourceLocation school) {
        return BY_SCHOOL_PATH.getOrDefault(school.getPath(), new Entry(0xAAAAAA, ResourceLocation.fromNamespaceAndPath("minecraft", "book")));
    }
}
