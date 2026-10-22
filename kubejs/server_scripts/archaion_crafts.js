ServerEvents.recipes(event => {

  // Броня Джаггернаута
  let rune = ['irons_spellbooks:ender_rune', 'hazentouvelib:cosmic_rune']
  let ingot = ['createcasing:chorium_ingot', 'create:shadow_steel']

  let JugTrans0 = 'kubejs:incomplete_unbreakable_skull'
  event.recipes.create.sequenced_assembly([
    'echoing_magic:reinforced_juggernaut_helmet',
  ], 'irons_spellbooks:netherite_mage_helmet', [
    event.recipes.createDeploying(JugTrans0, [JugTrans0, 'kubejs:dark_chorium_steel']),
    event.recipes.createFilling(JugTrans0, [JugTrans0, Fluid.of('create_wizardry:mana', 52)]),
    event.recipes.createDeploying(JugTrans0, [JugTrans0, rune]),
    event.recipes.createDeploying(JugTrans0, [JugTrans0, 'echoing_magic:echoed_manuscript']),
    event.recipes.createFilling(JugTrans0, [JugTrans0, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
    event.recipes.createDeploying(JugTrans0, [JugTrans0, 'cataclysm:enderite_block']),
  ]).transitionalItem(JugTrans0).loops(1)

  let JugTrans1 = 'kubejs:incomplete_unbreakable_skull'
  event.recipes.create.sequenced_assembly([
    'echoing_magic:reinforced_juggernaut_chestplate',
  ], 'irons_spellbooks:netherite_mage_chestplate', [
    event.recipes.createDeploying(JugTrans1, [JugTrans1, 'kubejs:dark_chorium_steel']),
    event.recipes.createFilling(JugTrans1, [JugTrans1, Fluid.of('create_wizardry:mana', 52)]),
    event.recipes.createDeploying(JugTrans1, [JugTrans1, rune]),
    event.recipes.createDeploying(JugTrans1, [JugTrans1, 'echoing_magic:echoed_manuscript']),
    event.recipes.createFilling(JugTrans1, [JugTrans1, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
    event.recipes.createDeploying(JugTrans1, [JugTrans1, 'cataclysm:enderite_block']),
  ]).transitionalItem(JugTrans1).loops(1)

  let JugTrans2 = 'kubejs:incomplete_unbreakable_skull'
  event.recipes.create.sequenced_assembly([
    'echoing_magic:reinforced_juggernaut_leggings',
  ], 'irons_spellbooks:netherite_mage_leggings', [
    event.recipes.createDeploying(JugTrans2, [JugTrans2, 'kubejs:dark_chorium_steel']),
    event.recipes.createFilling(JugTrans2, [JugTrans2, Fluid.of('create_wizardry:mana', 52)]),
    event.recipes.createDeploying(JugTrans2, [JugTrans2, rune]),
    event.recipes.createDeploying(JugTrans2, [JugTrans2, 'echoing_magic:echoed_manuscript']),
    event.recipes.createFilling(JugTrans2, [JugTrans2, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
    event.recipes.createDeploying(JugTrans2, [JugTrans2, 'cataclysm:enderite_block']),
  ]).transitionalItem(JugTrans2).loops(1)

  let JugTrans3 = 'kubejs:incomplete_unbreakable_skull'
  event.recipes.create.sequenced_assembly([
    'echoing_magic:reinforced_juggernaut_boots',
  ], 'irons_spellbooks:netherite_mage_boots', [
    event.recipes.createDeploying(JugTrans3, [JugTrans3, 'kubejs:dark_chorium_steel']),
    event.recipes.createFilling(JugTrans3, [JugTrans3, Fluid.of('create_wizardry:mana', 52)]),
    event.recipes.createDeploying(JugTrans3, [JugTrans3, rune]),
    event.recipes.createDeploying(JugTrans3, [JugTrans3, 'echoing_magic:echoed_manuscript']),
    event.recipes.createFilling(JugTrans3, [JugTrans3, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
    event.recipes.createDeploying(JugTrans3, [JugTrans3, 'cataclysm:enderite_block']),
  ]).transitionalItem(JugTrans3).loops(1)

})