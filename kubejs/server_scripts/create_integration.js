// kubejs/server_scripts/create_integration.js

// Облако в бутылке: ПКМ стеклянной бутылкой на высоте облаков (Y от 191 до 196, как у Quark) даёт артефакт,
// а не бутылочку Quark. Quark-модуль "Bottled Cloud" выключен в конфиге, бутылочка Quark забанена (itembans).
ItemEvents.rightClicked('minecraft:glass_bottle', event => {
    const player = event.player
    if (player.y > 191 && player.y < 196) {
        event.item.shrink(1)
        player.give('artifacts:cloud_in_a_bottle')
        event.cancel()
    }
})

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

    event.recipes.create.mixing(Fluid.of('kubejs:chaos_fluid', 750), [
        'born_in_chaos_v1:seedof_chaos'
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

    event.recipes.create.mixing(Fluid.of('kubejs:annihilation_fluid', 2000), [
        'legendary_monsters:enderitium_gem',
        'minecraft:ender_eye'
    ])

    // Облако
    event.custom({
    type: 'create:emptying',
    ingredients: [
        { item: 'artifacts:cloud_in_a_bottle' }
    ],
    results: [
        { id: 'minecraft:glass_bottle' },
        { id: 'kubejs:cloud_fluid', amount: 250 }
    ]
    })

    event.recipes.createFilling('artifacts:cloud_in_a_bottle', [
        'minecraft:glass_bottle',
        Fluid.of('kubejs:cloud_fluid', 250)
    ])

    // Черная сталь прокатном станке
    event.custom({
    type: 'createaddition:rolling',
    ingredients: [
        { item: 'cataclysm:black_steel_ingot' }
    ],
    results: [
        { id: 'kubejs:black_steel_rod', count: 2 }
    ]
    })

    // Черная сталь кусачками
    event.custom({
    type: 'createdieselgenerators:wire_cutting',
    ingredients: [
        { item: 'cataclysm:black_steel_ingot' }
    ],
    results: [
        { id: 'kubejs:black_steel_rod', count: 2 }
    ]
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

    // Портальная жидкость (Rift Essence) - Леветит + 4 Хоруса в Смешивателе при супернагреве
    event.recipes.createMixing(Fluid.of('aerowarptics:rift_essence', 1000), [
      ['aeronautics:levitite', 'aeronautics:pearlescent_levitite'],
      '4x minecraft:chorus_fruit'
    ]).superheated()

    // Дробильные колёса (Create): Базальт -> 1 Соль (Northstar), всегда (вместо 2 + шансы 75%/25%).
    event.remove({ id: 'northstar:crushing/basalt' });
    event.recipes.create.crushing('northstar:salt', 'minecraft:basalt').processingTime(1000).id('generation:crushing/basalt_salt');

    // Дробильные колёса (Create): Гравий -> Песок + 5% Глиняный шарик (убран шанс 10% на Кремень).
    event.remove({ id: 'create:crushing/gravel' });
    event.recipes.create.crushing([
      'minecraft:sand',
      CreateItem.of('minecraft:clay_ball', 0.05)
    ], 'minecraft:gravel').processingTime(250).id('generation:crushing/gravel_no_flint');

    // Гидравлический пресс (DnDesires) обрабатывает Зараженный камень так же, как обычный Пресс
    // (повторяет create_enchantment_industry:compacting/infested_stone: 4 зараженных камня -> каменные кирпичи + самородок опыта).
    event.custom({
      type: 'dndesires:hydraulic_compacting',
      ingredients: [
        { item: 'minecraft:infested_stone' },
        { item: 'minecraft:infested_stone' },
        { item: 'minecraft:infested_stone' },
        { item: 'minecraft:infested_stone' }
      ],
      results: [
        { id: 'minecraft:stone_bricks' },
        { id: 'create:experience_nugget' }
      ]
    }).id('generation:hydraulic_compacting/infested_stone');

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

    // Чешуя дракона
    let skinTrans1 = 'born_in_chaos_v1:monster_skin'
    //'legendary_monsters:lava_eaters_skin']
    event.recipes.create.sequenced_assembly([
        CreateItem.of('quark:dragon_scale', 0.50),
        CreateItem.of('born_in_chaos_v1:monster_skin', 0.50)
    ], skinTrans1, [
        event.recipes.createDeploying(skinTrans1, [skinTrans1, 'end_crystal']),
        event.recipes.createFilling(skinTrans1,  [skinTrans1, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
        event.recipes.createDeploying(skinTrans1, [skinTrans1, ['crying_obsidian', 'obsidian']]),
        event.recipes.createFilling(skinTrans1,  [skinTrans1, Fluid.of('create_dragons_plus:dragon_breath', 500)]),
        event.recipes.createPressing(skinTrans1, skinTrans1)
    ]).transitionalItem(skinTrans1).loops(3)

    // Кристалл Аркейна из одной маны: 100 мБ в чаше под прессом, на сверхнагреве
    event.custom({
        type: 'create:compacting',
        heat_requirement: 'superheated',
        ingredients: [
            { type: 'neoforge:single', amount: 100, fluid: 'create_wizardry:mana' }
        ],
        results: [
            { id: 'arcanerunes:arcane_crystal' }
        ]
    }).id('generation:compacting/arcane_crystal_superheated')

    // Хекс-кристалл (любой свиток) в чаше на сверхнагреве под миксером превращается в 60 мБ маны
    event.recipes.create.mixing(Fluid.of('create_wizardry:mana', 60), [
        'irons_spellbooks:scroll'
    ]).superheated()

    // Кристалл Аркейна обратно в 100 мБ маны: чаша на сверхнагреве под миксером
    event.recipes.create.mixing(Fluid.of('create_wizardry:mana', 100), [
        'arcanerunes:arcane_crystal'
    ]).superheated()

    // Рунический стол (Arcane Runes): механическая сборка на Тайном корпусе.
    // Любая руна школы - пресс - 50 мБ маны; затем кристаллический дисплей; готовый предмет эндерируется вентилятором
    event.remove({ id: 'arcanerunes:rune_table' })
    let anyRune = [
        'irons_spellbooks:fire_rune', 'irons_spellbooks:ice_rune', 'irons_spellbooks:lightning_rune',
        'wind_spellbooks:wind_rune', 'irons_spellbooks:nature_rune', 'irons_spellbooks:ender_rune',
        'irons_spellbooks:blood_rune', 'irons_spellbooks:holy_rune', 'irons_spellbooks:evocation_rune',
        'arcanerunes:eldritch_rune', 'arcanerunes:sand_rune', 'cataclysm_spellbooks:abyssal_rune',
        'hazentouvelib:cosmic_rune', 'discerning_the_eldritch:ritual_rune', 'somakespells:aqua_rune'
    ]
    let tableCasing = 'create_wizardry:arcane_casing'
    let tableSteps = []
    for (let n = 0; n < 1; n++) {
        tableSteps.push(
            event.recipes.createDeploying(tableCasing, [tableCasing, anyRune]),
            event.recipes.createPressing(tableCasing, tableCasing),
            event.recipes.createFilling(tableCasing, [tableCasing, Fluid.of('create_wizardry:mana', 50)])
        )
    }
    tableSteps.push(
        event.recipes.createDeploying(tableCasing, [tableCasing, 'supplementaries:crystal_display'])
    )
    event.recipes.create.sequenced_assembly([
        'kubejs:unendered_rune_table'
    ], tableCasing, tableSteps).transitionalItem(tableCasing).loops(3)

    // ...и последний шаг: эндерирование вентилятором над дыханием дракона (Create Dragons Plus)
    event.custom({
        type: 'create_dragons_plus:ending',
        ingredients: [{ item: 'kubejs:unendered_rune_table' }],
        results: [{ id: 'arcanerunes:rune_table', chance: 0.9 }]
    }).id('generation:ending/rune_table')

    // Оптика Kiroshi «Голографическое яблоко» (SpellClasses, имплант для лица): показывает размещённые схемы Create из всего инвентаря
    event.shaped('spellclasses:holographic_apple', [
        'PSP',
        'III'
    ], {
        P: 'create:placard',
        S: 'create:empty_schematic',
        I: 'create:iron_sheet'
    })

    // ---- Из версии друга (06.11.2026) ----
    // Фантомный порох
    event.recipes.create.mixing('born_in_chaos_v1:phantom_powder', [
        Item.of('gunpowder', 4),
        'phantom_membrane'
    ]).heated()

    // Светон: 8 светящихся ягод -> 1000 мВ в смешивателе (старые рецепты убраны в remove.js)
    event.recipes.create.mixing(Fluid.of('supplementaries:lumisene', 1000), [
        Item.of('minecraft:glow_berries', 8)
    ])

    // Жидкость контроля
    event.custom({
        type: 'create:mixing',
        heat_requirement: 'superheated',
        ingredients: [
            { type: 'neoforge:tag', tag: 'kubejs:control_fluid/chaos', amount: 500 },
            { type: 'neoforge:tag', tag: 'kubejs:control_fluid/annihilation', amount: 500 },
            [
                { item: 'born_in_chaos_v1:smoldering_infernal_ember' },
                { item: 'deeperdarker:soul_dust' },
                { item: 'witherstormmod:tainted_dust' }
            ],
            [
                { item: 'born_in_chaos_v1:smoldering_infernal_ember' },
                { item: 'deeperdarker:soul_dust' },
                { item: 'witherstormmod:tainted_dust' }
            ]
        ],
        results: [
            { id: 'kubejs:control_fluid', amount: 500 }
        ]
    }).id('generation:mixing/control_fluid')

    // Чёрный аргилит
    event.remove({ output: 'born_in_chaos_v1:black_argillite' })
    // бесформенный: Create сам берёт такие рецепты в смешиватель (allowShapelessInMixer)
    event.shapeless('born_in_chaos_v1:black_argillite', [
        'quartz',
        'cobbled_deepslate',
        'cobbled_deepslate',
        'blackstone'
    ])

    // Многомерный куб
    let trdice = 'kubejs:incomplete_multidimensional_dice'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('witherstormmod:multidimensional_dice', 0.50),
        CreateItem.of('create:refined_radiance', 0.50)
    ], 'create:refined_radiance', [
        event.recipes.createDeploying(trdice, [trdice, 'hazentouvelib:radiance_upgrade_orb']),
        event.recipes.createFilling(trdice, [trdice, Fluid.of('kubejs:control_fluid', 1000)]),
        event.recipes.createDeploying(trdice, [trdice, 'createnuclear:lead_ingot']),
        event.recipes.createFilling(trdice, [trdice, Fluid.of('supplementaries:lumisene', 1000)]),
        event.recipes.createDeploying(trdice, [trdice, 'irons_spellbooks:mithril_ingot'])
    ]).transitionalItem(trdice).loops(1)

    // Заражённый чёрный аргиллит
    event.recipes.create.haunting('kubejs:infested_black_argillite', 'born_in_chaos_v1:black_argillite')

    // Тёмный металл
    event.recipes.create.crushing([
        CreateItem.of('born_in_chaos_v1:black_argillite', 0.33),
        CreateItem.of('born_in_chaos_v1:dark_metal_nugget', 0.01),
        CreateItem.of('born_in_chaos_v1:pileof_dark_metal', 0.005)
    ], 'kubejs:infested_black_argillite').processingTime(350)

    // Эндеритиум
    event.recipes.create.mixing('legendary_monsters:enderitium_gem', [
        'popped_chorus_fruit',
        'born_in_chaos_v1:dark_metal_nugget',
        Fluid.of('aeronautics:levitite_blend', 20)
    ]).superheated()

    // Слиток эндеритиума
    event.recipes.create.mixing('legendary_monsters:enderitium_ingot', [
        Item.of('popped_chorus_fruit', 9),
        'born_in_chaos_v1:dark_metal_ingot',
        Fluid.of('aeronautics:levitite_blend', 180)
    ]).superheated()

    // Улучшения-помпы рюкзаков: опыт, кровь, мана
    let pump = ['create:mechanical_pump', 'createcasing:andesite_mechanical_pump', 'createcasing:brass_mechanical_pump', 'createcasing:zinc_mechanical_pump']
    event.remove({ output: 'sophisticatedbackpacks:xp_pump_upgrade' })
    event.shaped('sophisticatedbackpacks:xp_pump_upgrade', [
        'rpr',
        'bub',
        'rpr'
    ], {
        r: 'redstone',
        p: pump,
        b: 'experience_bottle',
        u: 'sophisticatedbackpacks:advanced_pump_upgrade'
    })
    event.shaped('spellclasses:blood_pump_upgrade', [
        'rpr',
        'bub',
        'rpr'
    ], {
        r: 'redstone',
        p: pump,
        b: 'chromeware:empty_syringe',
        u: 'sophisticatedbackpacks:advanced_pump_upgrade'
    })
    event.shaped('spellclasses:mana_pump_upgrade', [
        'rpr',
        'bub',
        'rsr'
    ], {
        r: 'redstone',
        p: 'create_wizardry:smart_arcane_pipe',
        b: 'irons_spellbooks:arcane_essence',
        u: 'sophisticatedbackpacks:advanced_pump_upgrade',
        s: 'create_wizardry:mana_siphon'
    })

    // Эндерированная кожа Дракона
    event.custom({
        type: 'create_dragons_plus:ending',
        ingredients: [{ item: 'irons_spellbooks:dragonskin' }],
        results: [{ id: 'kubejs:ending_dragonskin' }]
    })
    // Эндерированная драконья чешуя
    event.custom({
        type: 'create_dragons_plus:ending',
        ingredients: [{ item: 'quark:dragon_scale' }],
        results: [{ id: 'kubejs:ending_dragon_scale' }]
    })

    // Эндерированная бронепластина тёмно-хориевой стали
    event.shaped('kubejs:ending_armor_plate_from_dark_chorium_steel', [
        'Ssi',
        'iii',
        'isS'
    ], {
        S: 'kubejs:ending_dragonskin',
        s: 'kubejs:ending_dragon_scale',
        i: 'kubejs:dark_chorium_steel'
    })
    event.shaped('kubejs:ending_armor_plate_from_dark_chorium_steel', [
        'Ssi',
        'iii',
        'isS'
    ], {
        s: 'kubejs:ending_dragonskin',
        S: 'kubejs:ending_dragon_scale',
        i: 'kubejs:dark_chorium_steel'
    })
})

// Жидкости для жидкости контроля: любая из пары подходит
ServerEvents.tags('fluid', event => {
    // Хаос / кровь / драконье дыхание / портальная (пространственная) жидкость
    event.add('kubejs:control_fluid/chaos', ['kubejs:chaos_fluid', 'irons_spellbooks:blood', 'create_wizardry:blood', 'create_dragons_plus:dragon_breath', 'aerowarptics:rift_essence'])
    // Аннигиляция / опыт / уран / мана
    event.add('kubejs:control_fluid/annihilation', ['kubejs:annihilation_fluid', 'create_enchantment_industry:experience', 'createnuclear:uranium', 'create_wizardry:mana'])

})
