// kubejs/server_scripts/recipe_overrides.js
ServerEvents.recipes(event => {

    // Кристалл Энда
    event.shaped('minecraft:end_crystal', [
        'GGG',
        'GTG',
        'GSG'
    ], {
        G: 'minecraft:glass',
        T: 'ghast_tear',
        S: 'create_more_additions:silver_jewel'
    })

    event.shaped('minecraft:end_crystal', [
        'GGG',
        'GTG',
        'GSG'
    ], {
        G: 'minecraft:glass',
        T: 'ghast_tear',
        S: 'obsidian'
    })

    // Зачарованное золотое яблоко
    let appleTransitional = 'kubejs:incomplete_enchanted_apple'
    event.recipes.create.sequenced_assembly([
        'minecraft:enchanted_golden_apple'
    ], 'minecraft:golden_apple', [
        event.recipes.createDeploying(appleTransitional, [appleTransitional, 'minecraft:gold_block']),
        event.recipes.createPressing(appleTransitional, appleTransitional),
        event.recipes.createFilling(appleTransitional, [appleTransitional, Fluid.of('create_enchantment_industry:experience', 125)]),
        event.recipes.createDeploying(appleTransitional, [appleTransitional, 'minecraft:lapis_lazuli']),
        event.recipes.createDeploying(appleTransitional, [appleTransitional, 'create_enchantment_industry:super_experience_nugget'])
    ]).transitionalItem(appleTransitional).loops(2)

    // Тотем хоруса
    let totemTransitional = 'kubejs:incomplete_chorus_totem'
    event.recipes.create.sequenced_assembly([
        'artifacts:chorus_totem'
    ], 'kubejs:chorium_jewel', [
        event.recipes.createCutting(totemTransitional, totemTransitional),
        event.recipes.createDeploying(totemTransitional, [totemTransitional, 'minecraft:end_crystal']),
        event.recipes.createFilling(totemTransitional, [totemTransitional, Fluid.of('minecraft:lava', 500)]),
        event.recipes.createDeploying(totemTransitional, [totemTransitional, 'minecraft:emerald']),
        event.recipes.createPressing(totemTransitional, totemTransitional),
        event.recipes.createFilling(totemTransitional, [
            totemTransitional,
            Fluid.of('create_enchantment_industry:experience', 27)
        ])
    ]).transitionalItem(totemTransitional).loops(1)

    // Тотем смерти
    let deathTotemTransitional = 'kubejs:incomplete_death_totem'
    event.recipes.create.sequenced_assembly([
        'born_in_chaos_v1:death_totem'
    ], 'minecraft:bone', [
        event.recipes.createCutting(deathTotemTransitional, deathTotemTransitional),
        event.recipes.createDeploying(deathTotemTransitional, [deathTotemTransitional, 'minecraft:end_crystal']),
        event.recipes.createFilling(deathTotemTransitional, [deathTotemTransitional, Fluid.of('minecraft:lava', 500)]),
        event.recipes.createDeploying(deathTotemTransitional, [deathTotemTransitional, 'born_in_chaos_v1:spiritual_dust']),
        event.recipes.createPressing(deathTotemTransitional, deathTotemTransitional),
        event.recipes.createFilling(deathTotemTransitional, [deathTotemTransitional, Fluid.of('kubejs:chaos_fluid', 75)])
    ]).transitionalItem(deathTotemTransitional).loops(1)

    // Тёмная решётка
    event.shaped(Item.of('born_in_chaos_v1:dark_grid', 8), [
        ' I ',
        ' I ',
        '   '
    ], {
        I: 'born_in_chaos_v1:dark_metal_ingot'
    })

    // Шлем из тёмного металла
    event.recipes.minecraft.smithing_transform(
    'born_in_chaos_v1:dark_metal_armor_helmet',
    'born_in_chaos_v1:dark_upgrade',
    'cataclysm:monstrous_helm',
    'born_in_chaos_v1:armor_plate_from_dark_metal'
  )
    // Tentaworks
        // Щупальце

        // Медная установка
    event.recipes.create.mechanical_crafting('tentaworks:copper_tentacle_rig', [
        't   t',
        'rspsr',
        ' cbc ',
        'rsmsr',
        't   t'
    ], {
        t: 'tentaworks:tentacle',
        r: 'createaddition:brass_rod',
        s: 'createcasing:copper_shaft',
        p: 'create:precision_mechanism',
        c: 'create:copper_casing',
        b: 'create:copper_backtank',
        m: 'simulated:gyroscopic_mechanism'
    })

        // Незеритовая установка
    let casing = ['dndesires:industrial_casing']
    event.recipes.create.mechanical_crafting('tentaworks:netherite_tentacle_rig', [
        't   t',
        'rspsr',
        ' cbc ',
        'rsmsr',
        't   t'
    ], {
        t: 'tentaworks:tentacle',
        r: 'kubejs:black_steel_rod',
        s: 'create:shaft',
        p: 'create:precision_mechanism',
        c: casing,
        b: 'create:netherite_backtank',
        m: 'simulated:gyroscopic_mechanism'
    })

        // Щупальце
    let tentacleTrans = 'create:piston_extension_pole'
    event.recipes.create.sequenced_assembly([
        'tentaworks:tentacle'
    ], tentacleTrans, [
        event.recipes.createDeploying(tentacleTrans, [tentacleTrans, 'create:piston_extension_pole']),
        event.recipes.createPressing(tentacleTrans, tentacleTrans),
        event.recipes.createDeploying(tentacleTrans, [tentacleTrans, 'create:piston_extension_pole']),
        event.recipes.createPressing(tentacleTrans, tentacleTrans),
        event.recipes.createDeploying(tentacleTrans, [tentacleTrans, 'create:sticker']),
    ]).transitionalItem(tentacleTrans).loops(3)

    // Макска химзащиты
    event.remove({ output: 'alexscaves:hazmat_mask' })
    event.recipes.create.mechanical_crafting('alexscaves:hazmat_mask', [
        'ppppp',
        'ppppp',
        'pp pp',
        'ppppp',
        'ppppp'
    ], {
        p: 'alexscaves:polymer_plate'
    })

    // Иммунодепрессанты
    event.remove({ id: 'chromeware:mixing/standard_immunosuppressant' })
    event.remove({ id: 'chromeware:mixing/military_immunosuppressant' })
    event.remove({ id: 'chromeware:mixing/military_immunosuppressant_from_awkward' })
    // Стандартный
    event.custom({
        type: 'create:mixing',
        ingredients: [
            { item: 'minecraft:golden_carrot' },
            {
                type: 'neoforge:components',
                amount: 1000,
                components: {
                    'create:potion_fluid_bottle_type': 'regular',
                    'minecraft:potion_contents': { potion: 'minecraft:awkward' }
                },
                fluids: 'create:potion'
            }
        ],
        results: [{ id: 'chromeware:standard_immunosuppressant', amount: 1000 }]
    }).id('kubejs:mixing/standard_immunosuppressant')
    // Военный
    event.custom({
        type: 'create:mixing',
        ingredients: [
            { item: 'minecraft:golden_apple' },
            { type: 'neoforge:single', fluid: 'chromeware:standard_immunosuppressant', amount: 1000 }
        ],
        results: [{ id: 'chromeware:military_immunosuppressant', amount: 1000 }]
    }).id('kubejs:mixing/military_immunosuppressant')

    // TaCZ: оружейные станки стандартного пака без рецептов (шаг Extra руководства Create Armorer).
    // Оружие делается через Create (Create Immersive TaCZ), сами станки крафтятся как раньше
    event.remove({ type: 'tacz:gun_smith_table_crafting', id: /^tacz:/ })

    // Create Armorer: в сборке только «Моментъ», «Кранъ», «Катокъ», «Камень», «Подшипникъ», «Рукоятка», «Саламандра», «Маховикъ».
    // Остальные пушки, патроны и обвесы, которые не подходят ни к одной из них, без рецептов
    // (fluid_pipe_* и special_melee_atomic делают предметы, которых нет в этой версии пака; unmelee_wrench превращал любую пушку в ключ Create).
    // Скрыты в JEI: witherstormmod client/jei/TaczHiddenItems и client_scripts/hide_items.js
    ;[
        'guns/grenade_launcher', 'guns/pistol', 'guns/semi_rifle', 'guns/sniper',
        'guns/assault_rifle', // «Кранъ» только имплантом (lny_octo_tilt:crane_implant)
        'guns/double_barrel_shotgun', // «Камень» только имплантом (lny_octo_tilt:stone_implant)
        'guns/melee_wrench', 'guns/unmelee_wrench', 'guns/special_melee_atomic',
        'ammo/grenade_casing', 'ammo/grenade_casing_fill',
        'attachments/ap_projectile', 'attachments/wrench_iron_spike', 'attachments/muzzle_refit_energy_blade',
        'attachments/fluid_pipe_grip', 'attachments/fluid_pipe_muzzle', 'attachments/fluid_pipe_scope'
    ].forEach(id => event.remove({ id: `createimmersivetacz:${id}` }))

})