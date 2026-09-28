package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.data.ClassAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Periodically checks each online player's inventory for the items registered in
 * {@link FtbQuestsUniversalGate} - however they got it (crafted, looted, /give, traded) - and
 * remembers the first time they're seen holding one. Read by
 * {@link dev.qur1s.spellclasses.mixin.FtbQuestsChapterVisibilityMixin} to reveal the matching
 * universal chapter; never consumes or otherwise touches the item.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class UniversalItemPickupEvents {
    private UniversalItemPickupEvents() {
    }

    private static final int CHECK_INTERVAL_TICKS = 40;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL_TICKS != 0) return;

        Set<String> obtained = player.getData(ClassAttachments.OBTAINED_UNIVERSAL_ITEMS);
        Set<String> newlyObtained = null;
        for (ResourceLocation itemId : FtbQuestsUniversalGate.allRequiredItems()) {
            String key = itemId.toString();
            if (obtained.contains(key)) continue;
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == Items.AIR || !hasItem(player, item)) continue;
            if (newlyObtained == null) newlyObtained = new HashSet<>(obtained);
            newlyObtained.add(key);
        }
        if (newlyObtained != null) {
            player.setData(ClassAttachments.OBTAINED_UNIVERSAL_ITEMS, newlyObtained);
        }
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        if (player.getMainHandItem().is(item) || player.getOffhandItem().is(item)) return true;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) return true;
        }
        return false;
    }
}
