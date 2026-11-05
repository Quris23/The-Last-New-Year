package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.CyberwareSlots;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.Installing;
import com.chromeware.humanity.Humanity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The two "К" sockets added to Chromeware's Operating System group. They live in an attachment of their own (the same
 * CyberwareSlots record Chromeware uses, entries 1 and 2; entry 0 stays unused so the indices match the sockets of the menu) and
 * are reached through Chromeware's own install and removal code as its category number 8. Only an Altimeter and a Compass fit.
 */
public final class KSlots {
    public static final int CATEGORY = 8;
    public static final int FIRST = 1;
    public static final int LAST = 2;

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "spellclasses");
    public static final Supplier<AttachmentType<CyberwareSlots>> SLOTS = ATTACHMENTS.register("k_slots",
            () -> AttachmentType.builder(() -> CyberwareSlots.EMPTY).serialize(CyberwareSlots.CODEC)
                    .copyOnDeath().sync(CyberwareSlots.STREAM_CODEC).build());

    private KSlots() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    public static boolean isAltimeter(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("supplementaries") && id.getPath().equals("altimeter");
    }

    public static boolean isCompass(ItemStack stack) {
        return stack.is(Items.COMPASS);
    }

    /** The only items that fit the К sockets. */
    public static boolean isK(ItemStack stack) {
        return !stack.isEmpty() && (isAltimeter(stack) || isCompass(stack));
    }

    public static ItemStack slot(Player player, int index) {
        return player.getData(SLOTS).get(index).item();
    }

    /** The installed items of both sockets (copies), empty sockets left out. */
    public static List<ItemStack> items(Player player) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = FIRST; i <= LAST; i++) {
            ItemStack stack = slot(player, i);
            if (!stack.isEmpty()) result.add(stack);
        }
        return result;
    }

    public static ItemStack find(Player player, java.util.function.Predicate<ItemStack> what) {
        for (ItemStack stack : items(player)) {
            if (what.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Takes the implant out of a К socket (the item goes back to the player), like Chromeware's own removal. */
    public static boolean remove(Player player, int index) {
        if (index < FIRST || index > LAST || !Installing.canOperate(player)) return false;
        CyberwareSlots slots = player.getData(SLOTS);
        InstalledImplant previous = slots.get(index);
        if (!previous.installed()) return false;
        player.setData(SLOTS, slots.with(index, InstalledImplant.EMPTY));
        Humanity.removed(player);
        Installing.returnItem(player, previous.item());
        player.displayClientMessage(Component.translatable("message.chromeware.internal_removed", previous.item().getHoverName()), true);
        return true;
    }
}
