package dev.qur1s.spellclasses;

import dev.qur1s.spellclasses.mixin.ItemComponentsAccessor;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Rebuilds the default attribute_modifiers component on a handful of cataclysm_spellbooks items
 * (Nature/Light school buffs -> Sand-relevant or removed), reusing the same Item.components()
 * field-swap trick KubeJS's own mixin exposes to scripts - done in plain Java here since three
 * rounds of getting the JS record-conversion shape right for this API failed silently (empty
 * modifier lists / no thrown error) without a way to runtime-test in this environment.
 */
@EventBusSubscriber(modid = "spellclasses", bus = EventBusSubscriber.Bus.MOD)
public final class StaffAttributeOverrides {
    private StaffAttributeOverrides() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("spellclasses", path);
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Fake Wadjet's Staff - Nature+Light removed, Nature 25% only (Sand school reads the
            // same nature_spell_power attribute for its damage - see CSSchoolRegistry.SAND).
            override("cataclysm_spellbooks", "fake_wudjets_staff", ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.COOLDOWN_REDUCTION, new AttributeModifier(id("wadjet_cooldown"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(id("wadjet_sand_power"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .build());

            // Bloom Stone Staff - Nature buff 15% -> 25%
            override("cataclysm_spellbooks", "bloom_stone_staff", ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.COOLDOWN_REDUCTION, new AttributeModifier(id("bloom_stone_cooldown"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(id("bloom_stone_nature_power"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .build());

            // Desert Spellbook (Гримуар Песка) - Light buff removed, Nature/Mana unchanged
            override("cataclysm_spellbooks", "desert_spell_book", ItemAttributeModifiers.builder()
                    .add(AttributeRegistry.MAX_MANA, new AttributeModifier(id("desert_book_mana"), 300.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(id("desert_book_nature_power"), 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .build());

            // Полное Одеяние Фараона - Light buff removed on all 4 pieces, everything else unchanged
            // (defense/toughness/knockback from CSArmorMaterialRegistry.CURSIUM_WARLOCK_ARMOR: 4/9/7/4, 3.0, 0.1)
            overridePharaohPiece("pharaoh_helmet", EquipmentSlotGroup.HEAD, 4.0);
            overridePharaohPiece("pharaoh_chestplate", EquipmentSlotGroup.CHEST, 9.0);
            overridePharaohPiece("pharaoh_leggings", EquipmentSlotGroup.LEGS, 7.0);
            overridePharaohPiece("pharaoh_greaves", EquipmentSlotGroup.FEET, 4.0);

            // Трость Изобретателя - перенесена в раздел Шторма: вместо 10% общей Силы заклинаний - 20% Силы заклинаний Молний
            override("irons_spellbooks", "artificer_cane", ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.CAST_TIME_REDUCTION, new AttributeModifier(id("artificer_cane_cast_time"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.COOLDOWN_REDUCTION, new AttributeModifier(id("artificer_cane_cooldown"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .add(AttributeRegistry.LIGHTNING_SPELL_POWER, new AttributeModifier(id("artificer_cane_lightning_power"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
                    .build());

            // Броня Инженера - перенесена в раздел Шторма: бафф Молния 20%, статы подняты до
            // Незеритового тира (ванильный незерит: 3/8/6/3, toughness 3.0, kb 0.1)
            overrideEngineerPiece("engineer_hood", EquipmentSlotGroup.HEAD, 3.0);
            overrideEngineerPiece("engineer_suit", EquipmentSlotGroup.CHEST, 8.0);
            overrideEngineerPiece("engineer_leggings", EquipmentSlotGroup.LEGS, 6.0);
            overrideEngineerPiece("engineer_boots", EquipmentSlotGroup.FEET, 3.0);

            // Броня Пугала (irons_spellbooks:pumpkin_*) - статы Брони Инженера, но вместо Молнии бафф Крови 20%
            overrideScarecrowPiece("pumpkin_helmet", EquipmentSlotGroup.HEAD, 3.0);
            overrideScarecrowPiece("pumpkin_chestplate", EquipmentSlotGroup.CHEST, 8.0);
            overrideScarecrowPiece("pumpkin_leggings", EquipmentSlotGroup.LEGS, 6.0);
            overrideScarecrowPiece("pumpkin_boots", EquipmentSlotGroup.FEET, 3.0);

            // Нагрудник Светоносца (Iron's Spellbooks, paladin_chestplate) - броня 8 -> 16, остальные
            // атрибуты предмета (мана, сила заклинаний, стойкость...) остаются как есть
            overrideArmorValue("irons_spellbooks", "paladin_chestplate", 16.0);
            overrideLightbringerChestplate();

        });
    }

    private static void overrideScarecrowPiece(String path, EquipmentSlotGroup slot, double defense) {
        override("irons_spellbooks", path, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id(path + "_armor"), defense, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id(path + "_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id(path + "_knockback"), 0.1, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.MAX_MANA, new AttributeModifier(id(path + "_mana"), 125.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.BLOOD_SPELL_POWER, new AttributeModifier(id(path + "_blood_power"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .add(AttributeRegistry.SPELL_POWER, new AttributeModifier(id(path + "_spell_power"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .build());
    }

    private static void overrideEngineerPiece(String path, EquipmentSlotGroup slot, double defense) {
        override("cataclysm_spellbooks", path, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id(path + "_armor"), defense, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id(path + "_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id(path + "_knockback"), 0.1, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.MAX_MANA, new AttributeModifier(id(path + "_mana"), 125.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.LIGHTNING_SPELL_POWER, new AttributeModifier(id(path + "_lightning_power"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .add(AttributeRegistry.SPELL_POWER, new AttributeModifier(id(path + "_spell_power"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .build());
    }

    private static void overridePharaohPiece(String path, EquipmentSlotGroup slot, double defense) {
        override("cataclysm_spellbooks", path, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id(path + "_armor"), defense, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id(path + "_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id(path + "_knockback"), 0.1, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.MAX_MANA, new AttributeModifier(id(path + "_mana"), 150.0, AttributeModifier.Operation.ADD_VALUE), slot)
                .add(AttributeRegistry.NATURE_SPELL_POWER, new AttributeModifier(id(path + "_nature_power"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .add(AttributeRegistry.SPELL_POWER, new AttributeModifier(id(path + "_spell_power"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), slot)
                .build());
    }

    /** The same modifiers with the ARMOR entry set to {@code armor}; every other entry is untouched. */
    public static ItemAttributeModifiers withArmor(ItemAttributeModifiers current, double armor, EquipmentSlotGroup slot, ResourceLocation fallbackId) {
        ItemAttributeModifiers.Builder rebuilt = ItemAttributeModifiers.builder();
        boolean found = false;
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            AttributeModifier modifier = entry.modifier();
            if (entry.attribute().is(Attributes.ARMOR)) {
                modifier = new AttributeModifier(modifier.id(), armor, modifier.operation());
                found = true;
            }
            rebuilt.add(entry.attribute(), modifier, entry.slot());
        }
        if (!found) {
            rebuilt.add(Attributes.ARMOR, new AttributeModifier(fallbackId, armor, AttributeModifier.Operation.ADD_VALUE), slot);
        }
        return rebuilt.build();
    }

    /** Chestplate: +150 mana and +15% Holy spell power instead of Iron's own mana / general spell power; defence etc. kept. */
    private static void overrideLightbringerChestplate() {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "paladin_chestplate");
        Item item = BuiltInRegistries.ITEM.get(key);
        if (item == Items.AIR) return;
        ItemAttributeModifiers current = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (current == null) current = item.getDefaultAttributeModifiers();
        ItemAttributeModifiers.Builder rebuilt = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (entry.attribute().is(AttributeRegistry.MAX_MANA.getId()) || entry.attribute().is(AttributeRegistry.SPELL_POWER.getId())
                    || entry.attribute().is(AttributeRegistry.HOLY_SPELL_POWER.getId())) continue;
            rebuilt.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        rebuilt.add(AttributeRegistry.MAX_MANA, new AttributeModifier(id("paladin_chestplate_mana"), dev.qur1s.spellclasses.item.ArmorItems.MANA, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.CHEST);
        rebuilt.add(AttributeRegistry.HOLY_SPELL_POWER, new AttributeModifier(id("paladin_chestplate_holy_power"), dev.qur1s.spellclasses.item.ArmorItems.HOLY_POWER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.CHEST);
        override("irons_spellbooks", "paladin_chestplate", rebuilt.build());
    }

    /** Replaces only the ARMOR entry of an item's attribute modifiers (component), keeping every other one. */
    private static void overrideArmorValue(String namespace, String path, double armor) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (item == Items.AIR) return;
        ItemAttributeModifiers current = item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (current == null) {
            current = item.getDefaultAttributeModifiers();
        }
        override(namespace, path, withArmor(current, armor, EquipmentSlotGroup.CHEST, id(path + "_armor")));
    }

    private static void override(String namespace, String path, ItemAttributeModifiers modifiers) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (item == Items.AIR) return;

        DataComponentMap.Builder builder = DataComponentMap.builder().addAll(item.components());
        builder.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        ((ItemComponentsAccessor) item).spellclasses$setComponents(builder.build());
    }
}
