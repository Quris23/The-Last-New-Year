// Все правки мода Somake's Spells, которые делаются в KubeJS (рецепты и т.п.). Новые правки к Somake писать сюда.
// Атрибуты оружия Somake и высеченные заклинания делаются в Java (SpellClasses): Somake строит их лениво, KubeJS их не видит.
ServerEvents.recipes(event => {
    // Глеф из Пирия (бывшая Глефа из Визерита): механическая сборка
    event.remove({ output: 'somakespells:witherite_glaive' })
    event.recipes.create.mechanical_crafting('somakespells:witherite_glaive', [
        ' P',
        'CP',
        'SI',
        'R '
    ], {
        P: 'irons_spellbooks:pyrium_ingot',
        C: 'somakespells:fire',
        S: 'irons_spellbooks:pyrium_staff',
        I: 'cataclysm:ignitium_ingot',
        R: 'kubejs:black_steel_rod'
    })
})
