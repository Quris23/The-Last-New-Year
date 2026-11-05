// Все правки мода Somake's Spells, которые делаются в KubeJS (рецепты и т.п.). Новые правки к Somake писать сюда.
// Атрибуты оружия Somake и высеченные заклинания делаются в Java (SpellClasses): Somake строит их лениво, KubeJS их не видит.
ServerEvents.recipes(event => {
    // Глеф из Пирия (бывшая Глефа из Визерита): механическая сборка
    event.remove({ output: 'somakespells:witherite_glaive' })
    event.recipes.create.mechanical_crafting('somakespells:witherite_glaive', [
        ' P',
        'CP',
        'SI',
        'R '
    ], {
        P: 'minecraft:redstone_block',
        C: 'somakespells:fire',
        S: 'irons_spellbooks:pyrium_staff',
        I: 'cataclysm:ignitium_ingot',
        R: 'block_factorys_bosses:dragon_bone'
    })
    // Пронзатель приливов: эссенция шторма, две руны гидро и коралловый бердыш (взамен рецепта мода, который требовал другой мод)
    event.remove({ output: 'somakespells:tide_piercer' })
    event.shaped('somakespells:tide_piercer', [
        ' E ',
        'RBR'
    ], {
        E: 'cataclysm:essence_of_the_storm',
        R: 'somakespells:aqua_rune',
        B: 'cataclysm:coral_bardiche'
    })

    // Медная перчатка
    event.remove({ output: 'somakespells:copper_glove' })
    event.recipes.create.mechanical_crafting('somakespells:copper_glove', [
        'beb',
        'BcB',
        'rMr',
        'sPs',
        'pHS'
    ], {
        b: 'pipeorgans:reinforced_copper_boot',
        e: 'create:steam_engine',
        B: 'copper_block',
        c: 'irons_spellbooks:energized_core',
        r: 'createaddition:copper_rod',
        M: 'kubejs:mithridium_ingot',
        s: 'createaddition:copper_spool',
        P: 'dndesires:hydraulic_press',
        p: 'create:fluid_pipe',
        H: 'create:brass_hand',
        S: 'create:smart_fluid_pipe'
    })

    // Ядро игнития
    let transfire = 'cataclysm:ignitium_ingot'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('somakespells:ignitium_core', 0.20),
        CreateItem.of('cataclysm:ignitium_ingot', 0.80)
    ], 'cataclysm:ignitium_ingot', [
        event.recipes.createDeploying(transfire, [transfire, 'irons_spellbooks:fire_rune']),
        event.recipes.createFilling(transfire, [transfire, Fluid.of('lava', 500)]),
        event.recipes.createPressing(transfire, transfire)
    ]).transitionalItem(transfire).loops(5)

    // Огонь
    event.remove({ output: 'somakespells:fire' })
    event.recipes.create.mechanical_crafting('somakespells:fire', [
        ' fff ',
        'fcecf',
        'feIef',
        'fcecf',
        ' fff '
    ], {
        f: 'fire_charge',
        c: 'cataclysm:lava_power_cell',
        e: 'irons_spellbooks:cinder_essence',
        I: 'somakespells:ignitium_core'
    })

    // Топор, рожденный скалком
    event.remove({ output: 'somakespells:sculkborne_axe' })
    event.custom({
        type: 'cataclysm:weapon_fusion',
        base: { item: 'born_in_chaos_v1:great_reaper_axe' },
        addition: { item: 'deeperdarker:warden_axe' },
        result: { id: 'somakespells:sculkborne_axe' }
    })

    // Запретный молот
    event.remove({ output: 'somakespells:forbidden_hammer' })
    event.custom({
        type: 'cataclysm:weapon_fusion',
        base: { item: 'born_in_chaos_v1:skullbreaker_hammer' },
        addition: { item: 'kubejs:dark_chorium_steel' },
        result: { id: 'somakespells:forbidden_hammer' }
    })
})
