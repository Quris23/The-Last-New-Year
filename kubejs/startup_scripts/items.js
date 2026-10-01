//.minecraft/kubejs/start_scripts/items.js
StartupEvents.registry('item', event => {

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

    event.create('incomplete_cursed_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Непростительного Проклятия')

    event.create('incomplete_mech_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око меха')

    event.create('incomplete_cryptic_eye', 'create:sequenced_assembly')
    .displayName('Незавершенное Око Безграничного Знания')
})

StartupEvents.registry('fluid', event => {
  event.create('chaos_fluid')
    .displayName('Жидкость хаоса')
    .stillTexture('kubejs:fluid/chaos_fluid_still')
    .flowingTexture('kubejs:fluid/chaos_fluid_flow')
    .noBlock()
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