// .minecraft/kubejs/client_scripts/hide_items.js
// Полностью убирает предметы из JEI и креативных вкладок (не только рецепт - см. server_scripts/remove.js)
RecipeViewerEvents.removeEntriesCompletely('item', event => {
    event.remove([
        'cataclysm_spellbooks:spirit_sunderer',
        'cataclysm_spellbooks:excelsius_speed_visors',
        'cataclysm_spellbooks:excelsius_speed_chestplate',
        'cataclysm_spellbooks:excelsius_power_visors',
        'cataclysm_spellbooks:excelsius_power_chestplate',
        'cataclysm_spellbooks:excelsius_resist_visors',
        'cataclysm_spellbooks:excelsius_resist_chestplate',
        'cataclysm_spellbooks:excelsius_leggings',
        'cataclysm_spellbooks:excelsius_greaves',
        'cataclysm_spellbooks:ignis_chestplate_elytra',
        'cataclysm_spellbooks:cursium_mage_elytra',
        'artifacts:everlasting_beef',
        'artifacts:eternal_steak',
        'aerowarptics:rift_gate_frame',
        'aerowarptics:rift_gate'
    ])
})
