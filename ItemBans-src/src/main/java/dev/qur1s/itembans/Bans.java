package dev.qur1s.itembans;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Bans {
    private Bans() {}

    private static volatile Set<Item> items = Set.of();

    public static boolean isBanned(ItemStack stack) {
        Set<Item> banned = items;
        return !banned.isEmpty() && !stack.isEmpty() && banned.contains(stack.getItem());
    }

    public static Set<Item> all() {
        return items;
    }

    public static void rebuild(List<? extends String> ids) {
        Set<Item> built = new HashSet<>();
        for (String raw : ids) {
            ResourceLocation id = ResourceLocation.tryParse(raw.trim());
            if (id == null) {
                ItemBans.LOGGER.warn("Ignoring invalid banned item id '{}'", raw);
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(id).ifPresentOrElse(built::add,
                    () -> ItemBans.LOGGER.info("Banned item '{}' isn't installed, skipping", id));
        }
        items = built;
        ItemBans.LOGGER.info("Banned items: {}", built);
    }
}
