// Забаненные предметы (config/itembans-common.toml) не должны выпадать нигде.
// Глобальный модификатор itembans уже вычищает их при выдаче лута; здесь они дополнительно убраны из самих таблиц,
// чтобы их не было и в просмотре лута. Список таблиц получен сканированием всех модов и kubejs (блоки не включены).
// Нужен мод LootJS; без него блок пропускается.
if (typeof LootJS !== 'undefined') {
  (function () {
    const BANNED_LOOT = {
    'discerning_the_eldritch:entities/pyrium_sheath': ['discerning_the_eldritch:pyrium_sheath'],
    'hazennstuff:chests/additional_catacombs_loot': ['hazennstuff:excalibur_fragment'],
    'hazennstuff:chests/additional_citadel_vault_loot': ['hazennstuff:legionnaire_warlock_axe'],
    'hazennstuff:chests/additional_end_city_treasure_loot': ['hazennstuff:pendant_of_harmony'],
    'hazennstuff:entities/additional_loot/additional_bogged_loot': ['hazennstuff:overgrown_bone'],
    'hazennstuff:entities/additional_loot/additional_cryomancer_loot': ['hazennstuff:permafrost_fragment'],
    'hazennstuff:entities/additional_loot/additional_dead_king_loot': ['hazennstuff:excalibur_fragment'],
    'hazennstuff:entities/additional_loot/additional_elder_guardian_loot': ['hazennstuff:elder_guardian_spine'],
    'hazennstuff:entities/additional_loot/additional_endermen_loot': ['hazennstuff:rod_of_discord'],
    'hazennstuff:entities/additional_loot/additional_evoker_loot': ['hazennstuff:malice'],
    'hazennstuff:entities/additional_loot/additional_illager_loot': ['hazennstuff:silver_scraps'],
    'hazennstuff:entities/additional_loot/additional_phantom_loot': ['hazennstuff:shadow_scale'],
    'hazennstuff:entities/additional_loot/additional_spider_loot': ['hazennstuff:spider_fang'],
    'hazennstuff:entities/additional_loot/additional_stray_loot': ['hazennstuff:permafrost_fragment'],
    'hazennstuff:entities/additional_loot/additional_tyros_loot': ['hazennstuff:cloth_of_the_flamebearer'],
    'hazennstuff:entities/additional_loot/additional_wither_skeleton_loot': ['hazennstuff:charred_bones', 'hazennstuff:nightmare_fuel'],
    'hazennstuff:entities/aegis': ['hazennstuff:the_prefects_ring'],
    'hazennstuff:entities/aptos': ['hazennstuff:blade_of_the_legate', 'hazennstuff:legionnaire_warlock_axe'],
    'hazennstuff:entities/pyromus': ['hazennstuff:the_tribunes_medallion'],
    'hazennstuff:entities/void_wanderer': ['hazennstuff:stardust', 'hazennstuff:starfury'],
    'hazennstuff:misc/cinderous_curios': ['hazennstuff:blade_of_the_legate', 'hazennstuff:the_prefects_ring', 'hazennstuff:the_tribunes_medallion'],
    'hazennstuff:misc/fireblossom_gown': ['hazennstuff:fireblossom_gown_boots', 'hazennstuff:fireblossom_gown_chestplate', 'hazennstuff:fireblossom_gown_helmet', 'hazennstuff:fireblossom_gown_leggings'],
    'incendium:artifact/tool/infernal_wings': ['minecraft:elytra'],
    'nova_structures:chests/end_castle/vault_brigattine': ['minecraft:elytra'],
    'nova_structures:chests/end_castle/vault_galleon': ['minecraft:elytra'],
    'nova_structures:chests/end_castle/vault_slope': ['minecraft:elytra'],
    'somakespells:entities/fire_ghost': ['somakespells:tormented_spirit'],
    'somakespells:entities/infernal_blaze': ['somakespells:infernal_blaze_rod'],
    'somakespells:entities/soul_blaze': ['somakespells:soul_blaze_rod']
    }

    LootJS.lootTables(event => {
        Object.keys(BANNED_LOOT).forEach(id => {
            if (!event.hasLootTable(id)) return
            event.getLootTable(id).removeItem(BANNED_LOOT[id])
        })
    })
  })()
}
