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
        { id: 'kubejs:chaos_fluid', amount: 75 }
    ]
    })

    event.recipes.createFilling('born_in_chaos_v1:chaos_component', [
        'minecraft:glass_bottle',
        Fluid.of('kubejs:chaos_fluid', 250)
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
})
