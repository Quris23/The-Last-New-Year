package dev.qur1s.reviveraltar;

import dev.qur1s.reviveraltar.compat.ModCompat;
import dev.qur1s.reviveraltar.data.ModAttachments;
import dev.qur1s.reviveraltar.data.RevivalSavedData;
import dev.qur1s.reviveraltar.network.GhostSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/**
 * Central logic for turning a hardcore death into "ghost" state, and reviving ghosts
 * back to survival via the ritual altar.
 */
public final class RevivalManager {
    private RevivalManager() {}

    public static boolean isAwaitingRevival(ServerLevel level, UUID uuid) {
        return RevivalSavedData.get(level).isAwaitingRevival(uuid);
    }

    public static String getName(ServerLevel level, UUID uuid) {
        return RevivalSavedData.get(level).getName(uuid);
    }

    /** Called right after a player's hardcore death has been finalized (new spectator entity exists). */
    public static void onHardcoreDeath(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RevivalSavedData.get(level).markAwaitingRevival(player.getUUID(), player.getGameProfile().getName());

        player.setGameMode(GameType.SPECTATOR);
        player.setHealth(player.getMaxHealth());
        setGhost(player, true);

        player.serverLevel().getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.reviveraltar.became_ghost", player.getName()), false);

        triggerPrimedAltar(level, player.getUUID());
    }

    /**
     * If this player pre-loaded an altar with their own head + totem while alive, start
     * that ritual now that they've actually died.
     */
    private static void triggerPrimedAltar(ServerLevel deathLevel, UUID uuid) {
        RevivalSavedData data = RevivalSavedData.get(deathLevel);
        RevivalSavedData.PrimedAltar primed = data.takePrimedAltar(uuid);
        if (primed == null) {
            return;
        }
        ServerLevel altarLevel = deathLevel.getServer().getLevel(primed.dimension());
        if (altarLevel == null) {
            return;
        }
        // Force the chunk to load so the block entity actually exists to trigger.
        altarLevel.getChunk(primed.pos());
        if (altarLevel.getBlockEntity(primed.pos())
                instanceof dev.qur1s.reviveraltar.block.RitualAltarBlockEntity altar) {
            altar.triggerAutoCharge();
        }
    }

    /**
     * Sets the ghost attachment and broadcasts it to every online player (including the
     * target) so everyone's client renders/HUD stays in sync. Server-side only.
     */
    private static void setGhost(ServerPlayer player, boolean ghost) {
        player.setData(ModAttachments.GHOST, ghost);
        PacketDistributor.sendToAllPlayers(new GhostSyncPayload(player.getUUID(), ghost));
    }

    /** Broadcasts this player's current ghost status to everyone (used on login). */
    public static void announceCurrentState(ServerPlayer player) {
        boolean ghost = player.getData(ModAttachments.GHOST);
        PacketDistributor.sendToAllPlayers(new GhostSyncPayload(player.getUUID(), ghost));
    }

    /** Sends the current ghost roster to a single (usually newly-joined) player. */
    public static void syncGhostRosterTo(ServerPlayer target) {
        for (ServerPlayer online : target.server.getPlayerList().getPlayers()) {
            boolean ghost = online.getData(ModAttachments.GHOST);
            if (ghost) {
                PacketDistributor.sendToPlayer(target, new GhostSyncPayload(online.getUUID(), true));
            }
        }
    }

    public static boolean isGhost(Player player) {
        if (player.level().isClientSide()) {
            return dev.qur1s.reviveraltar.client.ClientGhostState.isGhost(player.getUUID());
        }
        return player.getData(ModAttachments.GHOST);
    }

    /** Like {@link #isAwaitingRevival}, but works from either logical side given just a UUID. */
    public static boolean isGhostByUuid(net.minecraft.world.level.Level level, UUID uuid) {
        if (level.isClientSide()) {
            return dev.qur1s.reviveraltar.client.ClientGhostState.isGhost(uuid);
        }
        return isAwaitingRevival((ServerLevel) level, uuid);
    }

    /** Called when a ritual altar finishes channeling for the given player UUID. */
    public static void revive(ServerLevel level, UUID uuid, String name, BlockPos altarPos) {
        RevivalSavedData data = RevivalSavedData.get(level);
        data.clearAwaitingRevival(uuid);

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            reviveOnline(player, altarPos);
        } else {
            data.queueOfflineRevival(uuid, level.dimension(), altarPos.getX() + 0.5, altarPos.getY() + 1.0, altarPos.getZ() + 0.5);
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.reviveraltar.revived", name), false);
    }

    private static void reviveOnline(ServerPlayer player, BlockPos altarPos) {
        setGhost(player, false);
        player.setGameMode(GameType.SURVIVAL);
        player.teleportTo(player.serverLevel(), altarPos.getX() + 0.5, altarPos.getY() + 1.0, altarPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        applyPostRevivalState(player);
    }

    /**
     * Forces a revival outside of the ritual, e.g. from a command. Revives in place if the
     * target is online, otherwise queues them to revive at world spawn on next login.
     */
    public static boolean forceRevive(ServerLevel level, UUID uuid, String name) {
        RevivalSavedData data = RevivalSavedData.get(level);
        if (!data.isAwaitingRevival(uuid)) {
            return false;
        }
        data.clearAwaitingRevival(uuid);

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            reviveInPlace(player);
        } else {
            BlockPos spawn = level.getServer().overworld().getSharedSpawnPos();
            data.queueOfflineRevival(uuid, level.getServer().overworld().dimension(),
                    spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5);
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.reviveraltar.revived", name), false);
        return true;
    }

    private static void reviveInPlace(ServerPlayer player) {
        setGhost(player, false);
        player.setGameMode(GameType.SURVIVAL);
        applyPostRevivalState(player);
    }

    /** Called on login; applies a revival that finished while the player was offline. */
    public static void tryApplyOfflineRevival(ServerPlayer player) {
        RevivalSavedData data = RevivalSavedData.get(player.serverLevel());
        RevivalSavedData.PendingTeleport pending = data.takePendingRevival(player.getUUID());
        if (pending == null) {
            return;
        }
        setGhost(player, false);
        player.setGameMode(GameType.SURVIVAL);
        ServerLevel destination = player.getServer().getLevel(pending.dimension());
        if (destination != null) {
            player.teleportTo(destination, pending.x(), pending.y(), pending.z(), player.getYRot(), player.getXRot());
        }
        applyPostRevivalState(player);
    }

    /**
     * A ritual revival brings you back alive, but not unscathed: you come back hungry
     * and marked by the ritual's cost.
     */
    private static void applyPostRevivalState(ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(6);
        player.getFoodData().setSaturation(0.0F);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 6000, 0));

        Holder<MobEffect> cursedMark = ModCompat.cursedMark();
        if (cursedMark != null) {
            player.addEffect(new MobEffectInstance(cursedMark, 6000, 0));
        }
    }
}
