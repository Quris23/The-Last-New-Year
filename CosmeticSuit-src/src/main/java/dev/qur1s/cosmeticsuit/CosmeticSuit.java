package dev.qur1s.cosmeticsuit;

import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.List;

@Mod(CosmeticSuit.MODID)
public class CosmeticSuit {
    public static final String MODID = "cosmeticsuit";

    /** NorthStar's default spacesuitBaseOxygen: the fill of creative-menu backtanks. */
    private static final int CREATIVE_OXYGEN = 1800;

    public CosmeticSuit(IEventBus modBus) {
        modBus.addListener(CosmeticSuit::onCreativeTab);
    }

    /** Creative-menu copper/netherite backtanks come empty: no Create air, no oxygen. */
    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        var parent = List.copyOf(event.getParentEntries());
        var search = List.copyOf(event.getSearchEntries());
        var items = new java.util.LinkedHashSet<net.minecraft.world.item.Item>();
        for (ItemStack stack : parent) {
            if (!isOxygenBacktank(stack)) continue;
            items.add(stack.getItem());
            event.remove(stack, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
        }
        for (ItemStack stack : search) {
            if (!isOxygenBacktank(stack)) continue;
            items.add(stack.getItem());
            event.remove(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        }
        for (var item : items) {
            event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static final boolean COSARMOR_LOADED = ModList.get().isLoaded("cosmeticarmorreworked");

    /** The stack Northstar should treat as worn in {@code slot}: a space suit piece from the cosmetic slot wins over the real one. */
    public static ItemStack effective(LivingEntity entity, EquipmentSlot slot) {
        ItemStack real = entity.getItemBySlot(slot);
        if (!COSARMOR_LOADED || !(entity instanceof Player player) || !slot.isArmor()) return real;
        CAStacksBase stacks = entity.level().isClientSide()
                ? CosArmorAPI.getCAStacksClient(player.getUUID())
                : CosArmorAPI.getCAStacks(player.getUUID());
        if (stacks == null || slot.getIndex() >= stacks.getSlots()) return real;
        ItemStack cosmetic = stacks.getStackInSlot(slot.getIndex());
        return isSpaceSuit(cosmetic) ? cosmetic : real;
    }

    /** Visual variant: any non-empty cosmetic stack wins over the real one (what Cosmetic Armor Reworked draws on the body). */
    public static ItemStack visual(LivingEntity entity, EquipmentSlot slot) {
        ItemStack real = entity.getItemBySlot(slot);
        if (!COSARMOR_LOADED || !(entity instanceof Player player) || !slot.isArmor()) return real;
        CAStacksBase stacks = CosArmorAPI.getCAStacksClient(player.getUUID());
        if (stacks == null || slot.getIndex() >= stacks.getSlots()) return real;
        ItemStack cosmetic = stacks.getStackInSlot(slot.getIndex());
        return cosmetic.isEmpty() ? real : cosmetic;
    }

    /** Same order as {@link LivingEntity#getArmorSlots()}: feet, legs, chest, head. */
    public static Iterable<ItemStack> effectiveArmor(LivingEntity entity) {
        return List.of(
                effective(entity, EquipmentSlot.FEET),
                effective(entity, EquipmentSlot.LEGS),
                effective(entity, EquipmentSlot.CHEST),
                effective(entity, EquipmentSlot.HEAD));
    }

    private static final ResourceLocation OXYGEN_ID = ResourceLocation.fromNamespaceAndPath("northstar", "oxygen");
    private static final ResourceLocation BACKTANK_AIR_ID = ResourceLocation.fromNamespaceAndPath("create", "banktank_air");
    private static final ResourceLocation NETHERITE_BACKTANK_ID = ResourceLocation.fromNamespaceAndPath("create", "netherite_backtank");
    private static final ResourceLocation COPPER_BACKTANK_ID = ResourceLocation.fromNamespaceAndPath("create", "copper_backtank");

    /** Backtanks that run on NorthStar oxygen instead of Create air. */
    public static boolean isOxygenBacktank(ResourceLocation id) {
        return id.equals(NETHERITE_BACKTANK_ID) || id.equals(COPPER_BACKTANK_ID);
    }

    @SuppressWarnings("unchecked")
    private static DataComponentType<Integer> component(ResourceLocation id) {
        return (DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
    }

    public static boolean isCreateBacktank(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("create") && id.getPath().endsWith("backtank");
    }

    /**
     * Chestplates Punchy's first-person sleeve can't safely draw: it clones the vanilla
     * armor-layer arm and paints it with the item's flat vanilla texture layers, which
     * doesn't exist for GeckoLib-modelled armor (space suits, Iron's Spellbooks robes, ...)
     * or Create backtanks — the sleeve comes out solid black instead.
     */
    public static boolean isPunchySleeveUnsafe(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty()) return false;
        return isCreateBacktank(stack)
                || stack.getItem() instanceof software.bernie.geckolib.animatable.GeoItem
                || hasCustomArmorTexture(stack, entity)
                || isCataclysmSpellbooksArmor(stack);
    }

    /**
     * Cataclysm: Spellbooks' entire armor lineup ships its flat material textures without the
     * vanilla-required "_layer_1"/"_layer_2" suffix (e.g. "ignis_armor.png" instead of
     * "ignis_armor_layer_1.png") - every piece's real look comes from its own dedicated
     * GeckoLib-style armor renderer instead, so Punchy's sleeve painter always finds a missing
     * texture for these and renders solid black.
     */
    private static boolean isCataclysmSpellbooksArmor(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("cataclysm_spellbooks");
    }

    /**
     * True for chestplates whose {@code IItemExtension#getArmorTexture} override swaps in a
     * per-item texture instead of the shared {@link ArmorMaterial} layer texture — Punchy's sleeve
     * painter only ever reads {@code ArmorMaterial.layers()} and never consults this override, so it
     * paints the sleeve with an unrelated (often blank) texture instead, which renders solid black.
     * Many of Cataclysm's boss-tier chestplates (Ignitium's elytra variant included) do this.
     */
    private static boolean hasCustomArmorTexture(ItemStack stack, LivingEntity entity) {
        if (!(stack.getItem() instanceof ArmorItem armorItem) || armorItem.getEquipmentSlot() != EquipmentSlot.CHEST) {
            return false;
        }
        var layers = armorItem.getMaterial().value().layers();
        ArmorMaterial.Layer probeLayer = layers.isEmpty() ? null : layers.get(0);
        try {
            return stack.getItem().getArmorTexture(stack, entity, EquipmentSlot.CHEST, probeLayer, false) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isOxygenBacktank(ItemStack stack) {
        return isOxygenBacktank(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** NorthStar oxygen of a copper/netherite backtank; Create's own air is ignored for these. */
    public static int oxygenOf(ItemStack stack) {
        DataComponentType<Integer> oxygen = component(OXYGEN_ID);
        return oxygen == null ? 0 : stack.getOrDefault(oxygen, 0);
    }

    public static void setOxygen(ItemStack stack, int value) {
        DataComponentType<Integer> oxygen = component(OXYGEN_ID);
        if (oxygen != null) stack.set(oxygen, value);
    }

    private static boolean isSpaceSuit(ItemStack stack) {
        if (stack.isEmpty()) return false;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id.getNamespace().equals("northstar")) return id.getPath().contains("space_suit");
        return id.getNamespace().equals("create") && (id.getPath().startsWith("netherite_diving_") || id.getPath().equals("netherite_backtank"));
    }
}
