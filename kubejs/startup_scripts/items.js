// Все правки атрибутов (Fake Wadjet's Staff, Bloom Stone Staff, Desert Spellbook, Полное
// Одеяние Фараона, Трость Изобретателя, Броня Инженера) перенесены на чистую Java -
// см. SpellClasses/StaffAttributeOverrides.java.
//
// KubeJS-путь (item.setAttributeModifiers/addAttributeModifier через ItemEvents.modification)
// оказался ненадёжным: он ломает отображение тултипа предмета (пустая карточка без статов),
// и что важнее - выполняется НЕЗАВИСИМО от Java-фикса, так что оставленный здесь дублирующий
// код продолжал перетирать корректно выставленные Java'ой атрибуты обратно в битое состояние
// каждую загрузку игры. Ничего в этом файле больше не нужно - не добавляйте сюда новые правки
// атрибутов, делайте их в StaffAttributeOverrides.java.

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
})

ItemEvents.modification(event => { 
  event.modify('cataclysm:bone_reptile_helmet', item => {
    item.maxDamage = 385
  })
  event.modify('cataclysm:bone_reptile_chestplate', item => {
    item.maxDamage = 560
  })
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
})