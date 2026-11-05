//.minecraft/kubejs/start_scripts/items.js
StartupEvents.registry('item', event => {

    //event.create('echo_ender_rune').displayName('Руна эхо-эндера')
    event.create('dark_chorium_steel').displayName('Тёмно-хориевая сталь')

    event.create('ancient_upgrade_smithing_template', 'smithing_template')
    .displayName('Кузнечный шаблон: Улучшение костей рептилии')
    .appliesTo('§9Алмазному снаряжению')
    .ingredients('§9Слиток древнего металла')
    
    //event.create('ancient_upgrade_smithing_template', 'smithing_template').displayName('Кузнечный шаблон')//.tooltip('§7§oУлучшение костей рептилии')
    //event.create('ancient_upgrade_smithing_template').displayName('Кузнечный шаблон').tooltip('§7§oУлучшение костей рептилии')

    event.create('incomplete_unbreakable_skull', 'create:sequenced_assembly')
    .displayName('Незавершенный неразрушаемый череп')

    event.create('black_steel_rod').displayName('Черностальной стержень')

    event.create('cursium_sheet').displayName('Проклятый лист')

    event.create('aurumite_ingot').displayName('Аурумитовый слиток').rarity('uncommon')
    event.create('aurumite_scrap').displayName('Аурумитовый лом')

    event.create('mithridium_ingot').displayName('Мифридиновый слиток').rarity('uncommon')

    event.create('incomplete_cursium_upgrade_smithing_template', 'create:sequenced_assembly')
    .displayName('Незавершенный кузнечный шаблон')
    event.create('incomplete_ignitium_upgrade_smithing_template', 'create:sequenced_assembly')
    .displayName('Незавершенный кузнечный шаблон')
    //event.create('bronze_ingot').displayName('Бронзовый слиток')
    //event.create('bronze_scrap').displayName('Бронзовый осколок')   
    
    // Artifacts
    event.create('incomplete_warp_drive', 'create:sequenced_assembly')
    .displayName('Незавершенный варп-двигатель')

    // Iron's spell books

    event.create('incomplete_uportal_frame', 'create:sequenced_assembly')
    .displayName('Незавершенная рамка портала')

    event.create('nethertitanium_ingot').displayName('Слиток незер-титана')

    event.create('chorium_jewel').displayName('Пластина хориума')

    event.create('incomplete_enchanted_apple', 'create:sequenced_assembly')
    .displayName('Незавершенное зачарованное золотое Яблоко')

    event.create('incomplete_death_totem', 'create:sequenced_assembly')
    .displayName('Незавершенный Тотем смерти')

    event.create('incomplete_chorus_totem', 'create:sequenced_assembly')
    .displayName('Незавершенный Тотем хоруса')

    // Endrem + Cataclysm

    event.create('incomplete_mech_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око меха')

    event.create('incomplete_cryptic_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Безграничного Знания')

    event.create('incomplete_lost_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Истинной Скверны')

    event.create('incomplete_wither_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Вечных Бурь')

    event.create('incomplete_nether_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Адского Пламени')

    event.create('incomplete_rogue_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Фрактального Краха')

    event.create('incomplete_cursed_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Непростительного Проклятия')

    // Многомерный куб (witherstormmod:multidimensional_dice): промежуточный предмет конвейерной сборки.
    // Модель - куб без свечения, переливания и анимации, иероглифы бледные (assets/kubejs/models/item/incomplete_multidimensional_dice_full.json)
    event.create('incomplete_multidimensional_dice', 'create:sequenced_assembly')
    .displayName('Незавершенный многомерный куб')
    .parentModel('kubejs:item/incomplete_multidimensional_dice_full')
        
        
})

StartupEvents.registry('fluid', event => {
    // Жидкость Хаоса
    event.create('chaos_fluid')
      .displayName('Жидкость хаоса')
      .stillTexture('kubejs:fluid/chaos_fluid_still')
      .flowingTexture('kubejs:fluid/chaos_fluid_flow')
      .noBlock()

    // Жидкость аннигиляции
    event.create('annihilation_fluid')
      .displayName('Жидкость аннигиляции')
      .stillTexture('kubejs:fluid/annihilation_fluid_still')
      .flowingTexture('kubejs:fluid/annihilation_fluid_flow')
      //.noBlock()

    // Облако 
    event.create('cloud_fluid')
      .displayName('Облачная жидкость')
      .stillTexture('kubejs:fluid/cloud_still')
      .flowingTexture('kubejs:fluid/cloud_flowing')
      //.noBlock()

    // Жидкость контроля 
    event.create('control_fluid')
      .displayName('Жидкость контроля')
      .stillTexture('kubejs:fluid/control_fluid_still')
      .flowingTexture('kubejs:fluid/control_fluid_flowing')
      //.noBlock()
})

ItemEvents.modification(event => { 
  // маг тёмный металл
  event.modify('somakespells:dark_metal_battlemage_helmet', item => {
    item.maxDamage = 550; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('somakespells:dark_metal_battlemage_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('somakespells:dark_metal_battlemage_leggings', item => {
    item.maxDamage = 750; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('somakespells:dark_metal_battlemage_boots', item => {
    item.maxDamage = 650; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Чудовищные шлем + шляпа волшебника
  event.modify('cataclysm:monstrous_helm', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:monstrous_wizard_hat', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Иженер
  event.modify('cataclysm_spellbooks:engineer_hood', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:engineer_suit', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:engineer_leggings', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:engineer_boots', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Бездна
  event.modify('cataclysm_spellbooks:abyssal_warlock_mask', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:abyssal_warlock_helmet', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:abyssal_warlock_chestplate', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:abyssal_warlock_leggings', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:abyssal_warlock_boots', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Цветущий камень
  event.modify('cataclysm_spellbooks:bloom_stone_hat', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:bloom_stone_chestplate', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:bloom_stone_skirt', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:bloom_stone_greaves', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Рептилия
  event.modify('cataclysm:bone_reptile_helmet', item => {
    item.maxDamage = 385; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:bone_reptile_chestplate', item => {
    item.maxDamage = 560; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Фараон
  event.modify('cataclysm_spellbooks:pharaoh_helmet', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:pharaoh_chestplate', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:pharaoh_leggings', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:pharaoh_greaves', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Призрачная
  event.modify('cataclysm:cursium_helmet', item => {
    item.maxDamage = 550; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:cursium_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:cursium_leggings', item => {
    item.maxDamage = 750; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:cursium_boots', item => {
    item.maxDamage = 650; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Магическая призрачная
  event.modify('cataclysm_spellbooks:cursium_mage_circlet', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:cursium_mage_chestplate', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:cursium_mage_elytra', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:cursium_mage_skirt', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:cursium_mage_boots', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Игнитовая
  event.modify('cataclysm:ignitium_helmet', item => {
    item.maxDamage = 550; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:ignitium_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:ignitium_elytra_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:ignitium_leggings', item => {
    item.maxDamage = 750; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm:ignitium_boots', item => {
    item.maxDamage = 650; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Магическая игнитовая
  event.modify('cataclysm_spellbooks:ignis_helmet', item => {
    item.maxDamage = 478; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:ignis_chestplate', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:ignis_chestplate_elytra', item => {
    item.maxDamage = 696; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:ignis_leggings', item => {
    item.maxDamage = 652; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('cataclysm_spellbooks:ignis_boots', item => {
    item.maxDamage = 565; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // маг тёмный металл
  event.modify('echoing_magic:reinforced_juggernaut_helmet', item => {
    item.maxDamage = 550; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('echoing_magic:reinforced_juggernaut_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('echoing_magic:reinforced_juggernaut_leggings', item => {
    item.maxDamage = 750; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('echoing_magic:reinforced_juggernaut_boots', item => {
    item.maxDamage = 650; item.damage = 0; item.remove('minecraft:unbreakable')  })
  
  // Оплот пламени
  event.modify('cataclysm:bulwark_of_the_flame', item => {
    item.maxDamage = 840; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Перчатка защиты
  event.modify('cataclysm:gauntlet_of_guard', item => {
    item.maxDamage = 1561; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Перчатка оплота
  event.modify('cataclysm:gauntlet_of_bulwark', item => {
    item.maxDamage = 1680; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Перчатка вихря
  event.modify('cataclysm:gauntlet_of_maelstrom', item => {
    item.maxDamage = 1680; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Испепелитель
  event.modify('cataclysm:the_incinerator', item => {
    item.maxDamage = 4000; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Проклятый лук
  event.modify('cataclysm:cursed_bow', item => {
    item.maxDamage = 465; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Гнев пустыни
  event.modify('cataclysm:wrath_of_the_desert', item => {
    item.maxDamage = 465; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Расплата душ
  event.modify('cataclysm:soul_render', item => {
    item.maxDamage = 1785; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Аннигилятор
  event.modify('cataclysm:the_annihilator', item => {
    item.maxDamage = 1561; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Жертвенник
  event.modify('cataclysm:the_immolator', item => {
    item.maxDamage = 1680; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Мясорубка
  event.modify('cataclysm:meat_shredder', item => {
    item.maxDamage = 1561; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Визеритовый гранатомёт
  event.modify('cataclysm:wither_assault_shoulder_weapon', item => {
    item.maxDamage = 465; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Пустотный гранатомёт
  event.modify('cataclysm:void_assault_shoulder_weapon', item => {
    item.maxDamage = 465; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Пустотный горн
  event.modify('cataclysm:void_forge', item => {
    item.maxDamage = 2031; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Приливные когти
  event.modify('cataclysm:tidal_claws', item => {
    item.maxDamage = 1561; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Незеритовый горн
  event.modify('cataclysm:infernal_forge', item => {
    item.maxDamage = 1785; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Астрапа
  event.modify('cataclysm:astrape', item => {
    item.maxDamage = 1680; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Керавн
  event.modify('cataclysm:ceraunus', item => {
    item.maxDamage = 1785; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Молот Грома
  event.modify('cataclysm:brontes', item => {
    item.maxDamage = 2031; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Кузня адского огня
  event.modify('cataclysm_spellbooks:hellfire_forge', item => {
    item.maxDamage = 2031; item.damage = 0; item.remove('minecraft:unbreakable')  })

  // Топор, рождённый скалком
  event.modify('somakespells:sculkborne_axe', item => {
    item.maxDamage = 2519; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Запретный молот
  event.modify('somakespells:forbidden_hammer', item => {
    item.maxDamage = 1785; item.damage = 0; item.remove('minecraft:unbreakable')  })

  // Аннигилятор
  event.modify('legendary_monsters:annihilator_helmet', item => {
    item.maxDamage = 550; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('legendary_monsters:annihilator_chestplate', item => {
    item.maxDamage = 800; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('legendary_monsters:annihilator_leggings', item => {
    item.maxDamage = 750; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('legendary_monsters:annihilator_boots', item => {
    item.maxDamage = 650; item.damage = 0; item.remove('minecraft:unbreakable')  })
  event.modify('legendary_monsters:the_tesseract', item => {
    item.maxDamage = 2519; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Великий меч душ
  event.modify('legendary_monsters:soul_great_sword', item => {
    item.maxDamage = 2519; item.damage = 0; item.remove('minecraft:unbreakable')  })
  // Топор Молнии
  event.modify('legendary_monsters:axe_of_lightning', item => {
    item.maxDamage = 2519; item.damage = 0; item.remove('minecraft:unbreakable')  })
})

  // Legendary Monsters
StartupEvents.registry('block', event => {
  event.create('lunar_enderitium_ore')
    .texture('kubejs:block/lunar_enderitium_ore')
    .hardness(2.5)
    .resistance(2.5)
    .stoneSoundType()
    .requiresTool()
    .tagBlock('minecraft:mineable/pickaxe')
    .noDrops() // лут-таблица своя, см. kubejs/data/kubejs/loot_table/blocks/lunar_enderitium_ore.json

  // Born in Chaos: заражённый чёрный аргилит (как заражённый камень: прочность вдвое меньше, без инструмента;
  // с шёлковым касанием — сам блок, иначе чешуйница, см. server_scripts/infested_argillite.js)
  event.create('infested_black_argillite')
    .displayName('Infested Black Argillite')
    .texture('born_in_chaos_v1:block/tiemnyikirpich6')
    .hardness(2.0)
    .resistance(0.75)
    .soundType('deepslate')
    .tagBlock('minecraft:mineable/pickaxe')
    .noDrops() // лут-таблица своя, см. kubejs/data/kubejs/loot_table/blocks/infested_black_argillite.json
})