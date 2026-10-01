// kubejs/server_scripts/eye_crafts.js
ServerEvents.recipes(event => {

    // Око пустоты
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

    // Око Иссушающей Бури
    event.shaped('endrem:wither_eye', [
        ' A ',
        'BCD',
        ' A '
    ], {
        A: 'minecraft:wither_skeleton_skull',
        B: 'create:brass_ingot',
        C: 'minecraft:ender_eye',
        D: 'create:experience_nugget'
    })

    //Око Абсолютной Тишины
    event.shaped('endrem:black_eye', [
        'ABA',
        'CDC',
        'AEA'
    ], {
        A: 'deeperdarker:reinforced_echo_shard',
        B: 'deeperdarker:heart_of_the_deep',
        C: 'minecraft:sculk_catalyst',
        D: 'minecraft:ender_eye',
        E: 'deeperdarker:soul_crystal'
    })

    // Око Истинной Скверны
    event.shaped('endrem:lost_eye', [
        'ABA',
        'CDE',
        'AFA'
    ], {
        A: 'born_in_chaos_v1:dark_metal_nugget',
        B: 'alexscaves:darkened_apple',
        C: 'born_in_chaos_v1:dark_atrium',
        D: 'minecraft:ender_eye',
        E: 'born_in_chaos_v1:death_totem',
        F: 'born_in_chaos_v1:orbofthe_summoner'
    })

    // Око Пустыни
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
        K: 'minecraft:cactus',
        R: 'minecraft:rotten_flesh'
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

    // Око огня
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

    // Око Адского Пламени
    event.shaped('endrem:nether_eye', [
        'MIV',
        'NEN',
        'VIM'
    ], {
        M: 'alexscaves:primal_magma',
        I: 'cataclysm:ignitium_ingot',
        N: 'minecraft:netherite_ingot',
        E: 'minecraft:ender_eye',
        V: 'create:blaze_cake'
    })

    // Око Непростительного Проклятия
    let transitional = 'kubejs:incomplete_cursed_eye'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('endrem:cursed_eye', 0.80),
        CreateItem.of('2x cataclysm:cursium_ingot', 0.20)
    ], 'minecraft:ender_eye', [
        event.recipes.createDeploying(transitional, [transitional, 'cataclysm:cursium_ingot']),
        event.recipes.createDeploying(transitional, [transitional, 'alexscaves:uranium']),
        event.recipes.createFilling(transitional, [transitional, Fluid.of('fluid:haunting_fluid', 500)]),
        event.recipes.createDeploying(transitional, [transitional, 'minecraft:spider_eye']),
        event.recipes.createDeploying(transitional, [transitional, 'cataclysm:black_steel_ingot']),
        event.recipes.createPressing(transitional, transitional)
    ]).transitionalItem(transitional).loops(2)

    // Око меха
    transitional = 'kubejs:incomplete_mech_eye'
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
        event.recipes.createDeploying(crypticTransitional, [crypticTransitional, 'create_enchantment_industry:super_enchanting_template']),
        event.recipes.createPressing(crypticTransitional, crypticTransitional)
    ]).transitionalItem(crypticTransitional).loops(5)

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

})