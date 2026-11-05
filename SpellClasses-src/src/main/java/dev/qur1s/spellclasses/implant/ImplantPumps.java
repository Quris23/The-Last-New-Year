package dev.qur1s.spellclasses.implant;

import com.chromeware.cyberware.InventoryImplants;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;

/**
 * Blood Pump: while the player is in combat it fills the implanted Tank upgrade with Iron's Spells blood.
 * Mana Pump: every 6 seconds it spends 1 mana of the owner and fills the tank with Create Wizardry's mana fluid.
 */
public final class ImplantPumps {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final int BLOOD_PER_SECOND = 5;
    private static final int MANA_FLUID = 1;
    private static final int MANA_PERIOD_TICKS = 120;

    private static Fluid blood;
    private static Fluid mana;
    private static boolean looked;

    private ImplantPumps() {
    }

    private static Fluid fluid(String namespace, String path) {
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (fluid == Fluids.EMPTY) {
            LOGGER.warn("Implant pump: fluid {}:{} is not registered", namespace, path);
            return null;
        }
        return fluid;
    }

    /** Our pumps keep their on/off switch in the same component Sophisticated uses for its upgrades. */
    public static boolean enabled(ItemStack stack) {
        return stack.getOrDefault(net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents.ENABLED, true);
    }

    private static ItemStack find(ServerPlayer player, Object pump) {
        for (var implant : InventoryImplants.get(player, ImplantUpgrades.CIRCULATORY).slots()) {
            ItemStack stack = implant.item();
            if (!stack.isEmpty() && stack.getItem() == pump) return stack;
        }
        return null;
    }

    private static boolean flag(ItemStack stack, java.util.function.Supplier<net.minecraft.core.component.DataComponentType<Boolean>> type, boolean fallback) {
        return stack.getOrDefault(type, fallback);
    }

    private static final int MEND_XP = 1;
    private static final int GIVE_MAX = 1000;

    /**
     * The same thing Sophisticated's Pump upgrade does with "tanks and pipes": every second the fluid of the tank goes into a fluid
     * handler on one of the six blocks around the block the player stands in (up to a bucketful at once), except Create's Basin.
     */
    private static void give(ServerPlayer player, net.p3pp3rf1y.sophisticatedcore.api.IStorageFluidHandler tank) {
        var level = player.level();
        var origin = player.blockPosition();
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            // Create's Basin ("Чаша") is not a place for blood and mana to pour into
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(origin.relative(dir)).getBlock());
            if (id.getNamespace().equals("create") && id.getPath().equals("basin")) continue;
            var target = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                    origin.relative(dir), dir.getOpposite());
            if (target == null) continue;
            for (int i = 0; i < tank.getTanks(); i++) {
                FluidStack inTank = tank.getFluidInTank(i);
                if (inTank.isEmpty()) continue;
                int accepted = target.fill(new FluidStack(inTank.getFluid(), Math.min(GIVE_MAX, inTank.getAmount())), FluidAction.EXECUTE);
                if (accepted > 0) {
                    tank.drain(new FluidStack(inTank.getFluid(), accepted), FluidAction.EXECUTE, true);
                    return;
                }
            }
        }
    }

    /** The implanted XP pump mends the item in the main hand (if it has Mending) with the player's experience: 1 point every 5 ticks. */
    public static void repair(ServerPlayer player) {
        if (player.tickCount % 5 != 0 || player.totalExperience <= 0) return;
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !held.isDamaged()
                || !net.minecraft.world.item.enchantment.EnchantmentHelper.has(held,
                        net.minecraft.world.item.enchantment.EnchantmentEffectComponents.REPAIR_WITH_XP)) return;
        boolean pump = false;
        for (var implant : InventoryImplants.get(player, ImplantUpgrades.CIRCULATORY).slots()) {
            if (ImplantUpgrades.isXpPump(implant.item())) {
                pump = true;
                break;
            }
        }
        if (!pump) return;
        float ratio = held.getXpRepairRatio();
        if (ratio <= 0) return;
        int needed = (int) Math.ceil(held.getDamageValue() / ratio);
        int spend = Math.min(Math.min(MEND_XP, needed), player.totalExperience);
        if (spend <= 0) return;
        player.giveExperiencePoints(-spend);
        held.setDamageValue(Math.max(0, held.getDamageValue() - Math.max(1, (int) (spend * ratio))));
    }

    static void tick(ServerPlayer player, ItemStack backpack) {
        if (player.tickCount % 20 != 0) return;
        ItemStack bloodPump = find(player, ImplantBackpackItem.BLOOD_PUMP.get());
        ItemStack manaPump = find(player, ImplantBackpackItem.MANA_PUMP.get());
        if (bloodPump == null && manaPump == null) return;
        if (!looked) {
            looked = true;
            blood = fluid("irons_spellbooks", "blood");
            mana = fluid("create_wizardry", "mana");
        }
        var handler = BackpackWrapper.fromStack(backpack).getFluidHandler().orElse(null);
        if (handler == null) return;

        if (bloodPump != null && blood != null) {
            if (Fight.active(player)) {
                handler.fill(new FluidStack(blood, BLOOD_PER_SECOND), FluidAction.EXECUTE, true);
            }
        }
        give(player, handler);
        if (manaPump != null && mana != null) {
            if (player.tickCount % MANA_PERIOD_TICKS == 0) {
                MagicData magic = MagicData.getPlayerMagicData(player);
                FluidStack portion = new FluidStack(mana, MANA_FLUID);
                if (magic.getMana() >= 1.0F && handler.fill(portion, FluidAction.SIMULATE, true) > 0) {
                    magic.addMana(-1.0F);
                    handler.fill(portion, FluidAction.EXECUTE, true);
                }
            }
        }
    }

    /**
     * "The player is fighting", for the Blood Pump: a hit dealt to or taken from a living creature in the last ten seconds. Hitting a
     * training dummy does not count (Chromeware's own combat timer does count it).
     */
    @net.neoforged.fml.common.EventBusSubscriber(modid = "spellclasses", bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
    public static final class Fight {
        private static final java.util.Map<java.util.UUID, Long> LAST = new java.util.concurrent.ConcurrentHashMap<>();
        private static final long DURATION = 200;

        private Fight() {
        }

        static boolean active(ServerPlayer player) {
            Long last = LAST.get(player.getUUID());
            return last != null && player.level().getGameTime() - last <= DURATION;
        }

        private static boolean dummy(net.minecraft.world.entity.Entity entity) {
            return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals("dummmmmmy");
        }

        @net.neoforged.bus.api.SubscribeEvent
        static void onDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
            var victim = event.getEntity();
            var attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer player && attacker != victim && !dummy(victim)) {
                LAST.put(player.getUUID(), player.level().getGameTime());
            }
            if (victim instanceof ServerPlayer player && attacker != null && attacker != victim && !dummy(attacker)) {
                LAST.put(player.getUUID(), player.level().getGameTime());
            }
        }

        @net.neoforged.bus.api.SubscribeEvent
        static void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            LAST.remove(event.getEntity().getUUID());
        }
    }
}
