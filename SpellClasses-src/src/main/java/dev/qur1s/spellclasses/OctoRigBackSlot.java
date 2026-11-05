package dev.qur1s.spellclasses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import net.neoforged.neoforge.attachment.AttachmentType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Create: Octo Arms (Tentacle Rig) looks for its backtank in the chest slot. Tentacle rigs are installed as implants
 * in a Chromeware Skeleton socket (see InventoryImplantsMixin), Create's backtanks live in the Curios "back" slot:
 * every place of the mod (and Create's backpack renderer) that asks for the chest stack gets the worn one when there
 * is one.
 */
public final class OctoRigBackSlot {
    private OctoRigBackSlot() {
    }

    /** Octo Arms tentacle rigs and Create's copper/netherite backtanks: everything worn on the back. */
    public static boolean isRig(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return (id.getNamespace().equals("tentaworks") && id.getPath().endsWith("_rig"))
                || (id.getNamespace().equals("create") && id.getPath().endsWith("_backtank"));
    }

    /** Octo Arms tentacle rigs: they are installed into a Chromeware Skeleton socket. */
    public static boolean isTentacleRig(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("tentaworks")
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_tentacle_rig");
    }

    /** Implant capacity a tentacle rig takes: copper 20, netherite 32. */
    public static int rigCapacity(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().startsWith("netherite") ? 32 : 20;
    }

    private static Method limbsGet;
    private static Method limbMissing;
    private static Object bodyPart;
    private static boolean limbsBroken;

    /** True when the Chromeware body has no torso: there is no back left to carry anything on. */
    public static boolean torsoMissing(LivingEntity entity) {
        if (limbsBroken || !(entity instanceof Player player)) return false;
        try {
            if (limbsGet == null) {
                limbsGet = Class.forName("com.chromeware.body.LimbAttachments").getMethod("get", Player.class);
                Class<?> part = Class.forName("com.chromeware.body.BodyPart");
                bodyPart = part.getField("BODY").get(null);
                limbMissing = Class.forName("com.chromeware.body.LimbState").getMethod("isMissing", part);
            }
            return (boolean) limbMissing.invoke(limbsGet.invoke(null, player), bodyPart);
        } catch (Throwable t) {
            limbsBroken = true;
            return false;
        }
    }

    private static final int SKELETON = 2;
    private static Method implantsGet;
    private static Method slotsList;
    private static Method installedItem;
    private static boolean implantsBroken;

    /** The Chromeware Skeleton sockets of the player as a raw CyberwareSlots, or null. */
    public static Object skeletonSlots(Player player) {
        if (implantsBroken) return null;
        try {
            if (implantsGet == null) {
                implantsGet = Class.forName("com.chromeware.cyberware.InventoryImplants")
                        .getMethod("get", Player.class, int.class);
            }
            return implantsGet.invoke(null, player, SKELETON);
        } catch (Throwable t) {
            implantsBroken = true;
            return null;
        }
    }

    private record Found(int index, ItemStack copy) {
    }

    /** What the server handed out for a player: Chromeware's InstalledImplant.item() is a copy, so edits must be written back. */
    private static final class Held {
        final int slot;
        final ItemStack stack;
        int written;

        Held(int slot, ItemStack stack, int written) {
            this.slot = slot;
            this.stack = stack;
            this.written = written;
        }
    }

    private static final Map<UUID, Held> HELD = new ConcurrentHashMap<>();
    private static Method slotsWith;
    private static Constructor<?> implantCtor;
    private static Supplier<AttachmentType<Object>> skeletonType;

    private static Found find(Object slots) {
        try {
            if (slotsList == null) slotsList = slots.getClass().getMethod("slots");
            List<?> list = (List<?>) slotsList.invoke(slots);
            for (int i = 0; i < list.size(); i++) {
                Object installed = list.get(i);
                if (installedItem == null) installedItem = installed.getClass().getMethod("item");
                if (installedItem.invoke(installed) instanceof ItemStack stack && isTentacleRig(stack)) {
                    return new Found(i, stack);
                }
            }
        } catch (Throwable t) {
            implantsBroken = true;
        }
        return null;
    }

    /**
     * The tentacle rig installed in the Skeleton sockets of the player, or empty. On the server the very same stack object
     * is returned until the sockets change, so spending its air sticks; {@link #flush} writes the change back.
     */
    public static ItemStack installedRig(LivingEntity entity) {
        if (!(entity instanceof Player player)) return ItemStack.EMPTY;
        Object slots = skeletonSlots(player);
        if (slots == null) return ItemStack.EMPTY;
        Found found = find(slots);
        if (found == null) {
            if (player instanceof ServerPlayer) HELD.remove(player.getUUID());
            return ItemStack.EMPTY;
        }
        if (!(player instanceof ServerPlayer)) return found.copy();
        int hash = found.copy().getComponents().hashCode();
        Held held = HELD.get(player.getUUID());
        if (held != null && held.slot == found.index() && held.written == hash
                && held.stack.getItem() == found.copy().getItem()) {
            return held.stack;
        }
        held = new Held(found.index(), found.copy(), hash);
        HELD.put(player.getUUID(), held);
        return held.stack;
    }

    /** Writes the handed-out rig back into the Skeleton sockets (and syncs it to the client) when its components changed. */
    @SuppressWarnings("unchecked")
    public static void flush(ServerPlayer player) {
        Held held = HELD.get(player.getUUID());
        if (held == null || implantsBroken) return;
        int now = held.stack.getComponents().hashCode();
        if (now == held.written) return;
        Object slots = skeletonSlots(player);
        Found found = slots == null ? null : find(slots);
        if (found == null || found.index() != held.slot) {
            HELD.remove(player.getUUID());
            return;
        }
        try {
            if (implantCtor == null) {
                Class<?> installed = Class.forName("com.chromeware.cyberware.InstalledImplant");
                implantCtor = installed.getConstructor(ItemStack.class);
                slotsWith = slots.getClass().getMethod("with", int.class, installed);
                skeletonType = (Supplier<AttachmentType<Object>>) Class.forName("com.chromeware.cyberware.PlayerData")
                        .getField("SKELETON").get(null);
            }
            Object updated = slotsWith.invoke(slots, held.slot, implantCtor.newInstance(held.stack.copy()));
            player.setData(skeletonType, updated);
            held.written = now;
        } catch (Throwable t) {
            implantsBroken = true;
        }
    }

    /** Replaces the rig in the implant sockets by an edited copy (the caller changed {@code updated}) and syncs it to the client. */
    @SuppressWarnings("unchecked")
    public static boolean replace(ServerPlayer player, ItemStack updated) {
        Held held = HELD.get(player.getUUID());
        if (held == null || implantsBroken) return false;
        Object slots = skeletonSlots(player);
        Found found = slots == null ? null : find(slots);
        if (found == null || found.index() != held.slot) return false;
        try {
            if (implantCtor == null) {
                Class<?> installed = Class.forName("com.chromeware.cyberware.InstalledImplant");
                implantCtor = installed.getConstructor(ItemStack.class);
                slotsWith = slots.getClass().getMethod("with", int.class, installed);
                skeletonType = (Supplier<AttachmentType<Object>>) Class.forName("com.chromeware.cyberware.PlayerData")
                        .getField("SKELETON").get(null);
            }
            Object changed = slotsWith.invoke(slots, held.slot, implantCtor.newInstance(updated.copy()));
            player.setData(skeletonType, changed);
            HELD.put(player.getUUID(), new Held(held.slot, updated, updated.getComponents().hashCode()));
            return true;
        } catch (Throwable t) {
            com.mojang.logging.LogUtils.getLogger().warn("Cannot write the rig into the implant sockets", t);
            return false;
        }
    }

    public static void forget(UUID player) {
        HELD.remove(player);
    }

    /** The stack that {@code entity.getItemBySlot(slot)} should answer: the rig worn in the back slot, for the chest. */
    public static ItemStack stackFor(LivingEntity entity, EquipmentSlot slot, ItemStack original) {
        if (slot != EquipmentSlot.CHEST) return original;
        if (torsoMissing(entity)) return isRig(original) ? ItemStack.EMPTY : original;
        ItemStack implanted = installedRig(entity);
        if (!implanted.isEmpty()) return implanted;
        // a tentacle rig counts only as an implant, never from the chest slot
        if (isTentacleRig(original)) original = ItemStack.EMPTY;
        var inventory = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (inventory == null) return original;
        var handler = inventory.getStacksHandler("back").orElse(null);
        if (handler == null) return original;
        var stacks = handler.getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (isRig(stack) && !isTentacleRig(stack)) return stack;
        }
        return original;
    }

    /** Puts the rig into the first free back slot (right-click from hand). Returns whether it was moved. */
    public static boolean equipToBack(LivingEntity entity, ItemStack stack) {
        var inventory = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (inventory == null) return false;
        var handler = inventory.getStacksHandler("back").orElse(null);
        if (handler == null) return false;
        var stacks = handler.getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStackInSlot(i).isEmpty()) {
                stacks.setStackInSlot(i, stack.copyWithCount(1));
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
