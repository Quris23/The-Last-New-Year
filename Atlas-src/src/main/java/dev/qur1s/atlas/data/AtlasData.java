package dev.qur1s.atlas.data;

import dev.qur1s.atlas.StructureIcons;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Every Atlas's map sheets, keyed by the atlas id, then dimension, then (scale, centre) of the sheet. Lives in the overworld's data storage. */
public final class AtlasData extends SavedData {
    private static final String NAME = "atlas_data";

    public static final int MAX_MARKERS = 1024;

    private final Map<UUID, Map<String, Map<AtlasSheet.Key, AtlasSheet>>> atlases = new HashMap<>();
    private final Map<UUID, Map<String, List<AtlasMarker>>> markers = new HashMap<>();
    private long nextSeq;

    public static AtlasData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(AtlasData::new, AtlasData::load, null), NAME);
    }

    public Map<AtlasSheet.Key, AtlasSheet> sheets(UUID atlasId, String dimension) {
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> dims = atlases.get(atlasId);
        if (dims == null) return Map.of();
        return dims.getOrDefault(dimension, Map.of());
    }

    /** The dimensions this atlas has any map or flags in. */
    public List<String> dimensions(UUID atlasId) {
        java.util.Set<String> result = new java.util.TreeSet<>();
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> tiles = atlases.get(atlasId);
        if (tiles != null) tiles.forEach((dim, map) -> { if (!map.isEmpty()) result.add(dim); });
        Map<String, List<AtlasMarker>> flags = markers.get(atlasId);
        if (flags != null) flags.forEach((dim, list) -> { if (!list.isEmpty()) result.add(dim); });
        return new ArrayList<>(result);
    }

    /** Puts a whole sheet in, over what the Atlas already has at the same scale and place (its pixels win where it has any). */
    public AtlasSheet putSheet(UUID atlasId, String dimension, int scale, int centreX, int centreZ, byte[] colors) {
        Map<AtlasSheet.Key, AtlasSheet> sheets = atlases.computeIfAbsent(atlasId, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new HashMap<>());
        AtlasSheet incoming = new AtlasSheet(scale, centreX, centreZ, colors.clone());
        AtlasSheet mine = sheets.get(incoming.key());
        if (mine == null) {
            sheets.put(incoming.key(), incoming);
            mine = incoming;
        } else {
            mine.overlay(incoming);
        }
        setDirty();
        return mine;
    }

    /** Writes one 16x16 chunk of terrain colours (one byte per block, x fastest) into the finest sheet at that place. */
    public void putChunk(UUID atlasId, String dimension, int chunkX, int chunkZ, byte[] colors) {
        Map<AtlasSheet.Key, AtlasSheet> sheets = atlases.computeIfAbsent(atlasId, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new HashMap<>());
        int sheetX = Math.floorDiv(chunkX, 8), sheetZ = Math.floorDiv(chunkZ, 8);
        AtlasSheet.Key key = new AtlasSheet.Key(0, sheetX * AtlasSheet.SIZE + 64, sheetZ * AtlasSheet.SIZE + 64);
        AtlasSheet sheet = sheets.computeIfAbsent(key, k -> new AtlasSheet(k.scale(), k.centreX(), k.centreZ()));
        int baseX = Math.floorMod(chunkX, 8) * 16, baseZ = Math.floorMod(chunkZ, 8) * 16;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                sheet.colors[baseX + x + (baseZ + z) * AtlasSheet.SIZE] = colors[x + z * 16];
            }
        }
        sheet.invalidate();
        setDirty();
    }

    /** Adds everything atlas {@code from} knows to atlas {@code to}: the finer terrain wins, markers are joined. */
    public void mergeAtlas(UUID from, UUID to) {
        if (from.equals(to)) return;
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> source = atlases.get(from);
        if (source != null) {
            Map<String, Map<AtlasSheet.Key, AtlasSheet>> target = atlases.computeIfAbsent(to, k -> new HashMap<>());
            source.forEach((dimension, sheets) -> {
                Map<AtlasSheet.Key, AtlasSheet> into = target.computeIfAbsent(dimension, k -> new HashMap<>());
                sheets.forEach((key, sheet) -> {
                    AtlasSheet mine = into.get(key);
                    if (mine == null) {
                        AtlasSheet copy = new AtlasSheet(sheet.scale, sheet.centreX, sheet.centreZ, sheet.colors.clone());
                        copy.seq = sheet.seq;
                        into.put(key, copy);
                    } else {
                        mine.overlay(sheet);
                        mine.seq = Math.max(mine.seq, sheet.seq);
                    }
                });
            });
        }
        Map<String, List<AtlasMarker>> flags = markers.get(from);
        if (flags != null) {
            flags.forEach((dimension, list) -> {
                for (AtlasMarker marker : list) {
                    List<AtlasMarker> mine = markers.computeIfAbsent(to, k -> new HashMap<>())
                            .computeIfAbsent(dimension, k -> new ArrayList<>());
                    boolean same = false;
                    for (AtlasMarker m : mine) {
                        if (m.id().equals(marker.id()) || (m.x() == marker.x() && m.y() == marker.y() && m.z() == marker.z())) {
                            same = true;
                            break;
                        }
                    }
                    if (!same && mine.size() < MAX_MARKERS) mine.add(marker);
                }
            });
        }
        setDirty();
    }

    /** Keeps only the {@code keep} newest markers of this colour in the dimension; the older ones are removed. */
    public void trimMarkers(UUID atlasId, String dimension, int color, int keep) {
        Map<String, List<AtlasMarker>> dims = markers.get(atlasId);
        if (dims == null) return;
        List<AtlasMarker> list = dims.get(dimension);
        if (list == null) return;
        int count = 0;
        for (AtlasMarker m : list) if (m.color() == color) count++;
        // the list is in the order they were added, so the oldest come first
        for (java.util.Iterator<AtlasMarker> it = list.iterator(); it.hasNext() && count > keep; ) {
            if (it.next().color() == color) {
                it.remove();
                count--;
            }
        }
        setDirty();
    }

    /** Whether any sheet of the atlas has something drawn at this block. */
    public boolean isKnown(UUID atlasId, String dimension, int x, int z) {
        for (AtlasSheet sheet : sheets(atlasId, dimension).values()) {
            int half = sheet.blocks() / 2;
            int bx = x - (sheet.centreX - half), bz = z - (sheet.centreZ - half);
            if (bx < 0 || bz < 0 || bx >= sheet.blocks() || bz >= sheet.blocks()) continue;
            if (((sheet.colors[(bx >> sheet.scale) + (bz >> sheet.scale) * AtlasSheet.SIZE] & 0xFF) >> 2) != 0) return true;
        }
        return false;
    }

    /** Whether the atlas has no map sheets and no markers at all (or does not exist yet). */
    public boolean isEmpty(UUID atlasId) {
        return chunkCount(atlasId) < 0;
    }

    /** About how many 16x16 chunks are drawn on the atlas's sheets, or -1 if it has neither sheets nor markers. */
    public int chunkCount(UUID atlasId) {
        int count = 0;
        boolean any = false;
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> dims = atlases.get(atlasId);
        if (dims != null) {
            for (Map<AtlasSheet.Key, AtlasSheet> sheets : dims.values()) {
                if (!sheets.isEmpty()) any = true;
                for (AtlasSheet sheet : sheets.values()) count += sheet.coverage();
            }
        }
        Map<String, List<AtlasMarker>> flags = markers.get(atlasId);
        if (flags != null) for (List<AtlasMarker> list : flags.values()) if (!list.isEmpty()) any = true;
        return any ? count : -1;
    }

    /** Removes the structure markers of types the config says to leave off the atlas (old ones from before it said so). */
    public void purgeIgnored(UUID atlasId, java.util.function.Predicate<String> ignored) {
        Map<String, List<AtlasMarker>> dims = markers.get(atlasId);
        if (dims == null) return;
        boolean changed = false;
        for (List<AtlasMarker> list : dims.values()) {
            changed |= list.removeIf(m -> m.color() == AtlasMarker.COLOR_STRUCTURE && ignored.test(m.name()));
        }
        if (changed) setDirty();
    }

    /** The map that was poured into the atlas last, with the dimension it is in, or null. */
    public record LastMap(String dimension, AtlasSheet sheet) {
    }

    /**
     * The next map to take out of the atlas: the one poured in last; once those are all out, a sheet in the given
     * dimension nearest to {@code (x, z)} (the ones made by "load the whole map").
     */
    public LastMap lastMap(UUID atlasId, String dimension, int x, int z) {
        LastMap best = null;
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> dims = atlases.get(atlasId);
        if (dims == null) return null;
        for (var dim : dims.entrySet()) {
            for (AtlasSheet sheet : dim.getValue().values()) {
                if (sheet.seq > 0 && (best == null || sheet.seq > best.sheet().seq)) best = new LastMap(dim.getKey(), sheet);
            }
        }
        if (best != null) return best;
        Map<AtlasSheet.Key, AtlasSheet> here = dims.get(dimension);
        if (here == null) return null;
        long bestDistance = Long.MAX_VALUE;
        for (AtlasSheet sheet : here.values()) {
            long dx = sheet.centreX - x, dz = sheet.centreZ - z;
            long distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = new LastMap(dimension, sheet);
            }
        }
        return best;
    }

    /** Every sheet of the dimension that covers the block (x, z), whatever its scale. */
    public java.util.List<AtlasSheet> sheetsAt(UUID atlasId, String dimension, int x, int z) {
        java.util.List<AtlasSheet> result = new ArrayList<>();
        for (AtlasSheet sheet : sheets(atlasId, dimension).values()) {
            int half = sheet.blocks() / 2;
            if (x >= sheet.centreX - half && x < sheet.centreX + half && z >= sheet.centreZ - half && z < sheet.centreZ + half) result.add(sheet);
        }
        return result;
    }

    /** The smallest-scale sheet of the dimension that covers the block (x, z), or null. */
    public AtlasSheet finestSheetAt(UUID atlasId, String dimension, int x, int z) {
        AtlasSheet best = null;
        for (AtlasSheet sheet : sheets(atlasId, dimension).values()) {
            int half = sheet.blocks() / 2;
            if (x < sheet.centreX - half || x >= sheet.centreX + half || z < sheet.centreZ - half || z >= sheet.centreZ + half) continue;
            if (best == null || sheet.scale < best.scale) best = sheet;
        }
        return best;
    }

    /** Takes the sheet whose map item has this id out of the atlas again; returns whether there was one. */
    public boolean removeMapSheet(UUID atlasId, int mapId) {
        Map<String, Map<AtlasSheet.Key, AtlasSheet>> dims = atlases.get(atlasId);
        if (dims == null) return false;
        for (Map<AtlasSheet.Key, AtlasSheet> sheets : dims.values()) {
            for (var it = sheets.values().iterator(); it.hasNext(); ) {
                AtlasSheet sheet = it.next();
                if (sheet.pendingMapId == mapId) {
                    it.remove();
                    setDirty();
                    return true;
                }
            }
        }
        return false;
    }

    /** Forgets an atlas's data (after it was merged into another one). */
    public void forget(UUID atlasId) {
        atlases.remove(atlasId);
        markers.remove(atlasId);
        setDirty();
    }

    public List<AtlasMarker> markers(UUID atlasId, String dimension) {
        Map<String, List<AtlasMarker>> dims = markers.get(atlasId);
        if (dims == null) return List.of();
        return dims.getOrDefault(dimension, List.of());
    }

    /** Adds a flag unless one already stands on that block or the atlas is full; returns whether it was added. */
    public boolean addMarker(UUID atlasId, String dimension, AtlasMarker marker) {
        List<AtlasMarker> list = markers.computeIfAbsent(atlasId, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new ArrayList<>());
        if (list.size() >= MAX_MARKERS) return false;
        for (AtlasMarker existing : list) {
            if (existing.x() == marker.x() && existing.y() == marker.y() && existing.z() == marker.z()) return false;
        }
        list.add(marker);
        setDirty();
        return true;
    }

    /** Adds a marker the Atlas found by itself unless a marker with the same icon is already close by. */
    public boolean addAutoMarker(UUID atlasId, String dimension, AtlasMarker marker, int radius) {
        List<AtlasMarker> list = markers.computeIfAbsent(atlasId, k -> new HashMap<>())
                .computeIfAbsent(dimension, k -> new ArrayList<>());
        for (AtlasMarker existing : list) {
            if (existing.icon().equals(marker.icon())
                    && Math.abs(existing.x() - marker.x()) <= radius && Math.abs(existing.z() - marker.z()) <= radius) {
                return false;
            }
        }
        return addMarker(atlasId, dimension, marker);
    }

    /** Replaces the marker with the same id by an edited copy; returns whether it was found. */
    public boolean updateMarker(UUID atlasId, String dimension, AtlasMarker edited) {
        Map<String, List<AtlasMarker>> dims = markers.get(atlasId);
        if (dims == null) return false;
        List<AtlasMarker> list = dims.get(dimension);
        if (list == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(edited.id())) {
                AtlasMarker clean = edited.sanitized();
                list.set(i, new AtlasMarker(clean.id(), clean.x(), clean.y(), clean.z(), list.get(i).color(),
                        clean.name(), clean.icon(), clean.description()));
                setDirty();
                return true;
            }
        }
        return false;
    }

    public boolean removeMarker(UUID atlasId, String dimension, UUID markerId) {
        Map<String, List<AtlasMarker>> dims = markers.get(atlasId);
        if (dims == null) return false;
        List<AtlasMarker> list = dims.get(dimension);
        if (list != null && list.removeIf(m -> m.id().equals(markerId))) {
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * Pours everything drawn on a vanilla map into an atlas. Pixel (px, py) of a map of scale s covers the blocks
     * [centerX + (px - 64) * 2^s, +2^s) on x (likewise z); a pixel with colour id 0 was never explored and is skipped.
     */
        public int merge(UUID atlasId, MapItemSavedData map) {
        String dimension = map.dimension.location().toString();
        int size = 1 << map.scale;
        // the map itself becomes a sheet (a map poured again updates its own sheet)
        AtlasSheet sheet = putSheet(atlasId, dimension, map.scale, map.centerX, map.centerZ, map.colors);
        sheet.seq = ++nextSeq;        // the newest poured map
        int changed = sheet.coverage();
        for (var banner : map.getBanners()) {
            var pos = banner.pos();
            String name = banner.name().map(Component::getString).orElse("");
            if (addMarker(atlasId, dimension, AtlasMarker.flag(UUID.randomUUID(), pos.getX(), pos.getY(), pos.getZ(),
                    banner.color().getId(), name))) {
                changed++;
            }
        }
        // the pictures an explorer map carries (ocean monument, woodland mansion, treasure X, villages, ...)
        for (var decoration : map.getDecorations()) {
            var type = decoration.type().value();
            if (!type.explorationMapElement()) continue;
            String icon = StructureIcons.iconForDecoration(type.assetId().getPath());
            if (icon == null) continue;
            int worldX = map.centerX + Math.round(decoration.x() * size / 2.0f);
            int worldZ = map.centerZ + Math.round(decoration.y() * size / 2.0f);
            String name = decoration.name().map(Component::getString).orElse(type.assetId().getPath());
            if (addAutoMarker(atlasId, dimension, new AtlasMarker(UUID.randomUUID(), worldX, 64, worldZ,
                    AtlasMarker.COLOR_STRUCTURE, name, icon, ""), 48)) {
                changed++;
            }
        }
        if (changed > 0) setDirty();
        return changed;
    }

    public static AtlasData load(CompoundTag root, HolderLookup.Provider registries) {
        AtlasData data = new AtlasData();
        data.nextSeq = root.getLong("seq");
        // (the older format, terrain drawn from the world in tiles, is not read: the Atlases start over with sheets)
        ListTag atlasList = root.getList("sheetAtlases", Tag.TAG_COMPOUND);
        for (int i = 0; i < atlasList.size(); i++) {
            CompoundTag atlasTag = atlasList.getCompound(i);
            UUID id = atlasTag.getUUID("id");
            Map<String, Map<AtlasSheet.Key, AtlasSheet>> dims = new HashMap<>();
            ListTag dimList = atlasTag.getList("dims", Tag.TAG_COMPOUND);
            for (int j = 0; j < dimList.size(); j++) {
                CompoundTag dimTag = dimList.getCompound(j);
                Map<AtlasSheet.Key, AtlasSheet> sheets = new HashMap<>();
                ListTag sheetList = dimTag.getList("sheets", Tag.TAG_COMPOUND);
                for (int k = 0; k < sheetList.size(); k++) {
                    CompoundTag t = sheetList.getCompound(k);
                    byte[] colors = t.getByteArray("c");
                    if (colors.length != AtlasSheet.AREA) continue;
                    AtlasSheet sheet = new AtlasSheet(t.getInt("s"), t.getInt("x"), t.getInt("z"), colors);
                    sheet.seq = t.getLong("q");
                    sheets.put(sheet.key(), sheet);
                }
                dims.put(dimTag.getString("dim"), sheets);
            }
            data.atlases.put(id, dims);
        }
        ListTag markerList = root.getList("markers", Tag.TAG_COMPOUND);
        for (int i = 0; i < markerList.size(); i++) {
            CompoundTag m = markerList.getCompound(i);
            data.markers.computeIfAbsent(m.getUUID("atlas"), k -> new HashMap<>())
                    .computeIfAbsent(m.getString("dim"), k -> new ArrayList<>())
                    .add(new AtlasMarker(m.getUUID("id"), m.getInt("x"), m.getInt("y"), m.getInt("z"),
                            m.getInt("color"), m.getString("name"),
                            m.contains("icon") ? m.getString("icon") : AtlasMarker.DEFAULT_ICON, m.getString("description")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag root, HolderLookup.Provider registries) {
        ListTag atlasList = new ListTag();
        atlases.forEach((id, dims) -> {
            CompoundTag atlasTag = new CompoundTag();
            atlasTag.putUUID("id", id);
            ListTag dimList = new ListTag();
            dims.forEach((dim, sheets) -> {
                CompoundTag dimTag = new CompoundTag();
                dimTag.putString("dim", dim);
                ListTag sheetList = new ListTag();
                sheets.forEach((key, sheet) -> {
                    CompoundTag t = new CompoundTag();
                    t.putInt("s", sheet.scale);
                    t.putInt("x", sheet.centreX);
                    t.putInt("z", sheet.centreZ);
                    t.putByteArray("c", sheet.colors);
                    t.putLong("q", sheet.seq);
                    sheetList.add(t);
                });
                dimTag.put("sheets", sheetList);
                dimList.add(dimTag);
            });
            atlasTag.put("dims", dimList);
            atlasList.add(atlasTag);
        });
        root.put("sheetAtlases", atlasList);
        root.putLong("seq", nextSeq);
        ListTag markerList = new ListTag();
        markers.forEach((atlas, dims) -> dims.forEach((dim, list) -> list.forEach(marker -> {
            CompoundTag m = new CompoundTag();
            m.putUUID("atlas", atlas);
            m.putString("dim", dim);
            m.putUUID("id", marker.id());
            m.putInt("x", marker.x());
            m.putInt("y", marker.y());
            m.putInt("z", marker.z());
            m.putInt("color", marker.color());
            m.putString("name", marker.name());
            m.putString("icon", marker.icon());
            m.putString("description", marker.description());
            markerList.add(m);
        })));
        root.put("markers", markerList);
        return root;
    }
}
