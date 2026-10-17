package dev.qur1s.atlas;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AtlasConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_OTHER_PLAYERS;
    public static final ModConfigSpec.BooleanValue SHOW_RIFT_GATES;
    public static final ModConfigSpec.BooleanValue SHOW_TRAINS;
    public static final ModConfigSpec.BooleanValue SHOW_TRACKS;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> IGNORED_STRUCTURES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SHOW_OTHER_PLAYERS = builder.comment("Show the other players (their heads) in the same dimension on the Atlas")
                .define("showOtherPlayers", true);
        SHOW_RIFT_GATES = builder.comment("Show the AeroWarptics Rift Gates the player may use (needs AeroWarptics)")
                .define("showRiftGates", true);
        SHOW_TRAINS = builder.comment("Show Create trains (an engine icon with the train's name) on the Atlas (needs Create)")
                .define("showTrains", true);
        SHOW_TRACKS = builder.comment("Show Create tracks on the explored part of the Atlas (needs Create)")
                .define("showTracks", true);
        IGNORED_STRUCTURES = builder.comment("Structures that do not get a marker on the Atlas. An entry may end with * to match a whole group.")
                .defineListAllowEmpty("ignoredStructures", java.util.List.of("minecraft:ruined_portal*", "minecraft:ocean_ruin_*",
                        "minecraft:nether_fossil", "towns_and_towers:wreckage_ocean", "supplementaries:road_sign", "minecraft:shipwreck_beached"), () -> "minecraft:example", o -> o instanceof String);
        SPEC = builder.build();
    }

    private AtlasConfig() {
    }
}
