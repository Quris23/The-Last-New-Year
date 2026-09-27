// kubejs/server_scripts/recipe_overrides.js
ServerEvents.recipes(event => {

    // Простой крафт на обычном верстаке

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

    // Камень Пустоты
    event.shaped('cataclysm:void_stone', [
        'ASA',
        'SOS',
        'ASA'
    ], {
        O: ['minecraft:crying_obsidian', 'minecraft:obsidian', 'cataclysm:polished_obsidian'],
        A: 'northstar:polished_amethyst',
        S: 'northstar:polished_lunar_sapphire'
    })

    // Фонарь Пустоты
    event.shaped('cataclysm:void_lantern_block', [
        ' V ',
        'VGV',
        ' V '
    ], {
        G: 'minecraft:glowstone',
        V: 'cataclysm:void_stone'
    })

    // Пылающий пепел
    event.shaped('cataclysm:burning_ashes', [
        ' D ',
        'DLD',
        ' D '
    ], {
        L: 'cataclysm:lava_power_cell',
        D: 'cataclysm:dying_ember'
    })

    // Последовательная сборка Create (Sequenced Assembly)

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
})