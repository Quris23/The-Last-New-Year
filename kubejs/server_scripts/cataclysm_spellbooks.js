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

  // Жаровня
  event.shaped('irons_spellbooks:brazier', [
    ' I ',
    'IFI',
    ' I '
  ], {
    I: 'iron_bars',
    F: 'minecraft:campfire'
  })

  // Жаровня душ
  event.shaped('irons_spellbooks:brazier_soul', [
    ' I ',
    'IFI',
    ' C '
  ], {
    I: 'iron_bars',
    F: 'minecraft:soul_campfire',
    C: 'cataclysm:cursium_ingot'
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
  event.custom({
    type: 'minecraft:smithing_transform',
    base: { item: 'irons_spellbooks:netherite_mage_helmet' },
    template: { item: 'create:precision_mechanism' },
    addition: { item: 'irons_spellbooks:lightning_rune' },
    result: { id: 'cataclysm_spellbooks:engineer_hood' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    base: { item: 'irons_spellbooks:netherite_mage_chestplate' },
    template: { item: 'create:precision_mechanism' },
    addition: { item: 'irons_spellbooks:lightning_rune' },
    result: { id: 'cataclysm_spellbooks:engineer_suit' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    base: { item: 'irons_spellbooks:netherite_mage_leggings' },
    template: { item: 'create:precision_mechanism' },
    addition: { item: 'irons_spellbooks:lightning_rune' },
    result: { id: 'cataclysm_spellbooks:engineer_leggings' }
  })
  event.custom({
    type: 'minecraft:smithing_transform',
    base: { item: 'irons_spellbooks:netherite_mage_boots' },
    template: { item: 'create:precision_mechanism' },
    addition: { item: 'irons_spellbooks:lightning_rune' },
    result: { id: 'cataclysm_spellbooks:engineer_boots' }
  })
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