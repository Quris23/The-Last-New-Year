ServerEvents.recipes(event => {

  // Рамка портала
    let bars = ['iron_bars', 'born_in_chaos_v1:dark_grid']
    let portTrans = 'kubejs:incomplete_uportal_frame'
    event.recipes.create.sequenced_assembly([
        'irons_spellbooks:portal_frame'
    ], bars, [
        event.recipes.createCutting(portTrans, portTrans),
        event.recipes.createDeploying(portTrans, [portTrans, 'irons_spellbooks:arcane_ingot']),
        event.recipes.createFilling(portTrans,  [portTrans, Fluid.of('create_wizardry:mana', 125)]),
        event.recipes.createDeploying(portTrans, [portTrans, 'irons_spellbooks:mithril_ingot']),
        event.recipes.createDeploying(portTrans, [portTrans, 'irons_spellbooks:arcane_ingot']),
        event.recipes.createDeploying(portTrans, [portTrans, 'ender_pearl'])
    ])
    .transitionalItem(portTrans).loops(1)


  // Волшебный слиток

  let arcaneIngot = 'irons_spellbooks:arcane_ingot'
  let Ingots = ['iron_ingot', 'gold_ingot', 'copper_ingot', 'cataclysm:black_steel_ingot']
  event.shapeless(`9x ${'irons_spellbooks:arcane_ingot'}`, 'create_wizardry:arcane_block')

  event.recipes.createFilling('irons_spellbooks:arcane_ingot', [
        Ingots,
        Fluid.of('create_wizardry:mana', 1000)
    ])

  // Кожа хоглина
  event.shaped('irons_spellbooks:hogskin', [
    'AA',
    'AA'
], {
    A: 'cold_sweat:hoglin_hide'
})

    // Кольцо Лапласа

    let ringTrans0 = 'irons_spellbooks:cooldown_ring'
    event.recipes.create.sequenced_assembly([
        'spellclasses:laplace_ring'
    ], ringTrans0, [
        event.recipes.createDeploying(ringTrans0, [ringTrans0, 'irons_spellbooks:mana_ring']),
        event.recipes.createFilling(ringTrans0,  [ringTrans0, Fluid.of('create_wizardry:mana', 27)]),
        event.recipes.createDeploying(ringTrans0, [ringTrans0, 'cataclysm:black_steel_ingot']),
        event.recipes.createDeploying(ringTrans0, [ringTrans0, 'irons_spellbooks:mana_upgrade_orb']),
        event.recipes.createDeploying(ringTrans0, [ringTrans0, 'emerald'])
    ])
    .transitionalItem(ringTrans0).loops(1)

    let ringTrans1 = 'irons_spellbooks:mana_ring'
    event.recipes.create.sequenced_assembly([
        'spellclasses:laplace_ring'
    ], ringTrans1, [
        event.recipes.createDeploying(ringTrans1, [ringTrans1, 'irons_spellbooks:cooldown_ring']),
        event.recipes.createFilling(ringTrans1,  [ringTrans1, Fluid.of('create_wizardry:mana', 27)]),
        event.recipes.createDeploying(ringTrans1, [ringTrans1, 'cataclysm:black_steel_ingot']),
        event.recipes.createDeploying(ringTrans1, [ringTrans1, 'irons_spellbooks:mana_upgrade_orb']),
        event.recipes.createDeploying(ringTrans1, [ringTrans1, 'emerald'])
    ])
    .transitionalItem(ringTrans1).loops(1)

    // Броня Инженера - крафт на кузнечном столе: база Незеритовая Мантия Мага + Механизм Точности (шаблон) + Руна Молнии (добавка)
    event.remove({ output: 'cataclysm_spellbooks:engineer_hood' })
    event.custom({
        type: 'minecraft:smithing_transform',
        base: { item: 'irons_spellbooks:netherite_mage_helmet' },
        template: { item: 'create:precision_mechanism' },
        addition: { item: 'irons_spellbooks:lightning_rune' },
        result: { id: 'cataclysm_spellbooks:engineer_hood' }
    })
    event.remove({ output: 'cataclysm_spellbooks:engineer_suit' })
    event.custom({
        type: 'minecraft:smithing_transform',
        base: { item: 'irons_spellbooks:netherite_mage_chestplate' },
        template: { item: 'create:precision_mechanism' },
        addition: { item: 'irons_spellbooks:lightning_rune' },
        result: { id: 'cataclysm_spellbooks:engineer_suit' }
    })
    event.remove({ output: 'cataclysm_spellbooks:engineer_leggings' })
    event.custom({
        type: 'minecraft:smithing_transform',
        base: { item: 'irons_spellbooks:netherite_mage_leggings' },
        template: { item: 'create:precision_mechanism' },
        addition: { item: 'irons_spellbooks:lightning_rune' },
        result: { id: 'cataclysm_spellbooks:engineer_leggings' }
    })
    event.remove({ output: 'cataclysm_spellbooks:engineer_boots' })
    event.custom({
        type: 'minecraft:smithing_transform',
        base: { item: 'irons_spellbooks:netherite_mage_boots' },
        template: { item: 'create:precision_mechanism' },
        addition: { item: 'irons_spellbooks:lightning_rune' },
        result: { id: 'cataclysm_spellbooks:engineer_boots' }
    })

    // Трость Изобретателя - крафт на кузнечном столе: база Посох Молнии + Механизм Точности (шаблон) + Руна Молнии (добавка)
    event.remove({ output: 'irons_spellbooks:artificer_cane' })
    event.custom({
        type: 'minecraft:smithing_transform',
        base: { item: 'irons_spellbooks:lightning_rod' },
        template: { item: 'create:precision_mechanism' },
        addition: { item: 'irons_spellbooks:lightning_rune' },
        result: { id: 'irons_spellbooks:artificer_cane' }
    })

})
