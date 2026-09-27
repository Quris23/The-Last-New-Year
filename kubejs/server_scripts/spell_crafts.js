// Крафты для предметов Cataclysm: Spellbooks (кроме книг заклинаний - те остаются в spellbooks_crafts.js)
ServerEvents.recipes(event => {
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
