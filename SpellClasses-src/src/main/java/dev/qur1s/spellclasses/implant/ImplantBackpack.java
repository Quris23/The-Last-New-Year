package dev.qur1s.spellclasses.implant;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The hidden backpack of a player (server side only). The implanted Sophisticated upgrades are mirrored into its upgrade slots every
 * few ticks and the backpack is handed to Sophisticated as one more place where a player carries a backpack, so every upgrade
 * (magnet, pump, feeding...) works exactly as in a worn backpack. The implants stay the source of truth: when an upgrade changes
 * inside the backpack (its settings, the fluid in a tank) the changed stack is written back into the socket.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.GAME)
public final class ImplantBackpack {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    public static final String HANDLER = "spellclasses_implants";

    private static final Map<UUID, ItemStack> STACKS = new ConcurrentHashMap<>();
    /** The client's copy of its own player's hidden backpack: only the id matters, the contents come with the screen. */
    private static final Map<UUID, ItemStack> CLIENT_STACKS = new ConcurrentHashMap<>();
    /** Last seen component hash of every upgrade slot of a player's backpack. */
    private static final Map<UUID, int[]> SEEN = new ConcurrentHashMap<>();

    private ImplantBackpack() {
    }

    @EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        @SubscribeEvent
        static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                PlayerInventoryProvider provider = PlayerInventoryProvider.get();
                provider.addPlayerInventoryHandler(HANDLER,
                        player -> PlayerInventoryHandler.SINGLE_IDENTIFIER,
                        (player, identifier) -> stack(player).isEmpty() ? 0 : 1,
                        (player, identifier, slot) -> stack(player),
                        false, false, false, false);
                moveHandlerLast(provider);
            });
        }
    }

    /**
     * Sophisticated puts every new handler first, so the "toggle the upgrade in slot N" key (it acts on the first backpack found)
     * would always hit this hidden backpack instead of the worn one. The handler is moved to the end.
     */
    @SuppressWarnings("unchecked")
    private static void moveHandlerLast(PlayerInventoryProvider provider) {
        try {
            var field = PlayerInventoryProvider.class.getDeclaredField("playerInventoryHandlers");
            field.setAccessible(true);
            Map<String, Object> handlers = (Map<String, Object>) field.get(provider);
            Object ours = handlers.remove(HANDLER);
            if (ours != null) handlers.put(HANDLER, ours);
        } catch (Throwable t) {
            LOGGER.error("Cannot move the implant backpack handler to the end", t);
        }
    }

    /** The hidden backpack of a player who has implanted upgrades (on the client: its own copy), otherwise empty. */
    public static ItemStack stack(Player player) {
        if (player.level().isClientSide()) {
            if (!hasUpgrades(player)) return ItemStack.EMPTY;
            return CLIENT_STACKS.computeIfAbsent(player.getUUID(), id -> create(player));
        }
        if (!(player instanceof ServerPlayer)) return ItemStack.EMPTY;
        ItemStack stack = STACKS.get(player.getUUID());
        return stack == null || !hasUpgrades(player) ? ItemStack.EMPTY : stack;
    }

    private static int slotOf(ServerPlayer player, int category, int socket) {
        var upgrades = ImplantAccess.upgrades(player);
        for (int i = 0; i < upgrades.size(); i++) {
            if (upgrades.get(i).category() == category && upgrades.get(i).index() == socket) return i;
        }
        return -1;
    }

    /** A button of the pump panel: 0 on/off, 1 receive/give, 2 world, 3 storages, 4 tank in hand. */
    public static void act(ServerPlayer player, int category, int socket, int action) {
        if (action == 0) {
            toggle(player, category, socket);
            return;
        }
        int slot = slotOf(player, category, socket);
        ItemStack backpack = stack(player);
        if (slot < 0 || backpack.isEmpty()) return;
        var wrapper = BackpackWrapper.fromStack(backpack).getUpgradeHandler().getSlotWrappers().get(slot);
        if (!(wrapper instanceof net.p3pp3rf1y.sophisticatedcore.upgrades.pump.PumpUpgradeWrapper pump)) return;
        switch (action) {
            case 1 -> pump.setIsInput(!pump.isInput());
            case 2 -> pump.setInteractWithWorld(!pump.shouldInteractWithWorld());
            case 3 -> pump.setInteractWithFluidHandlers(!pump.shouldInteractWithFluidHandlers());
            case 4 -> pump.setInteractWithHand(!pump.shouldInteractWithHand());
            default -> {
            }
        }
    }

    /** Switches the implant in a socket on or off: a backpack upgrade inside the hidden backpack, our own pump by its own flag. */
    public static void toggle(ServerPlayer player, int category, int socket) {
        for (ImplantAccess.Entry entry : ImplantAccess.listed(player)) {
            if (entry.category() != category || entry.index() != socket) continue;
            if (ImplantUpgrades.forBackpack(entry.stack())) {
                toggle(player, slotOf(player, category, socket));
            } else {
                ItemStack stack = entry.stack();
                boolean on = !ImplantPumps.enabled(stack);
                stack.set(ModCoreDataComponents.ENABLED, on);
                ImplantAccess.write(player, category, socket, stack);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(on
                        ? "gui.sophisticatedbackpacks.status.upgrade_switched_on"
                        : "gui.sophisticatedbackpacks.status.upgrade_switched_off", stack.getHoverName()), true);
            }
            return;
        }
    }

    /** Switches the upgrade in a slot of the hidden backpack on or off. */
    public static void toggle(ServerPlayer player, int upgradeSlot) {
        ItemStack backpack = stack(player);
        if (backpack.isEmpty()) return;
        var wrappers = BackpackWrapper.fromStack(backpack).getUpgradeHandler().getSlotWrappers();
        var upgrade = wrappers.get(upgradeSlot);
        if (upgrade != null && upgrade.canBeDisabled()) {
            upgrade.setEnabled(!upgrade.isEnabled());
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(upgrade.isEnabled()
                    ? "gui.sophisticatedbackpacks.status.upgrade_switched_on"
                    : "gui.sophisticatedbackpacks.status.upgrade_switched_off", upgrade.getUpgradeStack().getHoverName()), true);
        }
    }

    private static boolean hasUpgrades(Player player) {
        return !ImplantAccess.upgrades(player).isEmpty();
    }

    private static ItemStack create(Player player) {
        ItemStack stack = new ItemStack(ImplantBackpackItem.INSTANCE.get());
        UUID id = UUID.nameUUIDFromBytes(("spellclasses:implant_backpack:" + player.getUUID()).getBytes());
        stack.set(ModCoreDataComponents.STORAGE_UUID, id);
        return stack;
    }

    private static int hash(ItemStack stack) {
        return stack.isEmpty() ? 0 : stack.getComponents().hashCode() * 31 + 1;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        ImplantPumps.repair(player);
        FaceImplants.tick(player);
        GauntletImplants.tick(player);
        if (net.neoforged.fml.ModList.get().isLoaded("tentaworks") && net.neoforged.fml.ModList.get().isLoaded("create")) RigAirRefill.tick(player);
        List<ImplantAccess.Entry> upgrades = ImplantAccess.upgrades(player);
        if (upgrades.isEmpty() && !STACKS.containsKey(player.getUUID())) return;
        try {
            ItemStack backpack = STACKS.computeIfAbsent(player.getUUID(), id -> create(player));
            if (upgrades.isEmpty() || player.tickCount % 5 == 0) sync(player, backpack, upgrades);
            if (!upgrades.isEmpty()) ImplantPumps.tick(player, backpack);
            if (!upgrades.isEmpty() && backpack.getItem() instanceof BackpackItem item) {
                item.inventoryTick(backpack, player.level(), player, -1, false);
            }
        } catch (Throwable t) {
            LOGGER.error("Implant backpack failed for {}", player.getGameProfile().getName(), t);
        }
    }

    private static void sync(ServerPlayer player, ItemStack backpack, List<ImplantAccess.Entry> upgrades) {
        IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);
        UpgradeHandler handler = wrapper.getUpgradeHandler();
        int[] seen = SEEN.computeIfAbsent(player.getUUID(), id -> new int[ImplantBackpackItem.UPGRADE_SLOTS]);
        for (int i = 0; i < handler.getSlots(); i++) {
            ImplantAccess.Entry wanted = i < upgrades.size() ? upgrades.get(i) : null;
            ItemStack current = handler.getStackInSlot(i);
            if (wanted == null) {
                if (!current.isEmpty()) handler.setStackInSlot(i, ItemStack.EMPTY);
                seen[i] = 0;
                continue;
            }
            if (!ItemStack.isSameItem(wanted.stack(), current)) {
                handler.setStackInSlot(i, wanted.stack().copy());
                seen[i] = hash(handler.getStackInSlot(i));
                continue;
            }
            int now = hash(current);
            if (now != seen[i]) {
                // the upgrade changed inside the backpack: the socket keeps the new state
                ImplantAccess.write(player, wanted.category(), wanted.index(), current);
                seen[i] = now;
            } else if (hash(wanted.stack()) != seen[i]) {
                handler.setStackInSlot(i, wanted.stack().copy());
                seen[i] = hash(handler.getStackInSlot(i));
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GauntletImplants.forget(event.getEntity().getUUID());
        STACKS.remove(event.getEntity().getUUID());
        CLIENT_STACKS.remove(event.getEntity().getUUID());
        SEEN.remove(event.getEntity().getUUID());
    }
}
