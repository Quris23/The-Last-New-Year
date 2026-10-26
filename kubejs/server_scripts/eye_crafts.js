// kubejs/server_scripts/eye_crafts.js
ServerEvents.recipes(event => {

    // Око меха
    let transitional = 'kubejs:incomplete_mech_eye'
    let ironBlocks = ['minecraft:iron_block', 'create:industrial_iron_block', 'create:weathered_iron_block']
    event.recipes.create.sequenced_assembly([
        CreateItem.of('cataclysm:mech_eye', 0.80),
        CreateItem.of('create:industrial_iron_block', 0.10),
        CreateItem.of('create:weathered_iron_block', 0.10)
    ], 'minecraft:ender_eye', [
        event.recipes.createFilling(transitional, [transitional, Fluid.of('minecraft:lava', 250)]),
        event.recipes.createDeploying(transitional, [transitional, 'minecraft:redstone_block']),
        event.recipes.createDeploying(transitional, [transitional, ironBlocks])
    ]).transitionalItem(transitional).loops(4)

    // Око Пустыни
    let flesh = ['born_in_chaos_v1:smoked_flesh', 'born_in_chaos_v1:smoked_monster_flesh']
    event.shaped('cataclysm:desert_eye', [
        'GCM',
        'DEK',
        'RSB'
    ], {
        G: 'minecraft:gold_block',
        S: 'minecraft:chiseled_sandstone',
        C: 'legendary_monsters:crystal_of_sandstorm',
        M: 'minecraft:emerald_block',
        B: 'legendary_monsters:dinosaur_bone',
        E: 'minecraft:ender_eye',
        D: 'minecraft:dead_bush',
        K: 'quark:cactus_block',
        R: flesh
    })

    // Око чудовища
    event.shaped('cataclysm:monstrous_eye', [
        'bsb',
        'dEd',
        'bsb'
    ], {
        b: 'lava_bucket',
        s: 'netherite_scrap',
        d: 'born_in_chaos_v1:fire_dust',
        E: 'ender_eye'
    })

    // Око Глубины
    event.shaped('cataclysm:abyss_eye', [
        'Oeo',
        'AEA',
        'oeO'
    ], {
        O: 'crying_obsidian',
        e: 'born_in_chaos_v1:sea_terror_eye',
        o: 'obsidian',
        A: 'alexscaves:abyssmarine',
        E: 'ender_eye'
    })

    // Око Бури
    event.shaped('cataclysm:storm_eye', [
        'src',
        'dEd',
        'SbS'
    ], {
        s: 'prismarine_shard',
        r: 'lightning_rod',
        c: 'prismarine_crystals',
        d: 'northstar:polished_diamond',
        E: 'minecraft:ender_eye',
        S: 'born_in_chaos_v1:spiny_shell',
        b: 'water_bucket'
    })

    // Око Огня
    event.shaped('cataclysm:flame_eye', [
        'LLL',
        'NEN',
        'SSS'
    ], {
        L: 'cataclysm:lava_power_cell',
        N: 'minecraft:netherite_scrap',
        E: 'minecraft:ender_eye',
        S: ['minecraft:soul_sand', 'minecraft:soul_soil']
    })

    // Око Пустоты
    event.shaped('cataclysm:void_eye', [
        'PLP',
        'VHV',
        'PLP'
    ], {
        H: 'legendary_monsters:eye_of_chorus',
        L: 'northstar:moon_stone_lamp',
        V: 'cataclysm:void_lantern_block',
        P: 'born_in_chaos_v1:phantom_powder'
    })

    // Око проклятия
    event.shaped('cataclysm:cursed_eye', [
        'GVG',
        'MEM',
        'GVG'
    ], {
        G: 'minecraft:gold_ingot',
        V: 'cataclysm:void_core',
        M: 'minecraft:phantom_membrane',
        E: 'minecraft:ender_eye'
    })

    // Око Безграничного Знания
    let crypticTransitional = 'kubejs:incomplete_cryptic_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:cryptic_eye', 0.80),
        CreateItem.of('create_enchantment_industry:enchanting_template', 0.10),
        CreateItem.of('create_enchantment_industry:super_enchanting_template', 0.10)
    ], 'minecraft:ender_eye', [
        event.recipes.createFilling(crypticTransitional, [crypticTransitional, Fluid.of('create_enchantment_industry:experience', 1000)]),
        event.recipes.createDeploying(crypticTransitional, [crypticTransitional, 'minecraft:enchanted_book']),
        event.recipes.createDeploying(crypticTransitional, [crypticTransitional, 'create_enchantment_industry:experience_cake_slice']),
        event.recipes.createFilling(crypticTransitional, [crypticTransitional, Fluid.of('create_enchantment_industry:experience', 1000)]),
        event.recipes.createDeploying(crypticTransitional, [crypticTransitional, 'irons_spellbooks:eldritch_manuscript']),
        event.recipes.createDeploying(crypticTransitional, [crypticTransitional, 'create_enchantment_industry:super_enchanting_template'])
    ]).transitionalItem(crypticTransitional).loops(5)

    // Око Истинной Скверны
    let trans = 'kubejs:incomplete_lost_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:lost_eye', 0.80),
        CreateItem.of('born_in_chaos_v1:orbofthe_summoner', 0.07),
        CreateItem.of('born_in_chaos_v1:transformative_flower', 0.07),
        CreateItem.of('alexscaves:darkened_apple', 0.06)
    ], 'minecraft:ender_eye', [
        event.recipes.createDeploying(trans, [trans, 'born_in_chaos_v1:orbofthe_summoner']),
        event.recipes.createFilling(trans, [trans, Fluid.of('kubejs:chaos_fluid', 500)]),
        event.recipes.createDeploying(trans, [trans, 'alexscaves:darkened_apple']),
        event.recipes.createDeploying(trans, [trans, 'born_in_chaos_v1:fel_soil']),
        event.recipes.createFilling(trans,  [trans, Fluid.of('irons_spellbooks:blood', 500)]),
        event.recipes.createDeploying(trans, [trans, 'born_in_chaos_v1:transformative_flower']),
    ]).transitionalItem(trans).loops(1)

    // Око Потерянной Технологии
    event.recipes.create.mechanical_crafting('endrem:corrupted_eye', [
        ' SRS ',
        'APVPA',
        'RVDVR',
        'APVPA',
        ' SRS '
    ], {
        D: 'minecraft:ender_eye',
        V: 'cataclysm:witherite_ingot',
        P: 'create:precision_mechanism',
        R: 'minecraft:redstone_block',
        S: 'alexscaves:azure_neodymium_ingot',
        A: 'alexscaves:scarlet_neodymium_ingot'
    })

    // Око Забытой Древности
    event.shaped('endrem:old_eye', [
        'TSA',
        'GEG',
        'ANT'
    ], {
        T: 'alexscaves:tectonic_shard',
        S: 'cataclysm:remnant_skull',
        A: 'kubejs:aurumite_ingot',
        G: 'minecraft:gold_block',
        E: 'minecraft:ender_eye',
        N: 'minecraft:netherite_scrap'
    })

    // Око Слепой Одержимости
    event.shaped('endrem:undead_eye', [
        'RSe',
        'sEs',
        'eMR'
    ], {
        R: 'born_in_chaos_v1:potion_of_rampage',
        S: 'legendary_monsters:corrupted_soul',
        s: 'cold_sweat:soul_sprout',
        e: 'born_in_chaos_v1:ethereal_spirit',
        E: 'minecraft:ender_eye',
        M: 'create:haunted_bell'
    })

    //Око Абсолютной Тишины
    event.shaped('endrem:black_eye', [
        'ABE',
        'CPC',
        'ESA'
    ], {
        A: 'deeperdarker:reinforced_echo_shard',
        B: 'deeperdarker:heart_of_the_deep',
        E: 'echoing_magic:echoed_manuscript',
        C: 'minecraft:sculk_catalyst',
        P: 'archaion:impact_pearl',
        S: 'deeperdarker:soul_crystal'
    })

    // Око Первого Разлома
    let ore = ['deepslate_diamond_ore', 'deepslate_emerald_ore', 'createpropulsion:deepslate_platinum_ore']
    event.shaped('endrem:cold_eye', [
        'dEd',
        'hCs',
        'dId'
    ], {
        d: ore,
        E: 'cataclysm:abyssal_egg',
        h: 'heart_of_the_sea',
        C: 'ender_eye',
        s: 'alexscaves:magic_conch',
        I: 'alexscaves:immortal_embryo'
    })

    // Око Вечных Бурь
    let trans3 = 'kubejs:incomplete_wither_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:wither_eye', 0.80),
        CreateItem.of('2x legendary_monsters:air_rune', 0.20)
    ], 'irons_spellbooks:energized_core', [ 
        event.recipes.createFilling(trans3, [trans3, Fluid.of('kubejs:cloud_fluid', 1000)]),
        event.recipes.createDeploying(trans3, [trans3, 'irons_spellbooks:mithril_weave']),
        event.recipes.createFilling(trans3,  [trans3, Fluid.of('aeronautics:levitite_blend', 1000)]),
        event.recipes.createDeploying(trans3, [trans3, 'legendary_monsters:cloud_rod']),
        event.recipes.createFilling(trans3,  [trans3, Fluid.of('create_wizardry:lightning', 500)]),
        event.recipes.createDeploying(trans3, [trans3, 'legendary_monsters:air_rune']),
    ]).transitionalItem(trans3).loops(2)

    // Око Семи Морей
    event.shaped('endrem:exotic_eye', [
        'tef',
        'sEw',
        'lCt'
    ], {
        t: 'tide_armor_trim_smithing_template',
        e: 'cataclysm:essence_of_the_storm',
        f: 'alexscaves:floater',
        s: 'sponge',
        E: 'ender_eye',
        w: 'wet_sponge',
        l: 'cataclysm:lacrima',
        C: 'conduit'
    })

    // Око Адского Пламени
    let trans1 = 'kubejs:incomplete_nether_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:nether_eye', 0.80),
        CreateItem.of('2x cataclysm:ignitium_ingot', 0.19),
        CreateItem.of('4x netherite_scrap', 0.01)
    ], 'legendary_monsters:eye_of_magma', [ 
        event.recipes.createDeploying(trans1, [trans1, 'cataclysm:ignitium_ingot']),
        event.recipes.createFilling(trans1, [trans1, Fluid.of('create_wizardry:fire_ale', 1000)]),
        event.recipes.createDeploying(trans1, [trans1, 'irons_spellbooks:cinderous_soulcaller']),
        event.recipes.createDeploying(trans1, [trans1, 'alexscaves:primal_magma']),
        event.recipes.createFilling(trans1,  [trans1, Fluid.of('lava', 1000)]),
        event.recipes.createDeploying(trans1, [trans1, 'create:blaze_cake']),
    ]).transitionalItem(trans1).loops(2)

    // Око Фрактального Краха
    let trans2 = 'kubejs:incomplete_rogue_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:rogue_eye', 0.80),
        CreateItem.of('2x legendary_monsters:portal_shard', 0.2)
    ], 'aerowarptics:singularity_core', [ 
        event.recipes.createFilling(trans2, [trans2, Fluid.of('kubejs:annihilation_fluid', 1000)]),
        event.recipes.createDeploying(trans2, [trans2, 'alexscaves:acidic_radrock']),
        event.recipes.createFilling(trans2, [trans2, Fluid.of('goingnuclear:uranyl_nitrate', 1000)]),
        event.recipes.createDeploying(trans2, [trans2, 'northstar:martian_steel_ingot']),
        event.recipes.createFilling(trans2, [trans2, Fluid.of('aerowarptics:rift_essence', 500)]),
        event.recipes.createDeploying(trans2, [trans2, 'legendary_monsters:portal_shard']),
    ]).transitionalItem(trans2).loops(2)

    // Око Непростительного Проклятия
    let transitional1 = 'kubejs:incomplete_cursed_eye'
    let lamp = ['born_in_chaos_v1:fel_lamp', 'born_in_chaos_v1:lord_pumpkinheads_lamp']
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:cursed_eye', 0.80),
        CreateItem.of('2x cataclysm:cursium_ingot', 0.20)
    ], 'minecraft:ender_eye', [
        event.recipes.createDeploying(transitional1, [transitional1, 'irons_spellbooks:brazier_soul']),
        event.recipes.createDeploying(transitional1, [transitional1, 'born_in_chaos_v1:smoldering_scorched_wood']),
        event.recipes.createFilling(transitional1, [transitional1, Fluid.of('fluid:haunting_fluid', 500)]),
        event.recipes.createDeploying(transitional1, [transitional1, lamp]),
        event.recipes.createDeploying(transitional1, [transitional1, 'cataclysm:black_steel_ingot']),
        event.recipes.createDeploying(transitional1, [transitional1, 'fermented_spider_eye']),
    ]).transitionalItem(transitional1).loops(2)

})