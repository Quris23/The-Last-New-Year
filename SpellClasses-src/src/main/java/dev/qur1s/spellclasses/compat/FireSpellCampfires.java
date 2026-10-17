package dev.qur1s.spellclasses.compat;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Every fire-school spell lights a campfire it is aimed at (and the campfires right next to it). */
@EventBusSubscriber(modid = "spellclasses")
public final class FireSpellCampfires {
    private static final double RANGE = 24.0;

    private FireSpellCampfires() {
    }

    @SubscribeEvent
    static void onSpellCast(SpellOnCastEvent event) {
        if (!SchoolRegistry.FIRE_RESOURCE.equals(event.getSchoolType().getId())) return;
        var player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos center = hit.getBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            light(level, pos.immutable());
        }
    }

    /** Lights the campfire at this position, if there is an unlit one that is not waterlogged. */
    static void light(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CampfireBlock) || !state.hasProperty(CampfireBlock.LIT)) return;
        if (state.getValue(CampfireBlock.LIT) || (state.hasProperty(CampfireBlock.WATERLOGGED) && state.getValue(CampfireBlock.WATERLOGGED))) return;
        level.setBlock(pos, state.setValue(CampfireBlock.LIT, true), 3);
        level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
