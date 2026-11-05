// Прячет из JEI и креатива вырезанные предметы Legendary Spellbooks (рецепты убраны в server_scripts/remove.js).
RecipeViewerEvents.removeEntriesCompletely('item', event => {
    event.remove([
        'legendary_spellbooks:stormbound_grimoire',
        'legendary_spellbooks:stormmancer_hood',
        'legendary_spellbooks:stormmancer_robe',
        'legendary_spellbooks:stormmancer_leggings',
        'legendary_spellbooks:stormmancer_boots',
        'legendary_spellbooks:oblivionmancer_robe',
        'legendary_spellbooks:oblivionmancer_hat',
        'legendary_spellbooks:oblivionmancer_leggings',
        'legendary_spellbooks:oblivionmancer_boots'
    ])
})
