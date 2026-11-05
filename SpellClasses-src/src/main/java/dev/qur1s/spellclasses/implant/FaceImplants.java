package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.CyberwareSlots;
import com.chromeware.cyberware.InventoryImplants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;

import java.util.function.Predicate;

/**
 * The two items added to the Face group: Alex's Caves Hazmat Mask and Artifacts' Night Vision Goggles. The mask is only drawn on
 * the player. The goggles are drawn only while switched on, and the switch (an ability of the implant wheel) gives night vision.
 */
public final class FaceImplants {
    public static final int CATEGORY = 7;

    /** Set only while the armor layer draws the implanted mask: what the head slot of the drawn player answers. */
    public static final ThreadLocal<ItemStack> RENDER_HEAD = new ThreadLocal<>();

    private FaceImplants() {
    }

    public static boolean isMask(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("alexscaves") && id.getPath().equals("hazmat_mask");
    }

    public static boolean isGoggles(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("artifacts") && id.getPath().equals("night_vision_goggles");
    }

    /** The Holographic Apple: shows every placed Create schematic of the inventory while switched on. */
    public static boolean isApple(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("spellclasses") && id.getPath().equals("holographic_apple");
    }

    /** The apple starts switched on. */
    public static boolean appleOn(ItemStack apple) {
        return apple.getOrDefault(ModCoreDataComponents.ENABLED, true);
    }

    public static void toggleApple(ServerPlayer player) {
        int socket = socketOf(player, FaceImplants::isApple);
        if (socket < 0) return;
        ItemStack apple = InventoryImplants.get(player, CATEGORY).get(socket).item();
        boolean now = !appleOn(apple);
        apple.set(ModCoreDataComponents.ENABLED, now);
        ImplantAccess.write(player, CATEGORY, socket, apple);
        player.displayClientMessage(Component.literal(now ? "Голографическое яблоко: включено" : "Голографическое яблоко: выключено"), true);
    }

    public static ItemStack find(Player player, Predicate<ItemStack> what) {
        CyberwareSlots slots = InventoryImplants.get(player, CATEGORY);
        for (int i = 0; i < slots.slots().size(); i++) {
            ItemStack stack = slots.get(i).item();
            if (!stack.isEmpty() && what.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static int socketOf(Player player, Predicate<ItemStack> what) {
        CyberwareSlots slots = InventoryImplants.get(player, CATEGORY);
        for (int i = 0; i < slots.slots().size(); i++) {
            ItemStack stack = slots.get(i).item();
            if (!stack.isEmpty() && what.test(stack)) return i;
        }
        return -1;
    }

    /** The goggles start switched off. */
    public static boolean on(ItemStack goggles) {
        return goggles.getOrDefault(ModCoreDataComponents.ENABLED, false);
    }

    public static void toggleGoggles(ServerPlayer player) {
        int socket = socketOf(player, FaceImplants::isGoggles);
        if (socket < 0) return;
        ItemStack goggles = InventoryImplants.get(player, CATEGORY).get(socket).item();
        boolean now = !on(goggles);
        goggles.set(ModCoreDataComponents.ENABLED, now);
        ImplantAccess.write(player, CATEGORY, socket, goggles);
        if (!now) removeEffect(player);
        player.displayClientMessage(Component.literal(now ? "Очки ночного зрения: включены" : "Очки ночного зрения: выключены"), true);
    }

    private static void removeEffect(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
        if (effect != null && effect.isAmbient() && !effect.isVisible()) player.removeEffect(MobEffects.NIGHT_VISION);
    }

    /** Switched on goggles keep night vision on the player (a quiet, invisible effect that is refreshed every second). */
    public static void tick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        ItemStack goggles = find(player, FaceImplants::isGoggles);
        if (!goggles.isEmpty() && on(goggles)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
        } else {
            removeEffect(player);
        }
    }
}
