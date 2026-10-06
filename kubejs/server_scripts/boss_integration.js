// priority: -100
// Интеграция Боссов - крафты, в которых предметы с боссов заменяют обычные ингредиенты.
// Приоритет -100: файл выполняется ПОСЛЕ остальных (spell_crafts.js и т.д.), чтобы заменять
// и уже добавленные ими рецепты.
ServerEvents.recipes(event => {

    // 1) Поддельный Посох Уаджит: Слиток Древнего металла -> Перчатка Сирока (Boss's Rise)
    event.remove({ id: 'cataclysm_spellbooks:wadjets_staff' })
    event.shaped('cataclysm_spellbooks:fake_wudjets_staff', [
        'nan',
        ' b ',
        ' b '
    ], {
        b: 'cataclysm:koboleton_bone',
        a: 'block_factorys_bosses:sandworm_gauntlet',
        n: 'cataclysm:ancient_metal_nugget'
    })

    // 2) Арктический Клинок (Boreal Blade): Лёд -> Перчатка Скора (Boss's Rise), в обоих крафтах
    // 2а) на верстаке
    event.remove({ id: 'irons_spellbooks:boreal_blade' })
    event.shaped('irons_spellbooks:boreal_blade', [
        '  M',
        'VI ',
        'WV '
    ], {
        M: '#c:ingots/mithril',
        V: 'irons_spellbooks:ice_venom_vial',
        W: 'irons_spellbooks:weapon_parts',
        I: 'block_factorys_bosses:ice_gauntlet'
    })

    // 2б) механический (Create, последовательная сборка): тот же рецепт Create Wizardry, лёд заменён
    event.remove({ id: 'irons_spellbooks:sequenced_assembly/boreal_blade' })
    event.custom({
        type: 'create:sequenced_assembly',
        ingredient: { item: 'irons_spellbooks:weapon_parts' },
        results: [{
            id: 'irons_spellbooks:boreal_blade',
            components: {
                'irons_spellbooks:spell_container': {
                    data: [{ id: 'irons_spellbooks:frostbite', index: 0, level: 3, locked: true }],
                    maxSpells: 1,
                    mustEquip: false,
                    spellWheel: true
                }
            }
        }],
        sequence: [
            {
                type: 'create:filling',
                ingredients: [
                    { item: 'irons_spellbooks:weapon_parts' },
                    { type: 'neoforge:single', amount: 250, fluid: 'irons_spellbooks:ice_venom' }
                ],
                results: [{ id: 'irons_spellbooks:weapon_parts' }]
            },
            {
                type: 'create:deploying',
                ingredients: [
                    { item: 'irons_spellbooks:weapon_parts' },
                    { item: 'block_factorys_bosses:ice_gauntlet' }
                ],
                results: [{ id: 'irons_spellbooks:weapon_parts' }]
            },
            {
                type: 'create:deploying',
                ingredients: [
                    { item: 'irons_spellbooks:weapon_parts' },
                    { item: 'irons_spellbooks:mithril_ingot' }
                ],
                results: [{ id: 'irons_spellbooks:weapon_parts' }]
            },
            {
                type: 'create:pressing',
                ingredients: [{ item: 'irons_spellbooks:weapon_parts' }],
                results: [{ id: 'irons_spellbooks:weapon_parts' }]
            }
        ],
        transitional_item: { id: 'irons_spellbooks:weapon_parts' }
    })

    // 3) Броня Инженера: Руна Молнии -> Эссенция Шторма (Ender's Cataclysm).
    // Рецепты (кузнечный стол: Мантия Мага + Механизм Точности) заданы в spell_crafts.js - здесь
    // они пересоздаются с новой добавкой.
    const engineerPieces = [
        ['engineer_hood', 'netherite_mage_helmet'],
        ['engineer_suit', 'netherite_mage_chestplate'],
        ['engineer_leggings', 'netherite_mage_leggings'],
        ['engineer_boots', 'netherite_mage_boots']
    ]
    engineerPieces.forEach(([piece, base]) => {
        event.remove({ output: 'cataclysm_spellbooks:' + piece })
        event.custom({
            type: 'minecraft:smithing_transform',
            base: { item: 'irons_spellbooks:' + base },
            template: { item: 'create:precision_mechanism' },
            addition: { item: 'cataclysm:essence_of_the_storm' },
            result: { id: 'cataclysm_spellbooks:' + piece }
        })
    })

    // 4) Великий Мороз (Legendary Bosses): Железный Меч -> Арктический Клинок.
    // Урон 18 и высеченное заклинание (Frostbite 3, как у клинка) задаются в Java (SpellClasses).
    event.replaceInput({ output: 'legendary_monsters:the_great_frost' },
        'minecraft:iron_sword', 'irons_spellbooks:boreal_blade')

})
