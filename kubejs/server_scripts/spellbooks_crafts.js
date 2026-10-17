ServerEvents.recipes(event => {
    let enchBook = 'enchanted_book'
    let ruinBook = 'irons_spellbooks:diamond_spell_book'
    // Сгнившая книга заклинаний
    event.recipes.create.sequenced_assembly([
      CreateItem.of('irons_spellbooks:rotten_spell_book', 0.30),
      CreateItem.of('rotten_flesh', 0.24),
      CreateItem.of('bone_meal', 0.23),
      CreateItem.of('dirt', 0.23)
    ], enchBook, [
        event.recipes.createDeploying(enchBook, [enchBook, 'rotten_flesh']),
        event.recipes.createDeploying(enchBook, [enchBook, 'dirt']),
        event.recipes.createDeploying(enchBook, [enchBook, 'bone_meal']),
        event.recipes.createFilling(enchBook,  [enchBook, Fluid.of('create_wizardry:mana', 31)]),
        event.recipes.createDeploying(enchBook, [enchBook, 'bone_meal'])
    ]).transitionalItem(enchBook).loops(1)

    // Хлипкий журнал
    event.shaped('irons_spellbooks:copper_spell_book', [
        'CPP',
        'SPP',
        'CPP'
    ], {
        C: 'copper_ingot',
        S: 'string',
        P: 'paper'
    })

    // let copperBook = 'enchanted_book'
    // event.recipes.create.sequenced_assembly([
        //     'irons_spellbooks:copper_spell_book'
        // ], 'enchanted_book', [
        //     event.recipes.createDeploying(copperBook, [copperBook, 'copper_ingot']),
        //     event.recipes.createDeploying(copperBook, [copperBook, 'string']),
        //     event.recipes.createDeploying(copperBook, [copperBook, 'paper']),
        //     event.recipes.createFilling(copperBook,  [copperBook, Fluid.of('create_wizardry:mana', 31)]),
        //     event.recipes.createDeploying(copperBook, [copperBook, 'paper'])
        // ]).transitionalItem(copperBook).loops(2)

    // Железный фолиант
    event.shaped('irons_spellbooks:iron_spell_book', [
        'CLL',
        'CPP',
        'CLL'
    ], {
        C: 'chain',
        L: 'leather',
        P: 'paper'
    })

    // let ironBook = 'enchanted_book'
    // event.recipes.create.sequenced_assembly([
        //     'irons_spellbooks:iron_spell_book'
        // ], 'enchanted_book', [
        //     event.recipes.createDeploying(ironBook, [ironBook, 'chain']),
        //     event.recipes.createDeploying(ironBook, [ironBook, 'leather']),
        //     event.recipes.createDeploying(ironBook, [ironBook, 'paper']),
        //     event.recipes.createFilling(ironBook,  [ironBook, Fluid.of('create_wizardry:mana', 62)]),
        //     event.recipes.createDeploying(ironBook, [ironBook, 'paper'])
        // ]).transitionalItem(ironBook).loops(2)

    // Книга заклинаний ученика
    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:gold_spell_book'
    ], enchBook, [
        event.recipes.createDeploying(enchBook, [enchBook, 'create:golden_sheet']),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:hogskin']),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:arcane_essence']),
        event.recipes.createFilling(enchBook,  [enchBook, Fluid.of('create_wizardry:mana', 37)]),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:arcane_essence'])
    ]).transitionalItem(enchBook).loops(2)

    // Зачарованная книга заклинаний
    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:diamond_spell_book'
    ], enchBook, [
        event.recipes.createDeploying(enchBook, [enchBook, 'diamond']),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:hogskin']),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(enchBook,  [enchBook, Fluid.of('create_wizardry:mana', 61)]),
        event.recipes.createDeploying(enchBook, [enchBook, 'irons_spellbooks:magic_cloth'])
    ]).transitionalItem(enchBook).loops(2)

    // Древний трактат
    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:netherite_spell_book'
    ], ruinBook, [
        event.recipes.createDeploying(ruinBook, [ruinBook, 'netherite_ingot']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:lightning_bottle']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(ruinBook,  [ruinBook, Fluid.of('irons_spellbooks:blood', 500)]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth'])
    ]).transitionalItem(ruinBook).loops(2)

    // Книга ледяного клейма
    let iceShard = 'irons_spellbooks:permafrost_shard'

    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:ice_spell_book'
    ], ruinBook, [
        event.recipes.createDeploying(ruinBook, [ruinBook, iceShard]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:mithril_ingot']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(ruinBook,  [ruinBook, Fluid.of('irons_spellbooks:ice_venom', 375)]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createPressing(ruinBook, ruinBook)
    ]).transitionalItem(ruinBook).loops(2)

    // Ледяной посох - Замороженная рукоять (левый низ) + Осколок Вечной Мерзлоты (центр) + Сосуд Ледяного Яда (правый верх)
    event.remove({ output: 'irons_spellbooks:ice_staff' })
    event.shaped('irons_spellbooks:ice_staff', [
        '  V',
        ' S ',
        'H  '
    ], {
        V: 'irons_spellbooks:ice_venom_vial',
        S: 'irons_spellbooks:permafrost_shard',
        H: 'irons_spellbooks:frosted_helve'
    })

    // Руководство по эксплуатации всполоха
    let fireShard = ['dndesires:burner', 'netherite_ingot', 'cataclysm:ignitium_ingot']

    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:blaze_spell_book'
    ], ruinBook, [
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:cinder_essence']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'create_enchantment_industry:blaze_enchanter']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(ruinBook,  [ruinBook, Fluid.of('create_wizardry:fire_ale', 250)]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createPressing(ruinBook, ruinBook)
    ]).transitionalItem(ruinBook).loops(2)

    // Штормовой Атлас
    let stormShard = [ 'createaddition:electrum_sheet', 'create_more_additions:electrum_jewel']

    event.recipes.create.sequenced_assembly([
        'spellclasses:storm_atlas'
    ], ruinBook, [
        event.recipes.createDeploying(ruinBook, [ruinBook, stormShard]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:mithril_ingot']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(ruinBook,  [ruinBook, Fluid.of('create_wizardry:lightning', 375)]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createPressing(ruinBook, ruinBook)
    ]).transitionalItem(enchBook).loops(2)

    // Рукопись друидов
    let druidShard = ['small_amethyst_bud', 'medium_amethyst_bud', 'large_amethyst_bud', 'amethyst_cluster', 'amethyst_shard']
    let bottles = ['honey_bottle', 'alexscaves:hot_chocolate_bottle', 'alexscaves:purple_soda_bottle']
    let green = ['spore_blossom', 'mangrove_propagule', 'torchflower', 'alexscaves:flytrap']

    event.shaped('irons_spellbooks:druidic_spell_book', [
    ' GM',
    'BRA',
    'MI '
  ], {
    G: green,
    M: 'irons_spellbooks:magic_cloth',
    B: bottles,
    R: 'irons_spellbooks:rotten_spell_book',
    A: druidShard,
    I: 'glow_ink_sac'
  })

  // Драконий кодекс
  let dragonBook = 'irons_spellbooks:ruined_book'

  event.recipes.create.sequenced_assembly([
        'irons_spellbooks:dragonskin_spell_book'
    ], dragonBook, [
        event.recipes.createDeploying(dragonBook, [dragonBook, 'cataclysm:enderite_block']),
        event.recipes.createDeploying(dragonBook, [dragonBook, 'irons_spellbooks:mithril_ingot']),
        event.recipes.createDeploying(dragonBook, [dragonBook, 'irons_spellbooks:dragonskin']),
        event.recipes.createFilling(dragonBook,  [dragonBook, Fluid.of('fluid:haunting_fluid', 435)]),
        event.recipes.createDeploying(dragonBook, [dragonBook, 'irons_spellbooks:dragonskin']),
        event.recipes.createPressing(dragonBook, dragonBook)
    ]).transitionalItem(dragonBook).loops(2)

    // Апокриф вампира

    event.shaped('irons_spellbooks:cursed_doll_spell_book', [
    'VBI',
    'BRB',
    'IBV'
  ], {
    V: 'born_in_chaos_v1:elixir_of_vampirism',
    B: 'irons_spellbooks:bloody_vellum',
    I: 'irons_spellbooks:arcane_ingot',
    R: ruinBook
  })

  // Деревенская библия

  event.recipes.create.sequenced_assembly([
        'irons_spellbooks:villager_spell_book'
    ], ruinBook, [
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:divine_pearl']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'emerald_block']),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createFilling(ruinBook,  [ruinBook, Fluid.of('create_wizardry:mana', 61)]),
        event.recipes.createDeploying(ruinBook, [ruinBook, 'irons_spellbooks:magic_cloth']),
        event.recipes.createPressing(ruinBook, ruinBook)
    ]).transitionalItem(ruinBook).loops(2)

})