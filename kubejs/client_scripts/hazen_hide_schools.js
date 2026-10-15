// .minecraft/kubejs/client_scripts/hazen_hide_schools.js
// Прячет предметы вырезанных школ Hazen's Touve Lib (Сияние, Тень) из JEI и креатива.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
    event.remove([
        'hazentouvelib:radiance_rune',
        'hazentouvelib:shadow_rune',
        'hazentouvelib:radiance_upgrade_orb',
        'hazentouvelib:shadow_upgrade_orb',
        'echoing_magic:echoed_manuscript'
    ])
})
