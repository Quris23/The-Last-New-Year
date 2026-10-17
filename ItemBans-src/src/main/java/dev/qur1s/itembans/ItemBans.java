package dev.qur1s.itembans;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(ItemBans.MODID)
public class ItemBans {
    public static final String MODID = "itembans";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BANNED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BANNED = builder
                .comment("Item ids that are banned: can't be worn, never dropped/looted, hidden from JEI and the creative menu.")
                .defineListAllowEmpty("banned", List.of("minecraft:elytra"), () -> "namespace:item", o -> o instanceof String);
        SPEC = builder.build();
    }

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    static {
        LOOT_MODIFIERS.register("ban", () -> BanLootModifier.CODEC);
    }

    public ItemBans(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        LOOT_MODIFIERS.register(modEventBus);
        modEventBus.addListener(FMLCommonSetupEvent.class, e -> Bans.rebuild(BANNED.get()));
        modEventBus.addListener(ModConfigEvent.Reloading.class, e -> {
            if (e.getConfig().getSpec() == SPEC) {
                Bans.rebuild(BANNED.get());
            }
        });
        modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, ItemBans::hideFromCreative);

        NeoForge.EVENT_BUS.addListener(LivingEquipmentChangeEvent.class, ItemBans::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class, ItemBans::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, ItemBans::onEntityJoin);
    }

    /** Anything banned that ends up in an armor/body slot (player, mob, dispenser) is taken off again. */
    private static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        EquipmentSlot slot = event.getSlot();
        if (entity.level().isClientSide() || slot.getType() == EquipmentSlot.Type.HAND) {
            return;
        }
        ItemStack worn = event.getTo();
        if (!Bans.isBanned(worn)) {
            return;
        }
        ItemStack copy = worn.copy();
        entity.setItemSlot(slot, ItemStack.EMPTY);
        if (entity instanceof Player player) {
            player.getInventory().placeItemBackInInventory(copy);
            player.displayClientMessage(Component.translatable("itembans.banned"), true);
        }
    }

    /** Stops right-click equipping so it doesn't even flicker on. */
    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (Bans.isBanned(stack) && stack.getItem() instanceof Equipable) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            if (!event.getLevel().isClientSide()) {
                event.getEntity().displayClientMessage(Component.translatable("itembans.banned"), true);
            }
        }
    }

    /** Dropped items and item frames (e.g. the End ship's Elytra) holding a banned item never appear. */
    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ItemEntity item) {
            // Items a player threw/dropped themselves are left alone.
            if (Bans.isBanned(item.getItem()) && item.getOwner() == null) {
                event.setCanceled(true);
            }
        } else if (event.getEntity() instanceof ItemFrame frame && Bans.isBanned(frame.getItem())) {
            event.setCanceled(true);
        }
    }

    private static void hideFromCreative(BuildCreativeModeTabContentsEvent event) {
        for (ItemStack stack : new ArrayList<>(event.getParentEntries())) {
            if (Bans.isBanned(stack)) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
        for (ItemStack stack : new ArrayList<>(event.getSearchEntries())) {
            if (Bans.isBanned(stack)) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }
}
