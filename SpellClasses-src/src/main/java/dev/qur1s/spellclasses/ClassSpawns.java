package dev.qur1s.spellclasses;

import com.mojang.datafixers.util.Pair;
import dev.qur1s.spellclasses.data.ClassSpawnData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Each class has a place in the overworld where its members begin: Infernal at a ruined portal, Frost on ice peaks,
 * Druid in a swamp, Storm on a sea shore, Vampire on open ground, Priest in a village. A place is looked for once (nearest
 * to the world spawn) and remembered; players are taken there when they choose their class. Ender has no place yet:
 * its structure is built by hand and set with "/spellclasses spawn set ender".
 */
public final class ClassSpawns {
    private ClassSpawns() {
    }

    private static final int STRUCTURE_RADIUS_CHUNKS = 200;
    private static final int BIOME_RADIUS = 8192;

    private static final String[] RUINED_PORTALS = {"ruined_portal", "ruined_portal_desert", "ruined_portal_jungle",
            "ruined_portal_mountain", "ruined_portal_swamp"};

    /** The remembered spawn of {@code school}, searching for it the first time. Empty if it cannot be found. */
    public static Optional<BlockPos> spawnFor(ServerLevel overworld, ResourceLocation school) {
        ClassSpawnData data = ClassSpawnData.get(overworld);
        Optional<BlockPos> known = data.get(school.toString());
        // the Vampire used to begin in a cave: a remembered underground spot is dropped and looked for again on the surface
        if (known.isPresent() && "blood".equals(school.getPath()) && !overworld.canSeeSky(known.get())) known = Optional.empty();
        if (known.isPresent()) return known;
        Optional<BlockPos> found = locate(overworld, school.getPath());
        found.ifPresent(pos -> data.set(school.toString(), pos));
        return found;
    }

    /** Takes {@code player} to their class spawn (a no-op with a notice if the class has none yet). */
    public static void teleport(ServerPlayer player, ResourceLocation school) {
        ServerLevel overworld = player.server.overworld();
        Optional<BlockPos> spawn = spawnFor(overworld, school);
        if (spawn.isEmpty()) {
            player.sendSystemMessage(Component.translatable("spellclasses.spawn.none", ClassSchools.className(school)));
            return;
        }
        BlockPos pos = spawn.get();
        player.teleportTo(overworld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0f);
        player.sendSystemMessage(Component.translatable("spellclasses.spawn.teleported", ClassSchools.className(school)));
    }

    private static Optional<BlockPos> locate(ServerLevel level, String path) {
        BlockPos origin = level.getSharedSpawnPos();
        switch (path) {
            case "fire":
                return structure(level, origin, ruinedPortals(level));
            case "holy":
                return structure(level, origin, level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .getOrCreateTag(StructureTags.VILLAGE));
            case "ice":
                return biome(level, origin, holder -> isAny(holder, "ice_spikes", "frozen_peaks", "jagged_peaks")
                        || holder.is(Tags.Biomes.IS_ICY) && holder.is(BiomeTags.IS_MOUNTAIN), false);
            case "nature":
                return biome(level, origin, holder -> isAny(holder, "swamp", "mangrove_swamp")
                        || holder.is(Tags.Biomes.IS_SWAMP), false);
            case "lightning":
                return coast(level, origin);
            case "aqua":
                return openSea(level, origin);
            case "blood":
                return randomSurface(level, origin);
            case "ender":
                // not built automatically: the observatory is placed by hand and set with "/spellclasses spawn set ender"
                return Optional.empty();
            default:
                return Optional.empty();
        }
    }

    // ---- the Ender class's structure -------------------------------------------------------------

    public static final ResourceLocation OBSERVATORY = ResourceLocation.fromNamespaceAndPath("spellclasses", "abandoned_observatory");
    private static final int OBSERVATORY_X = 48, OBSERVATORY_Z = 50;
    /** Where players stand in it (next to the Altar of Resurrection), from its low corner. */
    private static final BlockPos OBSERVATORY_SPAWN = new BlockPos(23, 2, 37);

    /**
     * Builds the Abandoned Observatory on level ground a few hundred blocks from the world spawn (the first time the
     * Ender class is chosen) and returns where its members stand.
     */
    private static Optional<BlockPos> buildObservatory(ServerLevel level, BlockPos origin) {
        var log = com.mojang.logging.LogUtils.getLogger();
        int sea = level.getSeaLevel();
        for (int radius : new int[]{400, 700, 1000, 1500, 2200}) {
            for (int k = 0; k < 12; k++) {
                double angle = k * Math.PI / 6;
                int cx = origin.getX() + (int) (Math.cos(angle) * radius);
                int cz = origin.getZ() + (int) (Math.sin(angle) * radius);
                int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
                boolean ok = true;
                for (int dx = -OBSERVATORY_X / 2; dx <= OBSERVATORY_X / 2 && ok; dx += 12) {
                    for (int dz = -OBSERVATORY_Z / 2; dz <= OBSERVATORY_Z / 2 && ok; dz += 12) {
                        int x = cx + dx, z = cz + dz;
                        // the generator's noise height: no chunk is generated just to be rejected as hilly
                        var gen = level.getChunkSource().getGenerator();
                        var rs = level.getChunkSource().randomState();
                        int y = gen.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, rs);
                        int top = gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, rs);
                        if (top > y || y < sea + 2 || y > sea + 70) ok = false;
                        low = Math.min(low, y);
                        high = Math.max(high, y);
                    }
                }
                if (!ok || high - low > 5) continue;
                BlockPos corner = new BlockPos(cx - OBSERVATORY_X / 2, low - 1, cz - OBSERVATORY_Z / 2);
                if (place(level, corner)) {
                    log.info("[SpellClasses] Abandoned Observatory built at {}", corner.toShortString());
                    return Optional.of(corner.offset(OBSERVATORY_SPAWN));
                }
                return Optional.empty();
            }
        }
        log.info("[SpellClasses] no level ground found for the Abandoned Observatory");
        return Optional.empty();
    }

    /** Places the observatory with its low corner at {@code corner}; false if the structure file is missing. */
    public static boolean place(ServerLevel level, BlockPos corner) {
        var template = level.getStructureManager().get(OBSERVATORY);
        if (template.isEmpty()) return false;
        template.get().placeInWorld(level, corner, corner,
                new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(), level.random, 2);
        return true;
    }

    /** The Ender spawn of an observatory built with its low corner at {@code corner}. */
    public static BlockPos observatorySpawn(BlockPos corner) {
        return corner.offset(OBSERVATORY_SPAWN);
    }

    public static BlockPos observatoryCorner(BlockPos center) {
        return center.offset(-OBSERVATORY_X / 2, -1, -OBSERVATORY_Z / 2);
    }

    private static boolean isAny(Holder<Biome> holder, String... names) {
        for (String name : names) {
            if (holder.is(ResourceLocation.withDefaultNamespace(name))) return true;
        }
        return false;
    }

    private static HolderSet<Structure> ruinedPortals(ServerLevel level) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> holders = new ArrayList<>();
        for (String name : RUINED_PORTALS) {
            registry.getHolder(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace(name)))
                    .ifPresent(holders::add);
        }
        return HolderSet.direct(holders);
    }

    private static Optional<BlockPos> structure(ServerLevel level, BlockPos origin, HolderSet<Structure> structures) {
        Pair<BlockPos, Holder<Structure>> result = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, structures, origin, STRUCTURE_RADIUS_CHUNKS, false);
        return result == null ? Optional.empty() : surfaceNear(level, result.getFirst(), pos -> true);
    }

    /**
     * A shore, judged by the ground that is really there: with the pack's world generators a column may be tagged an
     * ocean biome while its terrain is plains. Candidate seas come from the biome search and from points on a ring of
     * compass directions; a candidate counts only if its terrain holds deep water.
     */
    private static Optional<BlockPos> coast(ServerLevel level, BlockPos origin) {
        var log = com.mojang.logging.LogUtils.getLogger();
        List<BlockPos> candidates = new ArrayList<>();
        Pair<BlockPos, Holder<Biome>> ocean = level.findClosestBiome3d(holder -> holder.is(BiomeTags.IS_OCEAN),
                origin, BIOME_RADIUS, 32, 64);
        if (ocean != null) candidates.add(ocean.getFirst());
        for (int radius : new int[]{500, 1000, 2000, 3500}) {
            for (int k = 0; k < 8; k++) {
                double angle = k * Math.PI / 4;
                candidates.add(origin.offset((int) (Math.cos(angle) * radius), 0, (int) (Math.sin(angle) * radius)));
            }
        }
        int tried = 0;
        for (BlockPos candidate : candidates) {
            if (!deepWater(level, candidate.getX(), candidate.getZ())) continue;
            if (++tried > 5) break;
            Optional<BlockPos> shore = shoreNear(level, candidate);
            log.info("[SpellClasses] coast: sea at {} -> shore {}", candidate.toShortString(), shore.map(BlockPos::toShortString).orElse("none"));
            if (shore.isPresent()) return shore;
        }
        log.info("[SpellClasses] coast: no shore found ({} real seas checked among {} candidates)", tried, candidates.size());
        return Optional.empty();
    }

    /** A spot a few blocks under the surface of a real sea (the Aqua class begins in the water, not on a shore). */
    private static Optional<BlockPos> openSea(ServerLevel level, BlockPos origin) {
        List<BlockPos> candidates = new ArrayList<>();
        Pair<BlockPos, Holder<Biome>> ocean = level.findClosestBiome3d(holder -> holder.is(BiomeTags.IS_OCEAN),
                origin, BIOME_RADIUS, 32, 64);
        if (ocean != null) candidates.add(ocean.getFirst());
        for (int radius : new int[]{500, 1000, 2000, 3500}) {
            for (int k = 0; k < 8; k++) {
                double angle = k * Math.PI / 4;
                candidates.add(origin.offset((int) (Math.cos(angle) * radius), 0, (int) (Math.sin(angle) * radius)));
            }
        }
        for (BlockPos candidate : candidates) {
            if (!deepWater(level, candidate.getX(), candidate.getZ())) continue;
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, candidate.getX(), candidate.getZ());
            return Optional.of(new BlockPos(candidate.getX(), surface - 3, candidate.getZ()));
        }
        return Optional.empty();
    }

    /** Whether the terrain at x/z really is sea: water over a floor at least four blocks down. */
    private static boolean deepWater(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        return surface - floor >= 4 && level.getFluidState(new BlockPos(x, surface - 1, z)).is(net.minecraft.tags.FluidTags.WATER);
    }

    /** Dry ground at sea level with open water beside it, searched outwards from a point in the sea. */
    private static Optional<BlockPos> shoreNear(ServerLevel level, BlockPos sea) {
        int seaLevel = level.getSeaLevel();
        for (int ring = 0; ring <= 96; ring += 3) {
            for (int dx = -ring; dx <= ring; dx += 3) {
                for (int dz = -ring; dz <= ring; dz += 3) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    int x = sea.getX() + dx, z = sea.getZ() + dz;
                    level.getChunk(x >> 4, z >> 4);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos feet = new BlockPos(x, y, z);
                    if (Math.abs(y - seaLevel) > 6 || !standable(level, feet)) continue;
                    if (waterWithin(level, feet, 6, seaLevel) && sea(level, feet, 20)) return Optional.of(feet);
                }
            }
        }
        return Optional.empty();
    }

    /** Deep water within {@code reach} blocks, checked on the real terrain. */
    private static boolean sea(ServerLevel level, BlockPos pos, int reach) {
        for (int dx = -reach; dx <= reach; dx += 5) {
            for (int dz = -reach; dz <= reach; dz += 5) {
                if (deepWater(level, pos.getX() + dx, pos.getZ() + dz)) return true;
            }
        }
        return false;
    }

    private static boolean waterWithin(ServerLevel level, BlockPos pos, int reach, int sea) {
        for (int dx = -reach; dx <= reach; dx += 2) {
            for (int dz = -reach; dz <= reach; dz += 2) {
                if (level.getFluidState(new BlockPos(pos.getX() + dx, sea - 1, pos.getZ() + dz)).is(net.minecraft.tags.FluidTags.WATER)) return true;
            }
        }
        return false;
    }

    /** Dry open ground at a random bearing a few hundred blocks from the world spawn (not in the sea, a river or a cave). */
    private static Optional<BlockPos> randomSurface(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 48; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            int radius = 200 + level.random.nextInt(800);
            BlockPos around = origin.offset((int) (Math.cos(angle) * radius), 0, (int) (Math.sin(angle) * radius));
            Optional<BlockPos> spot = surfaceNear(level, around, pos -> level.canSeeSky(pos)
                    && !level.getBiome(pos).is(BiomeTags.IS_OCEAN) && !level.getBiome(pos).is(BiomeTags.IS_RIVER));
            if (spot.isPresent()) return spot;
        }
        return Optional.empty();
    }

    private static Optional<BlockPos> biome(ServerLevel level, BlockPos origin, Predicate<Holder<Biome>> predicate, boolean underground) {
        return biome(level, origin, predicate, underground, pos -> true);
    }

    /**
     * {@code extra} also has to hold at the standing place. The place's own biome is checked against {@code predicate}
     * too: the biome search answers for a point in 3D, which may be above or below the ground the player stands on.
     */
    private static Optional<BlockPos> biome(ServerLevel level, BlockPos origin, Predicate<Holder<Biome>> predicate, boolean underground,
                                            Predicate<BlockPos> extra) {
        Pair<BlockPos, Holder<Biome>> result = level.findClosestBiome3d(predicate, origin, BIOME_RADIUS, 32, 64);
        if (result == null) return Optional.empty();
        Predicate<BlockPos> valid = pos -> predicate.test(level.getBiome(pos)) && extra.test(pos);
        return underground ? caveNear(level, result.getFirst(), valid) : surfaceNear(level, result.getFirst(), valid);
    }

    /** An ocean (not a pond or a river) within {@code reach} blocks: a sea shore has to face a sea. */
    private static boolean seaNearby(ServerLevel level, BlockPos pos, int reach) {
        int y = level.getSeaLevel() - 2;
        for (int dx = -reach; dx <= reach; dx += 8) {
            for (int dz = -reach; dz <= reach; dz += 8) {
                if (level.getBiome(new BlockPos(pos.getX() + dx, y, pos.getZ() + dz)).is(BiomeTags.IS_OCEAN)) return true;
            }
        }
        return false;
    }

    // ---- standing places -------------------------------------------------------------------------

    private static boolean standable(ServerLevel level, BlockPos feet) {
        BlockState floor = level.getBlockState(feet.below());
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty() && level.getFluidState(feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet.above()).isEmpty()
                && floor.isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP) && level.getFluidState(feet.below()).isEmpty();
    }

    /** A dry place on the ground near the column of {@code around}, looking in growing rings. */
    private static Optional<BlockPos> surfaceNear(ServerLevel level, BlockPos around, Predicate<BlockPos> valid) {
        for (int ring = 0; ring <= 64; ring += 2) {
            for (int dx = -ring; dx <= ring; dx += 2) {
                for (int dz = -ring; dz <= ring; dz += 2) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    int x = around.getX() + dx, z = around.getZ() + dz;
                    level.getChunk(x >> 4, z >> 4);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos feet = new BlockPos(x, y, z);
                    if (standable(level, feet) && valid.test(feet)) return Optional.of(feet);
                }
            }
        }
        return Optional.empty();
    }

    /** Open floor inside a cave, as near as possible to the position the biome search returned. */
    private static Optional<BlockPos> caveNear(ServerLevel level, BlockPos around, Predicate<BlockPos> valid) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -24; dx <= 24; dx += 2) {
            for (int dz = -24; dz <= 24; dz += 2) {
                level.getChunk((around.getX() + dx) >> 4, (around.getZ() + dz) >> 4);
                for (int dy = -20; dy <= 20; dy++) {
                    BlockPos feet = around.offset(dx, dy, dz);
                    if (feet.getY() < level.getMinBuildHeight() + 2 || feet.getY() > 60) continue;
                    double distance = feet.distSqr(around);
                    if (distance < bestDistance && standable(level, feet) && !level.canSeeSky(feet) && valid.test(feet)) {
                        bestDistance = distance;
                        best = feet;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }
}
