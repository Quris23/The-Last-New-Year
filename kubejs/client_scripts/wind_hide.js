// .minecraft/kubejs/client_scripts/wind_hide.js
// Прячет из JEI и креатива вырезанные предметы Wind's Spellbooks.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
    event.remove([
        'wind_spellbooks:wind_spell_book',
        'wind_spellbooks:wind_upgrade_orb'
    ])
})
