// .minecraft/kubejs/server_scripts/wind_integration.js
// Wind's Spellbooks вплетён в штормовой класс: отдельные предметы школы Ветра вырезаны.
// (Класс Ветра убран, Стальной ветер и Ветропик отключены в config/irons_spellbooks_spell_config/wind_spellbooks.)
ServerEvents.recipes(event => {
    event.remove({ output: 'wind_spellbooks:wind_spell_book' })     // Книга бурь
    event.remove({ output: 'wind_spellbooks:wind_upgrade_orb' })    // Сфера улучшения ветра
    event.remove({ output: 'wind_spellbooks:wind_staff' })          // Посох ветра
    // Мантия Аэроманта и Руна ветра
    event.remove({ output: 'wind_spellbooks:aeromancer_helmet' })
    event.remove({ output: 'wind_spellbooks:aeromancer_chestplate' })
    event.remove({ output: 'wind_spellbooks:aeromancer_leggings' })
    event.remove({ output: 'wind_spellbooks:aeromancer_boots' })
    event.remove({ output: 'wind_spellbooks:wind_rune' })
})
