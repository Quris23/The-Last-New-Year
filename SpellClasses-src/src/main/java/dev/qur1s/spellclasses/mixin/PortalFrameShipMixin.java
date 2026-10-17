package dev.qur1s.spellclasses.mixin;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlock;
import io.redspace.ironsspellbooks.block.portal_frame.PortalFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Iron's Spellbooks portal frame standing on a Sable sub-level (a ship): the frame can be walked into from the ground
 * but not from the ship. The stock check (and the Sable compat's projection of it) only moves the frame's corner into the
 * world and ignores the ship's rotation, so an entity inside a turned or tilted frame is never found. Here the entity is
 * brought into the ship's own space instead and tested against the frame's real shape; the frame then teleports it as usual.
 */
@Mixin(value = PortalFrameBlockEntity.class, remap = false)
public abstract class PortalFrameShipMixin {
    @Inject(method = "serverTick", at = @At("HEAD"), require = 0)
    private static void spellclasses$shipFrameTick(Level level, BlockPos pos, BlockState state, PortalFrameBlockEntity frame, CallbackInfo ci) {
        if (level.isClientSide) return;
        SubLevel ship = Sable.HELPER.getContaining(level, (Vec3i) pos);
        if (ship == null) return;
        Pose3dc pose = ship.logicalPose();
        AABB local = state.getShape(level, pos, CollisionContext.empty()).bounds().move(pos).inflate(0.01);
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            Vec3 w = pose.transformPosition(new Vec3((i & 1) == 0 ? local.minX : local.maxX,
                    (i & 2) == 0 ? local.minY : local.maxY, (i & 4) == 0 ? local.minZ : local.maxZ));
            minX = Math.min(minX, w.x); maxX = Math.max(maxX, w.x);
            minY = Math.min(minY, w.y); maxY = Math.max(maxY, w.y);
            minZ = Math.min(minZ, w.z); maxZ = Math.max(maxZ, w.z);
        }
        AABB world = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        PortalFrameBlock block = (PortalFrameBlock) state.getBlock();
        List<Entity> found = level.getEntities((Entity) null, world, block::canTeleport);
        for (Entity entity : found) {
            // an entity that itself sits in the ship's plot space is handled by the stock code
            if (Sable.HELPER.getContaining(level, entity.position()) != null) continue;
            if (spellclasses$localBox(entity, pose).intersects(local)) {
                frame.teleport(entity);
            }
        }
    }

    /** The entity's box around its centre as seen from the ship's own coordinates. */
    @Unique
    private static AABB spellclasses$localBox(Entity entity, Pose3dc pose) {
        AABB box = entity.getBoundingBox();
        Vec3 c = pose.transformPositionInverse(box.getCenter());
        double hx = box.getXsize() / 2, hy = box.getYsize() / 2, hz = box.getZsize() / 2;
        return new AABB(c.x - hx, c.y - hy, c.z - hz, c.x + hx, c.y + hy, c.z + hz);
    }
}
