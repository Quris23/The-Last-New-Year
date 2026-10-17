package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import dev.qur1s.atlas.data.AtlasSheet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * A player with an Atlas in the Atlas slot keeps every sheet under their feet drawn up, the way a map in
 * the hand is: what they build or change shows on that sheet as they walk about on it.
 */
@EventBusSubscriber(modid = "atlas")
public final class AtlasLiveUpdate {
    private static final int EVERY_TICKS = 4;

    private AtlasLiveUpdate() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || (player.tickCount + player.getId()) % EVERY_TICKS != 0) return;
        var atlas = Atlases.equippedStack(player);
        if (atlas.isEmpty()) return;
        var id = atlas.get(AtlasComponents.ATLAS_ID.get());
        if (id == null) return;
        var level = player.serverLevel();
        AtlasData data = AtlasData.get(player.getServer());
        String dimension = level.dimension().location().toString();
        var sheets = data.sheetsAt(id, dimension, player.getBlockX(), player.getBlockZ());
        if (sheets.isEmpty()) return;
        for (AtlasSheet sheet : sheets) {
            if (sheet.live == null) {
                // a map's data whose picture is the sheet's own pixels, so updating it updates the sheet
                sheet.live = MapItemSavedData.createFresh(sheet.centreX, sheet.centreZ, (byte) sheet.scale, false, false, level.dimension());
                sheet.live.colors = sheet.colors;
            }
            ((MapItem) Items.FILLED_MAP).update(level, player, sheet.live);
            sheet.invalidate();
        }
        data.setDirty();
    }
}
