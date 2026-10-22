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

    // Магическая ткань
    event.shaped('irons_spellbooks:magic_cloth', [
        'EEE',
        'EAE',
        'EEE'
    ], {
        A: 'northstar:durable_fabric',
        E: 'irons_spellbooks:arcane_essence'
    })

    event.recipes.create.filling(
        'irons_spellbooks:magic_cloth',
    ['northstar:durable_fabric', Fluid.of('create_wizardry:mana', 500)])

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
    I: 'born_in_chaos_v1:dark_grid',
    F: 'minecraft:soul_campfire',
    C: 'cataclysm:cursium_ingot'
  })

    // Хемп вместо картошки
    //  event.get('irons_spellbooks:nature_focus')
    //      .remove('minecraft:poisonous_potato')
    //      .add('nirvana:hemp')

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

    // Броня светоносца
    event.remove({ id: 'irons_spellbooks:paladin_chestplate' })

    const pieces = {
      helmet: 'spellclasses:paladin_helmet',
      chestplate: 'irons_spellbooks:paladin_chestplate',
      leggings: 'spellclasses:paladin_leggings',
      boots: 'spellclasses:paladin_boots'
    }
    Object.keys(pieces).forEach(piece => {
      event.shaped(pieces[piece], [
        'GDG',
        'WAW',
        'GWG'
      ], {
        G: '#c:ingots/gold',
        D: 'irons_spellbooks:divine_soulshard',
        W: 'irons_spellbooks:mithril_weave',
        A: `irons_spellbooks:netherite_mage_${piece}`
      }).id(`generation:paladin_${piece}`)
    })

    // Броня пугала
    const recipes = {
      helmet: [' C ', 'CNC', 'VPV'],
      chestplate: ['HVH', 'CNC', 'C C'],
      leggings: ['HNH', 'C C', 'V V'],
      boots: ['HNH', 'C C', 'V V']
    }
    Object.keys(recipes).forEach(piece => {
      event.remove({ id: `irons_spellbooks:pumpkin_${piece}` })
      const key = {
        C: 'irons_spellbooks:magic_cloth',
        N: `irons_spellbooks:netherite_mage_${piece}`,
        V: 'irons_spellbooks:blood_vial'
      }
      if (piece === 'helmet') key.P = 'minecraft:carved_pumpkin'
      else key.H = 'minecraft:hay_block'
      event.shaped(`irons_spellbooks:pumpkin_${piece}`, recipes[piece], key).id(`generation:pumpkin_${piece}`)
    })
    
    // Шкура дракона
    let skinTrans = 'phantom_membrane'
    event.recipes.create.sequenced_assembly([
        CreateItem.of('irons_spellbooks:dragonskin', 0.50),
        CreateItem.of('phantom_membrane', 0.50)
    ], skinTrans, [
        event.recipes.createDeploying(skinTrans, [skinTrans, 'end_crystal']),
        event.recipes.createFilling(skinTrans,  [skinTrans, Fluid.of('irons_spellbooks:timeless_slurry', 500)]),
        event.recipes.createDeploying(skinTrans, [skinTrans, ['crying_obsidian', 'obsidian']]),
        event.recipes.createFilling(skinTrans,  [skinTrans, Fluid.of('create_dragons_plus:dragon_breath', 500)]),
        event.recipes.createPressing(skinTrans, skinTrans)
    ]).transitionalItem(skinTrans).loops(3)

    // Незеритовая броня боевого мага
    const classRobes = ['priest', 'cultist', 'plagued', 'electromancer', 'pyromancer', 'cryomancer', 'shadowwalker']
    const pieces = ['helmet', 'chestplate', 'leggings', 'boots']

    pieces.forEach(piece => {
        event.remove({ output: `irons_spellbooks:netherite_mage_${piece}` })
        event.custom({
            type: 'minecraft:smithing_transform',
            template: { item: 'minecraft:netherite_upgrade_smithing_template' },
            base: classRobes.map(robe => ({ item: `irons_spellbooks:${robe}_${piece}` })),
            addition: { tag: 'c:ingots/netherite' },
            result: { id: `irons_spellbooks:netherite_mage_${piece}`, count: 1 }
        })
    })
})