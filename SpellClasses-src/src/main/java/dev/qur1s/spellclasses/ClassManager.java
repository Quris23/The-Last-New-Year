package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.data.ClassAttachments;
import dev.qur1s.spellclasses.data.GlobalClassData;
import dev.qur1s.spellclasses.data.PlayerClassData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClassManager {
    private ClassManager() {
    }

    private static Map<String, Integer> parsedLimits;

    private static Map<String, Integer> limits() {
        if (parsedLimits == null) {
            parsedLimits = new HashMap<>();
            for (String entry : ClassConfig.CLASS_LIMITS.get()) {
                int eq = entry.lastIndexOf('=');
                if (eq <= 0) continue;
                try {
                    parsedLimits.put(entry.substring(0, eq), Integer.parseInt(entry.substring(eq + 1).trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return parsedLimits;
    }

    /** Call once the config has (re)loaded, so edited limits take effect without a restart. */
    public static void invalidateLimitsCache() {
        parsedLimits = null;
    }

    /** Whether {@code player} may still lock in {@code school}, respecting any configured cap. */
    public static boolean canJoinSchool(ServerPlayer player, ResourceLocation school) {
        int max = limits().getOrDefault(school.toString(), 0);
        if (max <= 0) return true;
        GlobalClassData data = GlobalClassData.get(player.serverLevel());
        if (data.isMember(school.toString(), player.getUUID())) return true;
        return data.countInSchool(school.toString()) < max;
    }

    public static Optional<ResourceLocation> chosenSchool(ServerPlayer player) {
        return player.getData(ClassAttachments.PLAYER_CLASS).chosenSchool().map(ResourceLocation::parse);
    }

    public static boolean hasChosen(ServerPlayer player) {
        return player.getData(ClassAttachments.PLAYER_CLASS).hasChosen();
    }

    public static boolean bookGiven(ServerPlayer player) {
        return player.getData(ClassAttachments.PLAYER_CLASS).bookGiven();
    }

    public static void markBookGiven(ServerPlayer player) {
        player.setData(ClassAttachments.PLAYER_CLASS, player.getData(ClassAttachments.PLAYER_CLASS).withBookGiven());
    }

    /**
     * Attempts to lock {@code player} into {@code school}. Returns true and persists the choice
     * (attachment + the world-wide member set behind the cap) on success; false if already chosen
     * or the school is full, in which case the caller should tell the player why.
     */
    public static boolean tryChooseSchool(ServerPlayer player, ResourceLocation school) {
        if (hasChosen(player)) return false;
        if (!ClassSchools.restrictedSchools().contains(school)) return false;
        if (!canJoinSchool(player, school)) return false;

        player.setData(ClassAttachments.PLAYER_CLASS,
                player.getData(ClassAttachments.PLAYER_CLASS).withChosenSchool(school.toString()));
        GlobalClassData.get(player.serverLevel()).addMember(school.toString(), player.getUUID());
        ColdSweatCompat.syncResistance(player, Optional.of(school));
        return true;
    }

    public static void giveClassBook(ServerPlayer player) {
        var stack = new net.minecraft.world.item.ItemStack(dev.qur1s.spellclasses.item.ClassItems.CLASS_BOOK.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        markBookGiven(player);
    }

    /** Clears {@code player}'s chosen class (freeing their slot under any cap) so they can pick again. */
    public static void resetClass(ServerPlayer player) {
        chosenSchool(player).ifPresent(school -> {
            GlobalClassData.get(player.serverLevel()).removeMember(school.toString(), player.getUUID());
            ColdSweatCompat.syncPowerBuff(player, school, false);
        });
        player.setData(ClassAttachments.PLAYER_CLASS, new PlayerClassData(Optional.empty(), true));
        ColdSweatCompat.syncResistance(player, Optional.empty());
    }

    /** Admin shortcut: reset, then hand a fresh class book. */
    public static void reissueClassBook(ServerPlayer player) {
        resetClass(player);
        giveClassBook(player);
    }

    /** Admin shortcut: reset, then set the class directly, bypassing the book/screen (still respects the cap). */
    public static boolean forceChooseSchool(ServerPlayer player, ResourceLocation school) {
        resetClass(player);
        return tryChooseSchool(player, school);
    }
}
