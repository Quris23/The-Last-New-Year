ServerEvents.recipes(event => {
  
  // Пириевый посох
  event.shaped('irons_spellbooks:pyrium_staff', [
    ' P ',
    ' AP',
    'N  '
  ], {
    A: 'irons_spellbooks:fire_ale',
    N: '#c:ingots/netherite',
    P: '#c:ingots/pyrium'
  })

  // Void Staff
  event.custom({
    type: 'cataclysm:weapon_fusion',
    base: { item: 'irons_spellbooks:hither_thither_wand' },
    addition: { item: 'cataclysm:void_core' },
    result: { id: 'cataclysm_spellbooks:void_staff' }
  })

  // Броня бездны
  let coral = ['tube_coral_block', 'brain_coral_block', 'bubble_coral_block', 'fire_coral_block', 'horn_coral_block']
  event.shaped('cataclysm_spellbooks:abyssal_rune', [
    'CCC',
    'CRC',
    'CCC'
  ], {
    C: coral,
    R: 'irons_spellbooks:blank_rune'
  })
  event.shaped('cataclysm_spellbooks:abyssal_warlock_helmet', [
    'NR',
    'E '
  ], {
    N: 'irons_spellbooks:netherite_mage_helmet',
    R: 'cataclysm_spellbooks:abyssal_rune',
    E: 'irons_spellbooks:arcane_essence'
  })
  event.shaped('cataclysm_spellbooks:abyssal_warlock_chestplate', [
    'NR',
    'E '
  ], {
    N: 'irons_spellbooks:netherite_mage_chestplate',
    R: 'cataclysm_spellbooks:abyssal_rune',
    E: 'irons_spellbooks:arcane_essence'
  })
  event.shaped('cataclysm_spellbooks:abyssal_warlock_leggings', [
    'NR',
    'E '
  ], {
    N: 'irons_spellbooks:netherite_mage_leggings',
    R: 'cataclysm_spellbooks:abyssal_rune',
    E: 'irons_spellbooks:arcane_essence'
  })
  event.shaped('cataclysm_spellbooks:abyssal_warlock_boots', [
    'NR',
    'E '
  ], {
    N: 'irons_spellbooks:netherite_mage_boots',
    R: 'cataclysm_spellbooks:abyssal_rune',
    E: 'irons_spellbooks:arcane_essence'
  })

  // Броня цветущего камня
  event.custom({
    type: 'minecraft:smithing_transform',
    addition: { item: 'minecraft:amethyst_shard' },
    base: { item: 'irons_spellbooks:netherite_mage_helmet' },
    result: { id: 'cataclysm_spellbooks:bloom_stone_hat' },
    template: { item: 'cataclysm:amethyst_crab_shell' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    addition: { item: 'minecraft:amethyst_shard' },
    base: { item: 'irons_spellbooks:netherite_mage_chestplate' },
    result: { id: 'cataclysm_spellbooks:bloom_stone_chestplate' },
    template: { item: 'cataclysm:amethyst_crab_shell' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    addition: { item: 'minecraft:amethyst_shard' },
    base: { item: 'irons_spellbooks:netherite_mage_leggings' },
    result: { id: 'cataclysm_spellbooks:bloom_stone_skirt' },
    template: { item: 'cataclysm:amethyst_crab_shell' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    addition: { item: 'minecraft:amethyst_shard' },
    base: { item: 'irons_spellbooks:netherite_mage_boots' },
    result: { id: 'cataclysm_spellbooks:bloom_stone_greaves' },
    template: { item: 'cataclysm:amethyst_crab_shell' }
  })

  // Броня + трость Инженера
  let trans3 = 'kubejs:incomplete_wither_eye'
  let cogwheel = ['createcasing:brass_cogwheel', 'createcasing:brass_large_cogwheel']
  let neodymium = ['alexscaves:azure_neodymium_ingot', 'alexscaves:scarlet_neodymium_ingot']
  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm_spellbooks:engineer_hood', 0.90),
    CreateItem.of('irons_spellbooks:netherite_mage_helmet', 0.10)
    ], 'irons_spellbooks:netherite_mage_helmet', [ 
      event.recipes.createDeploying(trans3, [trans3, cogwheel]),
      event.recipes.createDeploying(trans3, [trans3, 'cataclysm:essence_of_the_storm']),
      event.recipes.createDeploying(trans3, [trans3, 'kubejs:mithridium_ingot']),
      event.recipes.createFilling(trans3,  [trans3, Fluid.of('create_wizardry:lightning', 500)]),
      event.recipes.createDeploying(trans3, [trans3, 'railways:remote_lens']),
      event.recipes.createDeploying(trans3, [trans3, 'irons_spellbooks:lightning_upgrade_orb'])
    ]).transitionalItem(trans3).loops(1)

  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm_spellbooks:engineer_suit', 0.90),
    CreateItem.of('irons_spellbooks:netherite_mage_chestplate', 0.10)
    ], 'irons_spellbooks:netherite_mage_chestplate', [ 
      event.recipes.createDeploying(trans3, [trans3, cogwheel]),
      event.recipes.createDeploying(trans3, [trans3, 'cataclysm:essence_of_the_storm']),
      event.recipes.createDeploying(trans3, [trans3, 'kubejs:mithridium_ingot']),
      event.recipes.createFilling(trans3,  [trans3, Fluid.of('create_wizardry:lightning', 500)]),
      event.recipes.createDeploying(trans3, [trans3, 'railways:remote_lens']),
      event.recipes.createDeploying(trans3, [trans3, 'irons_spellbooks:lightning_upgrade_orb'])
    ]).transitionalItem(trans3).loops(1)

  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm_spellbooks:engineer_leggings', 0.90),
    CreateItem.of('irons_spellbooks:netherite_mage_leggings', 0.10)
    ], 'irons_spellbooks:netherite_mage_leggings', [ 
      event.recipes.createDeploying(trans3, [trans3, cogwheel]),
      event.recipes.createDeploying(trans3, [trans3, 'cataclysm:essence_of_the_storm']),
      event.recipes.createDeploying(trans3, [trans3, 'kubejs:mithridium_ingot']),
      event.recipes.createFilling(trans3,  [trans3, Fluid.of('create_wizardry:lightning', 500)]),
      event.recipes.createDeploying(trans3, [trans3, 'railways:remote_lens']),
      event.recipes.createDeploying(trans3, [trans3, 'irons_spellbooks:lightning_upgrade_orb'])
    ]).transitionalItem(trans3).loops(1)

  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm_spellbooks:engineer_boots', 0.90),
    CreateItem.of('irons_spellbooks:netherite_mage_boots', 0.10)
    ], 'irons_spellbooks:netherite_mage_boots', [ 
      event.recipes.createDeploying(trans3, [trans3, cogwheel]),
      event.recipes.createDeploying(trans3, [trans3, 'cataclysm:essence_of_the_storm']),
      event.recipes.createDeploying(trans3, [trans3, 'kubejs:mithridium_ingot']),
      event.recipes.createFilling(trans3,  [trans3, Fluid.of('create_wizardry:lightning', 500)]),
      event.recipes.createDeploying(trans3, [trans3, 'railways:remote_lens']),
      event.recipes.createDeploying(trans3, [trans3, 'irons_spellbooks:lightning_upgrade_orb'])
    ]).transitionalItem(trans3).loops(1)

  event.custom({
    type: 'minecraft:smithing_transform',
    base: { item: 'irons_spellbooks:lightning_rod' },
    template: { item: 'create:precision_mechanism' },
    addition: { item: 'irons_spellbooks:lightning_rune' },
    result: { id: 'irons_spellbooks:artificer_cane' }
  })

  // Броня фараона
  let cloth = 'irons_spellbooks:magic_cloth'
  let rune = 'irons_spellbooks:nature_rune'

  event.shaped('cataclysm_spellbooks:pharaoh_helmet', [
    'csc',
    'crc'
  ], {
    c: cloth,
    r: rune,
    s: 'cataclysm:bone_reptile_helmet'
  })

  event.shaped('cataclysm_spellbooks:pharaoh_chestplate', [
    'ara',
    'bxb',
    'ccc'
  ], {
    c: cloth,
    r: rune,
    a: 'cataclysm:ancient_metal_ingot',
    b: 'cataclysm:koboleton_bone',
    x: 'cataclysm:bone_reptile_chestplate'
  })
  
})