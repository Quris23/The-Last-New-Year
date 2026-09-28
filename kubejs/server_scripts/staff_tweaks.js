// Правки крафта посохов Cataclysm: Spellbooks (баффы см. startup_scripts/staff_attributes.js)
ServerEvents.recipes(event => {
    // Void Staff - база фьюжна заменена с Трости Изобретателя на Палочку Туда-Обратно
    event.remove({ output: 'cataclysm_spellbooks:void_staff' })
    event.custom({
        type: 'cataclysm:weapon_fusion',
        base: { item: 'irons_spellbooks:hither_thither_wand' },
        addition: { item: 'cataclysm:void_core' },
        result: { id: 'cataclysm_spellbooks:void_staff' }
    })

    // Brontes - добавка фьюжна заменена с Астрапы на Twilight Gale (заклинание см. BrontesSpell.java)
    event.remove({ output: 'cataclysm:brontes' })
    event.custom({
        type: 'cataclysm:weapon_fusion',
        base: { item: 'cataclysm:infernal_forge' },
        addition: { item: 'irons_spellbooks:twilight_gale' },
        result: { id: 'cataclysm:brontes' }
    })
})
