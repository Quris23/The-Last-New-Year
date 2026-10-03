package dev.qur1s.spellclasses.mixin;

import dev.qur1s.spellclasses.compat.CrossFix;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.co.iceconchy.aerowarptics.AWConfig;
import uk.co.iceconchy.aerowarptics.advancement.AWCriteria;
import uk.co.iceconchy.aerowarptics.airship.Airship;
import uk.co.iceconchy.aerowarptics.beacon.RiftBeaconBinding;
import uk.co.iceconchy.aerowarptics.beacon.RiftBeaconItem;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveBlockEntity;
import uk.co.iceconchy.aerowarptics.drive.RiftDriveTier;
import uk.co.iceconchy.aerowarptics.network.AWNetwork;
import uk.co.iceconchy.aerowarptics.network.ClientboundRiftBeaconPacket;
import uk.co.iceconchy.aerowarptics.network.ClientboundWarpFeedbackPacket;
import uk.co.iceconchy.aerowarptics.registry.AWDataComponents;
import uk.co.iceconchy.aerowarptics.util.AWLang;
import uk.co.iceconchy.aerowarptics.warp.CrossDimensionWarp;
import uk.co.iceconchy.aerowarptics.warp.WarpFailure;

/**
 * A Rift Beacon bound to a drive in another dimension normally just says "cross-dimension warp unsupported".
 * With a Singularity (or Creative) drive it now summons the ship across: the drive is found in ITS dimension and
 * told to fly to the beacon user's spot in THEIR dimension (see {@link RiftDriveCrossDimensionMixin}).
 */
@Mixin(value = RiftBeaconItem.class, remap = false)
public abstract class RiftBeaconCrossDimensionMixin {
    private static final Logger SPELLCLASSES$LOG = LoggerFactory.getLogger("spellclasses/riftbeacon");

    @Inject(method = "summon", at = @At("HEAD"), cancellable = true)
    private void spellclasses$crossSummon(ServerLevel level, ServerPlayer player, ItemStack held, BlockPos target,
                                          CallbackInfoReturnable<InteractionResult> cir) {
        RiftBeaconBinding binding = held.get(AWDataComponents.BEACON_BINDING.get());
        if (binding == null || binding.isIn(level)) {
            return; // unbound or same dimension: AeroWarptics' own path
        }
        SPELLCLASSES$LOG.info("Beacon used in {} but bound to a drive at {} in {} (cross-dimension supported: {})",
                level.dimension().location(), binding.drivePos(), binding.dimension().location(),
                CrossDimensionWarp.isSupported());
        WarpFailure verdict = WarpFailure.NONE;
        RiftDriveBlockEntity drive = null;
        ServerLevel driveLevel = level.getServer().getLevel(binding.dimension());
        if (!CrossDimensionWarp.isSupported() || driveLevel == null) {
            verdict = WarpFailure.DIMENSION_UNSUPPORTED;
        } else {
            BlockEntity be = driveLevel.getBlockEntity(binding.drivePos());
            if (be instanceof RiftDriveBlockEntity found) {
                drive = found;
            } else {
                verdict = WarpFailure.BEACON_DRIVE_MISSING;
            }
        }
        if (drive != null) {
            RiftDriveTier tier = drive.tier();
            Airship airship = drive.airship();
            if (!(tier == RiftDriveTier.SINGULARITY || tier.creative())) {
                verdict = WarpFailure.DIMENSION_UNSUPPORTED;
            } else if (airship == null || !airship.isActive()) {
                verdict = WarpFailure.NO_AIRSHIP;
            }
        }
        if (verdict.isFailure()) {
            SPELLCLASSES$LOG.info("Cross-dimension summon refused: {} (drive entity: {}, tier: {})", verdict,
                    drive == null ? "not found/loaded" : "found", drive == null ? "-" : drive.tier());
            AWNetwork.sendTo(player, new ClientboundWarpFeedbackPacket(verdict));
            cir.setReturnValue(InteractionResult.CONSUME);
            return;
        }

        int cooldown = AWConfig.BEACON_COOLDOWN_TICKS.get();
        WarpFailure result;
        CrossFix.DIMENSION.set(level.dimension());
        try {
            result = drive.summonTo(player, target,
                    AWLang.translate("beacon.course", target.getX(), target.getY(), target.getZ()).string());
        } finally {
            CrossFix.DIMENSION.remove();
        }
        SPELLCLASSES$LOG.info("Drive answered the cross-dimension summon with {}", result);
        player.getCooldowns().addCooldown((Item) (Object) this, cooldown);
        if (!result.isFailure()) {
            ClientboundRiftBeaconPacket.broadcast(level, target, drive);
            AWCriteria.shipSummoned(player);
        }
        cir.setReturnValue(InteractionResult.CONSUME);
    }
}
