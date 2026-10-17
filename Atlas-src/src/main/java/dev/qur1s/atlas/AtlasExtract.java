package dev.qur1s.atlas;

import dev.qur1s.atlas.data.AtlasData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.UUID;

/** Taking the map that was poured in last out of an Atlas again (the Atlas alone in the cartography table). */
public final class AtlasExtract {
    private AtlasExtract() {
    }

    /** A filled map holding the picture of the atlas's last poured map, or EMPTY if the atlas has none. */
    public static ItemStack preview(ServerLevel level, UUID atlasId, net.minecraft.core.BlockPos near) {
        AtlasData.LastMap last = AtlasData.get(level.getServer()).lastMap(atlasId, level.dimension().location().toString(), near.getX(), near.getZ());
        if (last == null) return ItemStack.EMPTY;
        var sheet = last.sheet();
        if (sheet.pendingMapId < 0) {
            ResourceLocation dimension = ResourceLocation.tryParse(last.dimension());
            if (dimension == null) return ItemStack.EMPTY;
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
            MapId id = level.getFreeMapId();
            MapItemSavedData data = MapItemSavedData.createFresh(sheet.centreX, sheet.centreZ, (byte) sheet.scale, true, false, key);   // tracks the player, like a map made the usual way
            System.arraycopy(sheet.colors, 0, data.colors, 0, Math.min(sheet.colors.length, data.colors.length));
            data.setDirty();
            level.setMapData(id, data);
            sheet.pendingMapId = id.id();
        }
        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.set(DataComponents.MAP_ID, new MapId(sheet.pendingMapId));
        return stack;
    }
}
