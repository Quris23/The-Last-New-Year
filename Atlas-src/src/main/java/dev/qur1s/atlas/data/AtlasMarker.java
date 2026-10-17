package dev.qur1s.atlas.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.UUID;

/**
 * A marker on an Atlas: where it stands, its name, its icon, a description, and for flags the banner's colour
 * (a DyeColor id; -1 for the automatic bed marker).
 */
public record AtlasMarker(UUID id, int x, int y, int z, int color, String name, String icon, String description) {
    public static final String DEFAULT_ICON = "flag";
    /** The icons offered in the editor; "flag" draws the banner it came from, the others are the Atlas's own pictures. */
    public static final List<String> ICONS = List.of("flag", "house", "anchor", "skull", "star", "diamond", "sword",
            "chest", "portal", "tree", "dungeon", "monolith", "grave");
    /** Icons the Atlas draws itself for structures (and Rift Gates); "dungeon" stands for every structure without one. */
    public static final List<String> OWN_STRUCTURE_ICONS = List.of("dungeon", "stronghold", "pillager_outpost",
            "desert_pyramid", "igloo", "shipwreck", "ruins", "ruined_portal", "ancient_city", "bastion", "fortress",
            "end_city", "mineshaft", "nether_fossil", "trail_ruins", "gate", "monolith");
    /** Structure pictures taken from the game's own map icons ({@code textures/map/decorations/<key>.png}). */
    public static final List<String> VANILLA_ICONS = List.of("woodland_mansion", "ocean_monument", "red_x",
            "plains_village", "desert_village", "snowy_village", "taiga_village", "savanna_village", "jungle_temple",
            "swamp_hut", "trial_chambers");
    /** Colour value of markers the Atlas put there itself for structures. */
    public static final int COLOR_STRUCTURE = -2;
    /** Colour value of the marker put where the player died. */
    public static final int COLOR_DEATH = -3;

    public static boolean isKnownIcon(String icon) {
        return ICONS.contains(icon) || OWN_STRUCTURE_ICONS.contains(icon) || VANILLA_ICONS.contains(icon);
    }
    public static final int MAX_NAME = 48;
    public static final int MAX_DESCRIPTION = 400;

    public static final StreamCodec<RegistryFriendlyByteBuf, AtlasMarker> STREAM_CODEC = StreamCodec.of(
            (buf, m) -> {
                buf.writeUUID(m.id());
                buf.writeInt(m.x());
                buf.writeInt(m.y());
                buf.writeInt(m.z());
                buf.writeVarInt(m.color());
                buf.writeUtf(m.name(), MAX_NAME * 4);
                buf.writeUtf(m.icon(), 32);
                buf.writeUtf(m.description(), MAX_DESCRIPTION * 4);
            },
            buf -> new AtlasMarker(buf.readUUID(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readVarInt(),
                    buf.readUtf(MAX_NAME * 4), buf.readUtf(32), buf.readUtf(MAX_DESCRIPTION * 4)));

    /** A flag put on a banner. */
    public static AtlasMarker flag(UUID id, int x, int y, int z, int color, String name) {
        return new AtlasMarker(id, x, y, z, color, name, DEFAULT_ICON, "");
    }

    /** Cleans up what a client sent: bounded text, a known icon, sane coordinates. */
    public AtlasMarker sanitized() {
        String cleanName = name == null ? "" : name.length() > MAX_NAME ? name.substring(0, MAX_NAME) : name;
        String cleanDescription = description == null ? "" : description.length() > MAX_DESCRIPTION
                ? description.substring(0, MAX_DESCRIPTION) : description;
        String cleanIcon = isKnownIcon(icon) ? icon : DEFAULT_ICON;
        return new AtlasMarker(id, clamp(x, -30_000_000, 30_000_000), clamp(y, -2048, 4096),
                clamp(z, -30_000_000, 30_000_000), color, cleanName, cleanIcon, cleanDescription);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
