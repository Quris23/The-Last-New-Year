package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

import java.util.UUID;

/** Two Atlases on an anvil become one: the first one's id stays, and it takes the second one's terrain and markers. */
@EventBusSubscriber(modid = "atlas")
public final class AnvilHandler {
    private static final int COST = 1;

    private AnvilHandler() {
    }

    @SubscribeEvent
    static void onUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft(), right = event.getRight();
        if (!left.is(AtlasItems.ATLAS.get()) || !right.is(AtlasItems.ATLAS.get())) return;
        ItemStack result = left.copyWithCount(1);
        event.setOutput(result);
        event.setCost(COST);
        event.setMaterialCost(1);
    }

    @SubscribeEvent
    static void onTake(AnvilRepairEvent event) {
        ItemStack left = event.getLeft(), right = event.getRight();
        if (!left.is(AtlasItems.ATLAS.get()) || !right.is(AtlasItems.ATLAS.get())) return;
        var player = event.getEntity();
        if (player.level().isClientSide || player.getServer() == null) return;
        ItemStack output = event.getOutput();
        UUID into = AtlasItem.ensureId(output);
        UUID other = right.get(AtlasComponents.ATLAS_ID.get());
        AtlasData data = AtlasData.get(player.getServer());
        UUID original = left.get(AtlasComponents.ATLAS_ID.get());
        if (original != null && !original.equals(into)) data.mergeAtlas(original, into);
        if (other != null) {
            data.mergeAtlas(other, into);
            data.forget(other);
        }
    }
}
