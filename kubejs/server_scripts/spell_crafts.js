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


})