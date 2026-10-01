// server_scripts/catac_crafts.js
ServerEvents.recipes(event => {

  // Камень Пустоты
  let shard = ['amethyst_shard', 'northstar:lunar_sapphire_shard']
  let block = ['amethyst_block', 'northstar:lunar_sapphire_block']

  event.shaped('cataclysm:void_crystal', [
    'AAA',
    'AOA',
    'AAA'
  ], {
    O: ['crying_obsidian', 'obsidian'],
    A: shard
  })

  let polShard = ['northstar:polished_amethyst', 'northstar:polished_lunar_sapphire']
  let polBlock = ['amethyst_block', 'northstar:lunar_sapphire_block']

  event.shaped('cataclysm:void_stone', [
    'AAA',
    'AOA',
    'AAA'
  ], {
    O: 'cataclysm:polished_obsidian',
    A: polShard
  })

  event.shaped(Item.of('cataclysm:void_stone', 4), [
    'VV',
    'VV'
  ], {
    V: 'cataclysm:void_crystal'
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

  event.shapeless(Item.of('cataclysm:void_jaw', 9), [
    'cataclysm:void_lantern_block'
  ])

  event.shaped('cataclysm:void_lantern_block', [
    'SSS',
    'SSS',
    'SSS'
  ], {
    S: 'cataclysm:void_jaw'
  })

  // Визерит
  event.recipes.create.mechanical_crafting('cataclysm:mechanical_fusion_anvil', [
    ' RIR ',
    'IWPWI',
    'RWAWR',
    'IWCWI',
    ' RIR '
  ], {
    R: 'minecraft:redstone_block',
    I: 'minecraft:iron_block',
    W: 'cataclysm:witherite_ingot',
    P: 'create:precision_mechanism',
    A: 'minecraft:anvil',
    C: 'create:cogwheel'
  })

  event.recipes.create.mechanical_crafting('cataclysm:meat_shredder', [
    '  I  ',
    ' IRI ',
    'IRHRI',
    ' IEI ',
    '  I  ',
    '  W  ',
    '  W  '
  ], {
    R: 'minecraft:redstone_block',
    I: 'create:iron_sheet',
    W: 'cataclysm:witherite_ingot',
    H: 'dndesires:handheld_saw',
    E: 'createdieselgenerators:large_diesel_engine'
  })

  let ironBlocks = ['minecraft:iron_block', 'create:industrial_iron_block', 'create:weathered_iron_block']
  let smartPipe = ['create:smart_fluid_pipe', 'createcasing:andesite_smart_fluid_pipe', 'createcasing:brass_smart_fluid_pipe', 'createcasing:zinc_smart_fluid_pipe']
  let fluidPipe = ['create:fluid_pipe', 'createcasing:andesite_fluid_pipe', 'createcasing:brass_fluid_pipe', 'createcasing:zinc_fluid_pipe']
  event.recipes.create.mechanical_crafting('cataclysm:laser_gatling', [
    '  WOOO',
    'IGPRSF',
    'IIIW  '
  ], {
    W: 'cataclysm:witherite_ingot',
    O: 'create:sturdy_sheet',
    I: ironBlocks,
    G: 'dndesires:gatling_breaker',
    P: 'northstar:hardened_precision_mechanism',
    R: 'minecraft:redstone_block',
    S: smartPipe,
    F: fluidPipe
  })

  let withered = ['minecraft:wither_rose', 'minecraft:wither_skeleton_skull', 'legendary_monsters:withered_bone', 'legendary_monsters:withered_horn']

  event.recipes.create.mechanical_crafting('cataclysm:wither_assault_shoulder_weapon', [
    'VWTC',
    'WRB '
  ], {
    V: withered,
    W: 'cataclysm:witherite_ingot',
    T: 'minecraft:tnt',
    C: 'createdieselgenerators:chemical_sprayer_lighter',
    R: 'minecraft:redstone_block',
    B: 'createdieselgenerators:burner'
  })

  // Древний металл
  event.shaped('cataclysm:khopesh', [
    ' A ',
    'A  ',
    ' AB'
  ], {
    A: 'cataclysm:ancient_metal_ingot',
    B: 'cataclysm:koboleton_bone'
  })

  event.recipes.create.mechanical_crafting('cataclysm:ancient_spear', [
    '   A',
    ' RA ',
    ' SR ',
    'B   '
  ], {
    A: 'cataclysm:ancient_metal_ingot',
    R: 'kubejs:aurumite_ingot',
    B: 'cataclysm:koboleton_bone',
    S: 'alexscaves:limestone_spear'
  })

  event.shaped('kubejs:ancient_upgrade_smithing_template', [
    'DKD',
    'DAD',
    'DDD'
  ], {
    A: 'kubejs:aurumite_ingot',
    D: 'minecraft:diamond',
    K: 'cataclysm:kobolediator_skull'
  })

  event.shaped(Item.of('kubejs:ancient_upgrade_smithing_template', 2), [
    'DKD',
    'DAD',
    'DDD'
  ], {
    A: 'kubejs:aurumite_ingot',
    D: 'minecraft:diamond',
    K: 'kubejs:ancient_upgrade_smithing_template'
  })

  event.recipes.minecraft.smithing_transform(
    'cataclysm:bone_reptile_helmet',
    'kubejs:ancient_upgrade_smithing_template',
    'minecraft:diamond_helmet',
    'cataclysm:ancient_metal_ingot'
  )

  event.recipes.minecraft.smithing_transform(
    'cataclysm:bone_reptile_chestplate',
    'kubejs:ancient_upgrade_smithing_template',
    'minecraft:diamond_chestplate',
    'cataclysm:ancient_metal_ingot'
  )

  event.shaped('cataclysm:vitality_ankh', [
    ' N ',
    'ABA',
    ' B '
  ], {
    B: 'cataclysm:koboleton_bone',
    A: 'cataclysm:ancient_metal_ingot',
    N: 'cataclysm:ancient_metal_nugget'
  })

  let skullTransitional = 'kubejs:incomplete_unbreakable_skull'
  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm:unbreakable_skull', 0.80),
    CreateItem.of('cataclysm:kobolediator_skull', 0.20)
  ], 'cataclysm:kobolediator_skull', [
    event.recipes.createDeploying(skullTransitional, [skullTransitional, 'cataclysm:ancient_metal_ingot']),
    event.recipes.createDeploying(skullTransitional, [skullTransitional, 'cataclysm:koboleton_bone']),
    event.recipes.createFilling(skullTransitional, [skullTransitional, Fluid.of('fluid:haunting_fluid', 27)]),
    event.recipes.createPressing(skullTransitional, skullTransitional)
  ]).transitionalItem(skullTransitional).loops(1)

  // Эссенция Шторма
  event.recipes.create.mechanical_crafting('cataclysm:ceraunus', [
    ' LBL ',
    'E P E',
    '  P  ',
    '  A  ',
    '  D  '
  ], {
    E: 'cataclysm:essence_of_the_storm',
    L: 'cataclysm:lacrima',
    A: 'legendary_monsters:monstrous_anchor',
    P: 'minecraft:prismarine_shard',
    B: 'minecraft:prismarine_bricks',
    D: 'minecraft:dark_prismarine'
  })

  let tridents = ['minecraft:trident', 'cataclysm:coral_spear', 'cataclysm:coral_bardiche', 'block_factorys_bosses:kraken_trident']
  event.recipes.create.mechanical_crafting('cataclysm:astrape', [
    ' L ',
    'ELE',
    ' T ',
    ' P ',
    ' B '
  ], {
    E: 'cataclysm:essence_of_the_storm',
    L: 'cataclysm:lacrima',
    T: tridents,
    P: 'minecraft:prismarine_shard',
    B: 'minecraft:prismarine_bricks'
  })
    
  event.custom({
    type: 'cataclysm:weapon_fusion',
    base: { item: 'cataclysm:infernal_forge' },
    addition: { item: 'irons_spellbooks:twilight_gale' },
    result: { id: 'cataclysm:brontes' }
  })

  // Проклятый металл
  event.recipes.create.pressing('kubejs:cursium_sheet', 'cataclysm:cursium_ingot')
  
  event.recipes.create.mechanical_crafting('cataclysm:cursed_bow', [
    ' CS',
    'L S',
    'B S',
    'B S',
    ' CS'
  ], {
    C: 'cataclysm:cursium_ingot',
    S: 'minecraft:string',
    B: 'kubejs:black_steel_rod',
    L: 'createaddition:brass_rod'
  })

  event.recipes.create.mechanical_crafting('cataclysm:the_annihilator', [
    'S',
    'C',
    'L',
    'K',
    'B'
  ], {
    S: 'kubejs:cursium_sheet',
    C: 'cataclysm:cursium_ingot',
    K: 'cataclysm:strange_key',
    B: 'kubejs:black_steel_rod',
    L: 'createaddition:brass_rod'
  })

  event.recipes.create.mechanical_crafting('cataclysm:soul_render', [
    'CB ',
    'CLS',
    ' K ',
    ' B ',
    ' B '
  ], {
    S: 'kubejs:cursium_sheet',
    C: 'cataclysm:cursium_ingot',
    B: 'kubejs:black_steel_rod',
    L: 'createaddition:brass_rod',
    K: 'cataclysm:strange_key'
  })

  let upgrTransitional = 'kubejs:incomplete_cursium_upgrade_smithing_template'
  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm:cursium_upgrade_smithing_template', 0.70),
    CreateItem.of('minecraft:gold_ingot', 0.15),
    CreateItem.of('cataclysm:black_steel_ingot', 0.10),
    CreateItem.of('minecraft:netherite_upgrade_smithing_template', 0.05)
  ], 'minecraft:netherite_upgrade_smithing_template', [
    event.recipes.createDeploying(upgrTransitional, [upgrTransitional, 'kubejs:aurumite_ingot']),
    event.recipes.createDeploying(upgrTransitional, [upgrTransitional, 'cataclysm:black_steel_ingot']),
    event.recipes.createDeploying(upgrTransitional, [upgrTransitional, 'kubejs:aurumite_ingot']),
    event.recipes.createFilling(upgrTransitional, [upgrTransitional, Fluid.of('fluid:haunting_fluid', 125)]),
    event.recipes.createPressing(upgrTransitional, upgrTransitional)
  ]).transitionalItem(upgrTransitional).loops(2)

  event.shaped('cataclysm:ring_of_grudged', [
    'ACA',
    'NRN',
    ' B '
  ], {
    C: 'cataclysm:cursium_ingot',
    A: 'kubejs:aurumite_scrap',
    R: 'irons_spellbooks:affinity_ring',
    B: 'cataclysm:black_steel_ingot',
    N: 'cataclysm:black_steel_nugget'
  })

  event.shaped('cataclysm:berserker_soul_amulet', [
    'NBN',
    'AHB',
    'CAN'
  ], {
    C: 'cataclysm:cursium_ingot',
    A: 'kubejs:aurumite_ingot',
    H: 'irons_spellbooks:heavy_chain_necklace',
    B: 'cataclysm:black_steel_ingot',
    N: 'cataclysm:black_steel_nugget'
  })

  // Игнит
  event.shaped('cataclysm:burning_ashes', [
    ' D ',
    'DLD',
    ' D '
  ], {
    L: 'cataclysm:lava_power_cell',
    D: 'cataclysm:dying_ember'
  })
  
  event.recipes.create.mechanical_crafting('cataclysm:the_incinerator', [
    ' R ',
    'RIR',
    'RIR',
    'RUR',
    ' S '
  ], {
    I: 'cataclysm:ignitium_ingot',
    R: 'minecraft:blaze_rod',
    U: 'cataclysm:ignitium_upgrade_smithing_template',
    S: 'minecraft:netherite_sword'
  })

  let shields = ['minecraft:shield', 'block_factorys_bosses:enhanced_shield', 'cataclysm:black_steel_targe']
  event.recipes.create.mechanical_crafting('cataclysm:bulwark_of_the_flame', [
    'BRB',
    'RIR',
    'RSR',
    'RIR',
    'BRB'
  ], {
    I: 'cataclysm:ignitium_ingot',
    R: 'minecraft:blaze_rod',
    B: 'minecraft:nether_brick',
    S: shields
  })

  let grips = ['artifacts:fire_gauntlet', 'artifacts:power_glove']
  event.shaped('cataclysm:blazing_grips', [
    'NIN',
    'NGN',
    'NBN'
  ], {
    I: 'cataclysm:ignitium_ingot',
    G: grips,
    B: 'cataclysm:lava_power_cell',
    N: 'nether_brick'
  })

  let upgrTransit = 'kubejs:incomplete_ignitium_upgrade_smithing_template'
  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm:ignitium_upgrade_smithing_template', 0.70),
    CreateItem.of('minecraft:nether_brick', 0.15),
    CreateItem.of('netherite_scrap', 0.10),
    CreateItem.of('minecraft:netherite_upgrade_smithing_template', 0.05)
  ], 'minecraft:netherite_upgrade_smithing_template', [
    event.recipes.createDeploying(upgrTransit, [upgrTransit, 'blaze_powder']),
    event.recipes.createDeploying(upgrTransit, [upgrTransit, 'netherite_scrap']),
    event.recipes.createDeploying(upgrTransit, [upgrTransit, 'nether_brick']),
    event.recipes.createFilling(upgrTransit, [upgrTransit, Fluid.of('lava', 500)]),
    event.recipes.createPressing(upgrTransit, upgrTransit)
  ]).transitionalItem(upgrTransit).loops(2)

  // Эндеритовый блок
  let endTransit = 'crying_obsidian'
  event.recipes.create.sequenced_assembly([
    CreateItem.of('cataclysm:enderite_block', 0.80),
    CreateItem.of('crying_obsidian', 0.10),
    CreateItem.of('cataclysm:polished_obsidian', 0.10),
  ], 'crying_obsidian', [
    event.recipes.createDeploying(endTransit, [endTransit, 'ender_pearl']),
    event.recipes.createPressing(endTransit, endTransit),
    event.recipes.createDeploying(endTransit, [endTransit, 'cataclysm:void_jaw']),
    event.recipes.createPressing(endTransit, endTransit),
    event.recipes.createDeploying(endTransit, [endTransit, 'glow_ink_sac']),
    event.recipes.createPressing(endTransit, endTransit)
  ]).transitionalItem(endTransit).loops(3)
  
})
