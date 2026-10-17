package dev.qur1s.atlas;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.Optional;

/** Points a compass at a spot the way a lodestone does, without needing a lodestone there. */
public final class AtlasCompass {
    private AtlasCompass() {
    }

    /** The compass in a hand (main hand first), or EMPTY. */
    public static ItemStack inHands(net.minecraft.world.entity.player.Player player) {
        if (player.getMainHandItem().is(Items.COMPASS)) return player.getMainHandItem();
        if (player.getOffhandItem().is(Items.COMPASS)) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    /** Magnetises the compass in the player's hands onto {@code target}; returns whether there was one. */
    public static boolean magnetize(ServerPlayer player, GlobalPos target) {
        return magnetize(player, inHands(player), target);
    }

    public static boolean magnetize(ServerPlayer player, ItemStack compass, GlobalPos target) {
        if (compass.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.atlas.no_compass"), true);
            return false;
        }
        ItemStack tuned = compass;
        if (compass.getCount() > 1) {
            tuned = compass.split(1);
            tuned.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(target), false));
            if (!player.getInventory().add(tuned)) player.drop(tuned, false);
        } else {
            tuned.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(target), false));
        }
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.LODESTONE_COMPASS_LOCK,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.atlas.compass_set",
                target.pos().getX(), target.pos().getY(), target.pos().getZ()), true);
        return true;
    }

    /** Whether {@code hand} is the one not holding... helper for the other hand. */
    public static InteractionHand other(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
}
