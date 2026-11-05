package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.CyberwareSlots;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Reads the implanted upgrades out of Chromeware's sockets and writes a changed upgrade back. */
public final class ImplantAccess {
    private ImplantAccess() {
    }

    public record Entry(int category, int index, ItemStack stack) {
    }

    /** Every implanted item that has a button (backpack upgrades and our own pumps), in the order of the sockets. */
    public static List<Entry> listed(Player player) {
        List<Entry> result = new ArrayList<>();
        for (int category : ImplantUpgrades.categories()) {
            CyberwareSlots slots = InventoryImplants.get(player, category);
            for (int i = 0; i < slots.slots().size(); i++) {
                ItemStack stack = slots.get(i).item();
                if (ImplantUpgrades.rule(stack) != null) result.add(new Entry(category, i, stack));
            }
        }
        return result;
    }

    /** Installed upgrade items in a fixed order (category, then socket), stacks are copies. */
    public static List<Entry> upgrades(Player player) {
        List<Entry> result = new ArrayList<>();
        for (int category : ImplantUpgrades.categories()) {
            CyberwareSlots slots = InventoryImplants.get(player, category);
            for (int i = 0; i < slots.slots().size(); i++) {
                ItemStack stack = slots.get(i).item();
                if (ImplantUpgrades.forBackpack(stack)) result.add(new Entry(category, i, stack));
            }
        }
        return result;
    }

    /** Replaces the stack in a socket by an updated copy of itself (upgrade settings, tank contents). */
    public static void write(ServerPlayer player, int category, int index, ItemStack stack) {
        CyberwareSlots slots = InventoryImplants.get(player, category);
        if (index < 0 || index >= slots.slots().size()) return;
        var updated = slots.with(index, new InstalledImplant(stack.copy()));
        switch (category) {
            case ImplantUpgrades.CIRCULATORY -> player.setData(PlayerData.CIRCULATORY, updated);
            case ImplantUpgrades.LEGS -> player.setData(PlayerData.LEGS, updated);
            case FaceImplants.CATEGORY -> player.setData(PlayerData.FACE, updated);
            default -> {
            }
        }
    }
}
