package dev.qur1s.spellclasses.compat;

import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessing;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.HashSet;
import java.util.Set;

/** Runs a Create fan-processing type on everything inside a breath cone: loose items and items on depots/belts. */
final class BreathProcessing {
    /** How many fan-processing ticks one tick inside the breath counts for (a fan needs fanProcessingTime ticks in a row). */
    private static final int STEPS_PER_TICK = 3;

    private BreathProcessing() {
    }

    static void process(Level level, AbstractConeProjectile cone, FanProcessingType type, boolean ignite) {
        PartEntity<?>[] parts = cone.getParts();
        if (parts == null) return;
        Set<ItemEntity> items = new HashSet<>();
        Set<BlockPos> blocks = new HashSet<>();
        for (PartEntity<?> part : parts) {
            AABB box = part.getBoundingBox();
            items.addAll(level.getEntitiesOfClass(ItemEntity.class, box));
            for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
                blocks.add(pos.immutable());
            }
        }
        if (type != null) {
            for (ItemEntity item : items) {
                if (item.isRemoved() || !FanProcessing.canProcess(item, type)) continue;
                for (int i = 0; i < STEPS_PER_TICK && !item.isRemoved(); i++) {
                    if (FanProcessing.applyProcessing(item, type)) break;
                }
            }
        }
        for (BlockPos pos : blocks) {
            if (!level.hasChunkAt(pos)) continue;
            if (ignite) FireSpellCampfires.light(level, pos);
            if (type == null) continue;
            TransportedItemStackHandlerBehaviour handler = BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
            if (handler == null) continue;
            for (int i = 0; i < STEPS_PER_TICK; i++) {
                handler.handleProcessingOnAllItems(stack -> FanProcessing.applyProcessing(stack, level, type));
            }
        }
    }
}
