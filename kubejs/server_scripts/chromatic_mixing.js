// Рецепты смешивания Create (с перегревом) из ветки yarick (create_integration.js): Хроматическое соединение,
// Изысканное сияние и Теневая сталь. Добавлено отдельным файлом.
ServerEvents.recipes(event => {

    // Хроматическое соединение
    event.recipes.create.mixing('create:chromatic_compound', [
        Item.of('glowstone_dust', 3),
        Item.of('create:powdered_obsidian', 3),
        'create:polished_rose_quartz'
    ]).superheated().id('generation:mixing_chromatic_compound')

    // Изысканное сияние
    event.recipes.create.mixing('create:refined_radiance', [
        Item.of('cataclysm:void_jaw', 3),
        Item.of('glow_ink_sac', 3),
        'create:chromatic_compound'
    ]).superheated().id('generation:mixing_refined_radiance')

    // Теневая сталь
    event.recipes.create.mixing('create:shadow_steel', [
        Item.of('cataclysm:black_steel_nugget', 3),
        Item.of('born_in_chaos_v1:dark_metal_nugget', 3),
        'create:chromatic_compound'
    ]).superheated().id('generation:mixing_shadow_steel')
})
