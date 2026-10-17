package dev.qur1s.atlas;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Where a player's Atlas is kept: the "atlas" Curios slot. */
public final class Atlases {
    public static final String SLOT = "atlas";

    private Atlases() {
    }

    /** The Atlas in the Atlas slot (EMPTY if there is none). */
    public static ItemStack equippedStack(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler(SLOT))
                .map(handler -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(i);
                        if (stack.is(AtlasItems.ATLAS.get())) return stack;
                    }
                    return ItemStack.EMPTY;
                })
                .orElse(ItemStack.EMPTY);
    }

    /** The id of the Atlas in the Atlas slot, or null if there is none (or it has no id yet). */
    public static UUID equippedId(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler(SLOT))
                .map(handler -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(i);
                        if (stack.is(AtlasItems.ATLAS.get())) return stack.get(AtlasComponents.ATLAS_ID.get());
                    }
                    return null;
                })
                .orElse(null);
    }

    /** Whether the player carries the Atlas with this id (inventory, hands or the Atlas slot). */
    public static boolean carries(Player player, UUID id) {
        if (id == null) return false;
        for (ItemStack stack : player.getInventory().items) {
            if (id.equals(stack.get(AtlasComponents.ATLAS_ID.get()))) return true;
        }
        if (id.equals(player.getOffhandItem().get(AtlasComponents.ATLAS_ID.get()))) return true;
        return id.equals(equippedId(player)) || AtlasLectern.allows(player, id);
    }

    public static boolean hasEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.getStacksHandler(SLOT))
                .map(handler -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        if (handler.getStacks().getStackInSlot(i).is(AtlasItems.ATLAS.get())) return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
