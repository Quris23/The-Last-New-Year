package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasMarker;

import java.util.Map;

/** Which picture a structure gets on the Atlas; any structure not listed here (all modded ones) gets the dungeon. */
public final class StructureIcons {
    private static final Map<String, String> BY_PATH = Map.ofEntries(
            Map.entry("village_plains", "plains_village"),
            Map.entry("village_desert", "desert_village"),
            Map.entry("village_savanna", "savanna_village"),
            Map.entry("village_snowy", "snowy_village"),
            Map.entry("village_taiga", "taiga_village"),
            Map.entry("mansion", "woodland_mansion"),
            Map.entry("monument", "ocean_monument"),
            Map.entry("jungle_pyramid", "jungle_temple"),
            Map.entry("swamp_hut", "swamp_hut"),
            Map.entry("trial_chambers", "trial_chambers"),
            Map.entry("buried_treasure", "red_x"),
            Map.entry("stronghold", "stronghold"),
            Map.entry("pillager_outpost", "pillager_outpost"),
            Map.entry("desert_pyramid", "desert_pyramid"),
            Map.entry("igloo", "igloo"),
            Map.entry("shipwreck", "shipwreck"),
            Map.entry("shipwreck_beached", "shipwreck"),
            Map.entry("ocean_ruin_cold", "ruins"),
            Map.entry("ocean_ruin_warm", "ruins"),
            Map.entry("ruined_portal", "ruined_portal"),
            Map.entry("ruined_portal_desert", "ruined_portal"),
            Map.entry("ruined_portal_jungle", "ruined_portal"),
            Map.entry("ruined_portal_mountain", "ruined_portal"),
            Map.entry("ruined_portal_nether", "ruined_portal"),
            Map.entry("ruined_portal_ocean", "ruined_portal"),
            Map.entry("ruined_portal_swamp", "ruined_portal"),
            Map.entry("ancient_city", "ancient_city"),
            Map.entry("bastion_remnant", "bastion"),
            Map.entry("fortress", "fortress"),
            Map.entry("end_city", "end_city"),
            Map.entry("mineshaft", "mineshaft"),
            Map.entry("mineshaft_mesa", "mineshaft"),
            Map.entry("nether_fossil", "nether_fossil"),
            Map.entry("trail_ruins", "trail_ruins"));

    private StructureIcons() {
    }

    /** The icon for a structure registry id like {@code minecraft:village_plains}. */
    public static String iconFor(String structureId) {
        if (structureId.startsWith("minecraft:")) {
            String icon = BY_PATH.get(structureId.substring("minecraft:".length()));
            if (icon != null) return icon;
        }
        if (structureId.startsWith("ancient_remnants:") && structureId.endsWith("_monolith")) return "monolith";
        return "dungeon";
    }

    /** The icon for a vanilla map decoration (the picture on an explorer map), or null if it is not a structure one. */
    public static String iconForDecoration(String assetPath) {
        return AtlasMarker.VANILLA_ICONS.contains(assetPath) ? assetPath : null;
    }
}
