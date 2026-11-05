
ServerEvents.recipes(event => {
    let largeCogs = [
        'create:large_cogwheel',
        'createcasing:andesite_large_cogwheel',
        'createcasing:brass_large_cogwheel',
        'createcasing:copper_large_cogwheel',
        'createcasing:zinc_large_cogwheel',
        'northstar:iron_large_cogwheel',
        'dndecor:large_dark_metal_cogwheel',
        '#dndecor:large_industrial_cogwheel'
    ]
    let Cogs = [
        'create:cogwheel',
        'createcasing:andesite_cogwheel',
        'createcasing:brass_cogwheel',
        'createcasing:copper_cogwheel',
        'createcasing:zinc_cogwheel',
        'northstar:iron_cogwheel',
        'dndecor:dark_metal_cogwheel',
        '#dndecor:industrial_cogwheel'
    ]

    event.remove({ output: 'clockwork:clockwork_gear' })
    event.replaceInput({ mod: 'clockwork' }, 'clockwork:clockwork_gear', largeCogs)
    event.remove({ output: ['clockwork:clockwork_drill', 'clockwork:clockwork_flamethrower', 'clockwork:clockwork_potion_sprayer'] })
    event.remove({ output: ['clockwork:barrel_crossbow', 'clockwork:scope_crossbow'] })
    event.remove({ output: 'clockwork:crossbow_barrel' })
    event.remove({ output: 'clockwork:clockwork_arrow' })

    // Арбалетный барабан
    event.shaped('clockwork:crossbow_barrel', [
        'bpb',
        'pgp',
        'bpb'
    ], {
        b: 'create:brass_ingot',
        p: 'create:polished_rose_quartz',
        g: largeCogs
    })

    // Заводная стрела
    event.shaped(Item.of('clockwork:clockwork_arrow', 4), [
        ' b ',
        ' s ',
        ' gf'
    ], {
        b: 'create:brass_sheet',
        s: 'stick',
        g: Cogs,
        f: 'feather'
    })
    event.recipes.create.mechanical_crafting(Item.of('clockwork:clockwork_arrow', 8), [
        ' aa ',
        'agba',
        'abga',
        ' aa '
    ], {
        b: 'create:brass_sheet',
        a: 'arrow',
        g: Cogs
    })

    // Автоматический арбалет
    event.recipes.create.mechanical_crafting('clockwork:barrel_crossbow', [
        'bssB',
        'rpcs',
        'rsps',
        'srrb'
    ], {
        b: 'create:brass_ingot',
        s: 'minecraft:stick',
        B: 'clockwork:crossbow_barrel',
        r: 'minecraft:string',
        c: 'minecraft:crossbow',
        p: 'create:precision_mechanism'
    })

    // Арбалет с прицелом
    event.recipes.create.mechanical_crafting('clockwork:scope_crossbow', [
        'bssS',
        'Ppcs',
        'rsps',
        'srPb'
    ], {
        b: 'create:brass_ingot',
        s: 'minecraft:stick',
        S: 'minecraft:spyglass',
        r: 'minecraft:string',
        c: 'minecraft:crossbow',
        p: 'create:precision_mechanism',
        P: 'simulated:spring'
    })

})
