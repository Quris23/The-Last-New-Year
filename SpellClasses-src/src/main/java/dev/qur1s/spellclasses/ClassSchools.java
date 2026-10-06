package dev.qur1s.spellclasses;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The two schools everyone can always cast, and the pool of schools locked behind picking a class.
 * Iron's Spellbooks registers exactly 9 real schools (fire, ice, lightning, holy, ender, blood,
 * evocation, nature, eldritch) — "void" only exists as an unused translation key, no SchoolType.
 */
public final class ClassSchools {
    private ClassSchools() {
    }

    public static final Set<ResourceLocation> FREE_SCHOOLS = Set.of(
            SchoolRegistry.EVOCATION_RESOURCE,
            SchoolRegistry.ELDRITCH_RESOURCE
    );

    /**
     * Schools added by addons (Cataclysm: Spellbooks, Ace's Spell Utils) that everyone can cast
     * regardless of chosen class - not part of the class system, just extra spell schools.
     */
    public static final Set<ResourceLocation> ADDON_FREE_SCHOOLS = Set.of(
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "abyssal"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "technomancy"),
            ResourceLocation.fromNamespaceAndPath("cataclysm_spellbooks", "sand"),
            ResourceLocation.fromNamespaceAndPath("aces_spell_utils", "ritual"),
            ResourceLocation.fromNamespaceAndPath("aces_spell_utils", "hydro"),
            ResourceLocation.fromNamespaceAndPath("aces_spell_utils", "technomancy"),
            ResourceLocation.fromNamespaceAndPath("hazentouvelib", "cosmic")
    );

    /** Schools cut from the pack (Hazen's Touve Lib): not selectable as a class, items/recipes removed in KubeJS. */
    public static final Set<ResourceLocation> REMOVED_SCHOOLS = Set.of(
            ResourceLocation.fromNamespaceAndPath("hazentouvelib", "radiance"),
            ResourceLocation.fromNamespaceAndPath("hazentouvelib", "shadow")
    );

    /**
     * The Wind school (Wind's Spellbooks) is part of Storm: not a class of its own, castable by exactly
     * those who may cast Lightning, and (see WindSchoolIsStormMixin) it looks and scales like Lightning.
     */
    public static final ResourceLocation WIND_SCHOOL = ResourceLocation.fromNamespaceAndPath("wind_spellbooks", "wind");

    /** The Storm (Lightning) school to stand in for {@code id} when it is the folded-in Wind school, else null. */
    public static SchoolType stormFor(ResourceLocation id) {
        return WIND_SCHOOL.equals(id) ? SchoolRegistry.getSchool(SchoolRegistry.LIGHTNING_RESOURCE) : null;
    }

    /** Every school a player can pick as their one class. */
    public static Set<ResourceLocation> restrictedSchools() {
        LinkedHashSet<ResourceLocation> result = new LinkedHashSet<>();
        for (SchoolType school : SchoolRegistry.REGISTRY) {
            ResourceLocation id = school.getId();
            if (!FREE_SCHOOLS.contains(id) && !ADDON_FREE_SCHOOLS.contains(id)
                    && !REMOVED_SCHOOLS.contains(id) && !WIND_SCHOOL.equals(id)) result.add(id);
        }
        return result;
    }

    /** Our own class name for a school (e.g. "Вампир" for blood), distinct from the mod's own school name. */
    public static Component className(ResourceLocation school) {
        return Component.translatable("spellclasses.class." + school.getPath());
    }

    /** Whether {@code school} is castable by a player whose chosen class is {@code chosenSchool} (may be null/unset). */
    public static boolean isAllowed(ResourceLocation school, ResourceLocation chosenSchool) {
        if (WIND_SCHOOL.equals(school)) school = SchoolRegistry.LIGHTNING_RESOURCE;
        if (FREE_SCHOOLS.contains(school)) return true;
        if (!restrictedSchools().contains(school)) return true; // unknown/modded school: don't gate it
        return school.equals(chosenSchool);
    }
}
