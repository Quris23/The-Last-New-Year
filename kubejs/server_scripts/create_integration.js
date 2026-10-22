// kubejs/server_scripts/create_integration.js
ServerEvents.recipes(event => {

    // Жидкий Хаос
    event.custom({
    type: 'create:emptying',
    ingredients: [
        { item: 'born_in_chaos_v1:chaos_component' }
    ],
    results: [
        { id: 'minecraft:glass_bottle' },
        { id: 'kubejs:chaos_fluid', amount: 250 }
    ]
    })

    event.recipes.createFilling('born_in_chaos_v1:chaos_component', [
        'minecraft:glass_bottle',
        Fluid.of('kubejs:chaos_fluid', 250)
    ])

    // Жидкость аннигиляции
    event.custom({
    type: 'create:emptying',
    ingredients: [
        { item: 'legendary_monsters:bottle_of_annihilation' }
    ],
    results: [
        { id: 'minecraft:glass_bottle' },
        { id: 'kubejs:annihilation_fluid', amount: 250 }
    ]
    })

    event.recipes.createFilling('legendary_monsters:bottle_of_annihilation', [
        'minecraft:glass_bottle',
        Fluid.of('kubejs:annihilation_fluid', 250)
    ])

    // Облако
    event.custom({
    type: 'create:emptying',
    ingredients: [
        { item: 'quark:bottled_cloud' }
    ],
    results: [
        { id: 'minecraft:glass_bottle' },
        { id: 'kubejs:cloud_fluid', amount: 250 }
    ]
    })

    event.recipes.createFilling('quark:bottled_cloud', [
        'minecraft:glass_bottle',
        Fluid.of('kubejs:cloud_fluid', 250)
    ])

    // Черная сталь
    event.shaped(Item.of('kubejs:black_steel_rod', 4), [
        ' I ',
        ' I ',
        '   '
    ], {
        I: 'cataclysm:black_steel_ingot'
    })

    let coals = ['minecraft:coal', 'minecraft:charcoal']

    event.recipes.create.mixing('cataclysm:black_steel_ingot', [
        'minecraft:iron_ingot',
        coals,
        coals,
        
    ]).heated()

    // Аурумитовый слиток
    event.recipes.create.mixing(Item.of('kubejs:aurumite_ingot', 3), [
        'minecraft:gold_ingot',
        'create:brass_ingot',
        'cataclysm:ancient_metal_ingot'
    ]).superheated()

    event.shapeless(Item.of('kubejs:aurumite_scrap', 9), [
        'kubejs:aurumite_ingot'
    ])

    event.shaped('kubejs:aurumite_ingot', [
        'SSS',
        'SSS',
        'SSS'
    ], {
        S: 'kubejs:aurumite_scrap'
    })

    event.recipes.create.sandpaper_polishing('kubejs:chorium_jewel', 'createcasing:chorium_ingot')

    // Алтарь возрождения
    let fluidTank = ['create:fluid_tank', 'createcasing:andesite_fluid_tank', 'createcasing:brass_fluid_tank', 'createcasing:zinc_fluid_tank']
    event.recipes.create.mechanical_crafting('reviveraltar:ritual_altar', [
        ' AAA ',
        'ASBSA',
        'ACTCA',
        'ASRSA',
        ' AAA '
    ], {
        A: 'create_wizardry:arcane_sheet',
        S: 'irons_spellbooks:shriving_stone',
        B: 'beacon',
        C: 'create_wizardry:arcane_casing',
        R: 'respawn_anchor',
        T: fluidTank
    })

    // Хроматическое соединение
    event.recipes.create.mixing('create:chromatic_compound', [
        Item.of('glowstone_dust', 3),
        Item.of('create:powdered_obsidian', 3),
        'create:polished_rose_quartz'
    ]).superheated()

    // Изысканное сияние
    event.recipes.create.mixing('create:refined_radiance', [
        Item.of('cataclysm:void_jaw', 3),
        Item.of('glow_ink_sac', 3),
        'create:chromatic_compound'
    ]).superheated()

    // Теневая сталь
    event.recipes.create.mixing('create:shadow_steel', [
        Item.of('cataclysm:black_steel_nugget', 3),
        Item.of('born_in_chaos_v1:dark_metal_nugget', 3),
        'create:chromatic_compound'
    ]).superheated()

    event.recipes.create.pressing(
        'create:shadow_steel_casing',           // результат
        'create:shadow_steel'             // входной предмет
    );

    // Тёмно-хориевая сталь
    event.recipes.create.mixing(Item.of('kubejs:dark_chorium_steel', 2), [
        'createcasing:chorium_ingot',
        'cataclysm:black_steel_ingot',
        'create:shadow_steel',
        Fluid.of('create_dragons_plus:dragon_breath', 250)
    ]).superheated()

    // Осколок эха
    const sculkBlocks = [
        'minecraft:sculk',
        'minecraft:sculk_catalyst',
        'minecraft:sculk_shrieker',
        'minecraft:sculk_sensor'
    ];
        // Дробление скалк блоков
    sculkBlocks.forEach(block => {
        event.recipes.create.crushing([
            CreateItem.of('minecraft:echo_shard', 0.005),
            CreateItem.of('create:experience_nugget', 0.05)
        ], block);
    });
    
        // Продув огнем душ
    event.recipes.create.haunting(
        [
            CreateItem.of('minecraft:echo_shard', 0.01)
        ],
        'minecraft:amethyst_shard'
    );

    // Тлеющий уголек скверны
    event.recipes.create.mixing(Item.of('born_in_chaos_v1:smoldering_infernal_ember', 2), [
         Item.of('soul_fire_charge', 2),
         Item.of('alexscaves:uranium_shard', 2),
        'irons_spellbooks:cinder_essence'
    ]).superheated()

    // Мифридиновый слиток
    event.recipes.create.mixing('kubejs:mithridium_ingot', [
        'irons_spellbooks:mithril_ingot',
        'createpropulsion:platinum_ingot'
    ]).superheated()

    // Портальная жидкость
    event.recipes.createMixing(Fluid.of('aerowarptics:rift_essence', 1000), [
      ['aeronautics:levitite', 'aeronautics:pearlescent_levitite'],
      '4x minecraft:chorus_fruit'
    ]).superheated()

    // Варп-двигатель
    let trwarp = 'kubejs:incomplete_warp_drive'
    let tank = ['create:fluid_tank', 'createcasing:andesite_fluid_tank', 'createcasing:brass_fluid_tank', 'createcasing:zinc_fluid_tank']
    event.recipes.create.sequenced_assembly([
        'artifacts:warp_drive'
    ], tank, [ 
        event.recipes.createDeploying(trwarp, [trwarp, 'create:refined_radiance']),
        event.recipes.createFilling(trwarp, [trwarp, Fluid.of('irons_spellbooks:timeless_slurry', 1000)]),
        event.recipes.createDeploying(trwarp, [trwarp, 'aerowarptics:rift_lens']),
        event.recipes.createFilling(trwarp, [trwarp, Fluid.of('aerowarptics:rift_essence', 250)]),
        event.recipes.createDeploying(trwarp, [trwarp, 'sophisticatedcore:ender_linker']),
    ]).transitionalItem(trwarp).loops(1)

})