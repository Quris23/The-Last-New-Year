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
})
