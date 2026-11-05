package dev.qur1s.spellclasses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import com.simibubi.create.content.schematics.client.SchematicTransformation;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.qur1s.spellclasses.implant.FaceImplants;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holographic Apple: while the implant is switched on, every deployed (placed) Create schematic in the player's inventory is drawn
 * in the world, not only the one held in the hand. Create draws the held schematic itself, so that one is skipped here.
 * Registered by SpellClasses on the client only when Create and Chromeware are present.
 */
public final class HologramSchematics {
    private record Key(String owner, String file, BlockPos anchor, Rotation rotation, Mirror mirror) {
    }

    private static final class Entry {
        SchematicTransformation transformation = new SchematicTransformation();
        final SchematicRenderer[] renderers = new SchematicRenderer[3];
        boolean seen;
    }

    private static final Map<Key, Entry> CACHE = new HashMap<>();
    private static final Set<Key> FAILED = new HashSet<>();

    private static final float DEPTH_PUSH = 1.001F;

    private HologramSchematics() {
    }

    private static boolean implantOn(Player player) {
        ItemStack apple = FaceImplants.find(player, FaceImplants::isApple);
        return !apple.isEmpty() && FaceImplants.appleOn(apple);
    }

    private static Key keyOf(ItemStack stack) {
        if (!AllItems.SCHEMATIC.isIn(stack) || !stack.has(AllDataComponents.SCHEMATIC_FILE)) return null;
        if (!stack.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false) || stack.get(AllDataComponents.SCHEMATIC_BOUNDS) == null) return null;
        return new Key(stack.get(AllDataComponents.SCHEMATIC_OWNER), stack.get(AllDataComponents.SCHEMATIC_FILE),
                stack.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO),
                stack.getOrDefault(AllDataComponents.SCHEMATIC_ROTATION, Rotation.NONE),
                stack.getOrDefault(AllDataComponents.SCHEMATIC_MIRROR, Mirror.NONE));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null && !CACHE.isEmpty()) {
            CACHE.clear();
            FAILED.clear();
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        if (!implantOn(player)) {
            if (!CACHE.isEmpty()) {
                CACHE.clear();
                FAILED.clear();
            }
            return;
        }

        Key held = keyOf(player.getMainHandItem());
        Set<Key> wanted = new HashSet<>();
        for (ItemStack stack : player.getInventory().items) addKey(wanted, stack);
        addKey(wanted, player.getOffhandItem());
        wanted.remove(held); // Create draws the held one itself

        CACHE.keySet().removeIf(k -> !wanted.contains(k));
        FAILED.removeIf(k -> !wanted.contains(k));
        if (wanted.isEmpty()) return;

        PoseStack ms = event.getPoseStack();
        ms.pushPose();
        // Everything is pushed 0.1% away from the camera (a scale about the camera: the picture does not move, only the depth grows), so where a real
        // block stands, its faces always win over the hologram's and nothing flickers.
        ms.scale(DEPTH_PUSH, DEPTH_PUSH, DEPTH_PUSH);
        SuperRenderTypeBuffer base = DefaultSuperRenderTypeBuffer.getInstance();
        SuperRenderTypeBuffer buffer = new HologramBuffer(base);
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        for (Key key : wanted) {
            if (FAILED.contains(key)) continue;
            Entry entry = CACHE.get(key);
            if (entry == null) {
                entry = build(mc.level, key);
                if (entry == null) {
                    FAILED.add(key);
                    continue;
                }
                CACHE.put(key, entry);
            }
            draw(entry, ms, buffer, camera);
        }
        RenderSystem.setShaderColor(HologramBuffer.RED, HologramBuffer.GREEN, HologramBuffer.BLUE, 1.0F);
        base.draw();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        ms.popPose();
    }

    private static void addKey(Set<Key> set, ItemStack stack) {
        Key key = keyOf(stack);
        if (key != null) set.add(key);
    }

    private static void draw(Entry entry, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
        ms.pushPose();
        entry.transformation.applyTransformations(ms, camera);
        SchematicRenderer r = entry.renderers[0];
        float pt = net.createmod.catnip.animation.AnimationTickHolder.getPartialTicks();
        boolean lr = entry.transformation.getScaleLR().getValue(pt) < 0.0F;
        boolean fb = entry.transformation.getScaleFB().getValue(pt) < 0.0F;
        if (lr && !fb && entry.renderers[2] != null) r = entry.renderers[2];
        else if (fb && !lr && entry.renderers[1] != null) r = entry.renderers[1];
        if (r != null) r.render(ms, buffer);
        ms.popPose();
    }

    /** The same preparation Create's own handler does for the held schematic (three renderers: normal and the two mirrored). */
    private static Entry build(Level level, Key key) {
        try {
            ItemStack blueprint = findStack(key);
            if (blueprint == null) return null;
            Vec3i size = blueprint.get(AllDataComponents.SCHEMATIC_BOUNDS);
            if (size == null) return null;
            StructureTemplate schematic = SchematicItem.loadSchematic(level, blueprint);
            Vec3i real = schematic.getSize();
            if (real.equals(Vec3i.ZERO)) return null;

            Entry entry = new Entry();
            AABB bounds = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
            entry.transformation.init(key.anchor(), SchematicItem.getSettings(blueprint), bounds);

            SchematicLevel w = new SchematicLevel(level);
            SchematicLevel wFB = new SchematicLevel(level);
            SchematicLevel wLR = new SchematicLevel(level);
            StructurePlaceSettings placement = new StructurePlaceSettings();
            BlockPos pos = BlockPos.ZERO;
            schematic.placeInWorld(w, pos, pos, placement, w.getRandom(), 2);
            for (BlockEntity be : w.getBlockEntities()) be.setLevel(w);
            fixControllers(w);

            placement.setMirror(Mirror.FRONT_BACK);
            pos = BlockPos.ZERO.east(real.getX() - 1);
            schematic.placeInWorld(wFB, pos, pos, placement, wFB.getRandom(), 2);
            StructureTransform transform = new StructureTransform(placement.getRotationPivot(), Axis.Y, Rotation.NONE, placement.getMirror());
            for (BlockEntity be : wFB.getRenderedBlockEntities()) transform.apply(be);
            fixControllers(wFB);

            placement.setMirror(Mirror.LEFT_RIGHT);
            pos = BlockPos.ZERO.south(real.getZ() - 1);
            schematic.placeInWorld(wLR, pos, pos, placement, wFB.getRandom(), 2);
            transform = new StructureTransform(placement.getRotationPivot(), Axis.Y, Rotation.NONE, placement.getMirror());
            for (BlockEntity be : wLR.getRenderedBlockEntities()) transform.apply(be);
            fixControllers(wLR);

            entry.renderers[0] = new SchematicRenderer(w);
            entry.renderers[1] = new SchematicRenderer(wFB);
            entry.renderers[2] = new SchematicRenderer(wLR);
            return entry;
        } catch (Throwable t) {
            LogUtils.getLogger().warn("Holographic Apple: cannot load schematic {}", key.file(), t);
            return null;
        }
    }

    private static ItemStack findStack(Key key) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return null;
        for (ItemStack stack : player.getInventory().items) if (key.equals(keyOf(stack))) return stack;
        ItemStack off = player.getOffhandItem();
        return key.equals(keyOf(off)) ? off : null;
    }

    private static void fixControllers(SchematicLevel level) {
        for (BlockEntity blockEntity : level.getBlockEntities()) {
            if (blockEntity instanceof IMultiBlockEntityContainer multi) {
                BlockPos lastKnown = multi.getLastKnownPos();
                BlockPos current = blockEntity.getBlockPos();
                if (lastKnown != null && current != null && !multi.isController() && !lastKnown.equals(current)) {
                    BlockPos controller = multi.getController().offset(current.subtract(lastKnown));
                    if (multi instanceof SmartBlockEntity sbe) sbe.markVirtual();
                    multi.setController(controller);
                }
            }
        }
    }
}
