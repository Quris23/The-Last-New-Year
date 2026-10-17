package dev.qur1s.reviveraltar.event;

import dev.qur1s.reviveraltar.RevivalManager;
import dev.qur1s.reviveraltar.block.ModBlocks;
import dev.qur1s.reviveraltar.command.RevivalCommand;
import dev.qur1s.reviveraltar.data.ModAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommonEventHandlers {

    /** Drop the dead player's head alongside their normal death drops, so corpse mods pick it up too. */
    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().getLevelData().isHardcore()) return;
        if (RevivalManager.isGhost(player)) return; // don't re-drop for a ghost that dies again

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        ItemEntity headEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), head);
        event.getDrops().add(headEntity);
    }

    /** Fires when the ghost's new player entity is created after the death respawn. */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        if (!newPlayer.level().getLevelData().isHardcore()) return;

        RevivalManager.onHardcoreDeath(newPlayer);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RevivalManager.tryApplyOfflineRevival(player);
            // Tell everyone (including the player themselves) their current ghost status,
            // and tell the newly-joined player about everyone else's.
            RevivalManager.announceCurrentState(player);
            RevivalManager.syncGhostRosterTo(player);
            giveStarterKitIfNeeded(player);
        }
    }

    /**
     * A hardcore world normally has no legitimate way to obtain a Ritual Altar (creative
     * mode is off-limits). Singleplayer/LAN hardcore worlds get one on first join so the
     * revival system is actually reachable. Dedicated servers are excluded - there, players
     * are expected to build/trade for the altar themselves.
     */
    private void giveStarterKitIfNeeded(ServerPlayer player) {
        if (player.getData(ModAttachments.STARTER_KIT_GIVEN)) return;
        if (player.getServer() == null || player.getServer().isDedicatedServer()) return;
        if (!player.level().getLevelData().isHardcore()) return;

        player.setData(ModAttachments.STARTER_KIT_GIVEN, true);

        player.getInventory().placeItemBackInInventory(new ItemStack(ModBlocks.RITUAL_ALTAR_ITEM.get()));

        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        player.getInventory().placeItemBackInInventory(head);
    }

    /** Ghosts can't interact with anything in the world. */
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (RevivalManager.isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (RevivalManager.isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (RevivalManager.isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (RevivalManager.isGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (RevivalManager.isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RevivalCommand.register(event.getDispatcher());
    }
}
