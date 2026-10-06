// .minecraft/kubejs/server_scripts/hazen_remove_schools.js
// Hazen's Touve Lib: школы Сияние (radiance) и Тень (shadow) вырезаны - убираем крафты сфер и рун.
// Космос (cosmic) оставлен общедоступным (см. SpellClasses: ADDON_FREE_SCHOOLS).
ServerEvents.recipes(event => {
    event.remove({ output: 'hazentouvelib:radiance_rune' })
    event.remove({ output: 'hazentouvelib:shadow_rune' })
    event.remove({ output: 'hazentouvelib:radiance_upgrade_orb' })
    event.remove({ output: 'hazentouvelib:shadow_upgrade_orb' })
    // Echoing Magic: Космос вырезан - манускрипт больше не нужен
    event.remove({ output: 'echoing_magic:echoed_manuscript' })
})
