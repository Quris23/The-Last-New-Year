// Свои рецепты оружия Hazen N' Stuff.
// Мурамаса:
//  . . M
//  S M .
//  B S .
// M - мифриловый слиток, S - кожа монстра, B - латунный лист (Create).
// Высеченное заклинание Шторма на неё накладывается на Арканной наковальне (SpellClasses, WeaponSpells), ПКМ его кастует.
ServerEvents.recipes(event => {
    event.remove({ output: 'hazennstuff:muramasa' })
    event.shaped('hazennstuff:muramasa', [
        '  M',
        'SM ',
        'BS '
    ], {
        M: 'irons_spellbooks:mithril_ingot',
        S: 'born_in_chaos_v1:monster_skin',
        B: 'create:brass_sheet'
    }).id('kubejs:muramasa')

    // Скипетр призыва душ: механический крафтер, 3 x 5.
    //  G B G
    //  G R G
    //  . R .
    //  G R G
    //  G . G
    // G - золотой лист, R - латунный стержень (Create Crafts & Additions), B - призрачный колокол.
    event.remove({ output: 'hazennstuff:soulcalling_scepter' })
    event.recipes.create.mechanical_crafting('hazennstuff:soulcalling_scepter', [
        'GBG',
        'GRG',
        ' R ',
        'GRG',
        'G G'
    ], {
        G: 'create:golden_sheet',
        R: 'createaddition:brass_rod',
        B: 'create:haunted_bell'
    }).id('kubejs:soulcalling_scepter')

    // Чакрам из шипов: кольцо вокруг панциря аметистового краба.
    //  S V S
    //  M C M
    //  S V S
    // S - бамбуковые шипы (Supplementaries), V - лианы, M - блок мха, C - панцирь аметистового краба (Cataclysm).
    event.remove({ output: 'hazennstuff:thorn_chakram' })
    event.shaped('hazennstuff:thorn_chakram', [
        'SVS',
        'MCM',
        'SVS'
    ], {
        S: 'supplementaries:bamboo_spikes',
        V: 'minecraft:vine',
        M: 'minecraft:moss_block',
        C: 'cataclysm:amethyst_crab_shell'
    }).id('kubejs:thorn_chakram')

    // Молот правосудия: механическая сборка (Create), 5 циклов.
    // Основа - слиток алого неодима; в каждом цикле: пресс x3, заливка светоном (Supplementaries, 250 мБ), золотой блок.
    event.remove({ output: 'hazennstuff:hammer_of_justice' })
    const NEO = 'alexscaves:scarlet_neodymium_ingot'
    const press = { type: 'create:pressing', ingredients: [{ item: NEO }], results: [{ id: NEO }] }
    event.custom({
        type: 'create:sequenced_assembly',
        ingredient: { item: NEO },
        results: [{ id: 'hazennstuff:hammer_of_justice' }],
        loops: 5,
        sequence: [
            press, press, press,
            {
                type: 'create:filling',
                ingredients: [{ item: NEO }, { type: 'neoforge:single', amount: 250, fluid: 'supplementaries:lumisene' }],
                results: [{ id: NEO }]
            },
            {
                type: 'create:deploying',
                ingredients: [{ item: NEO }, { item: 'minecraft:gold_block' }],
                results: [{ id: NEO }]
            }
        ],
        transitional_item: { id: NEO }
    }).id('kubejs:sequenced/hammer_of_justice')

    // Insania Aeternus: механическая сборка (Create), один проход.
    // Основа - усиленный осколок эха; деплоер: осколок потусторонней души, шаблон потусторонного усиления, заряд эха.
    event.remove({ output: 'hazennstuff:insania_aeternus' })
    const SHARD = 'deeperdarker:reinforced_echo_shard'
    const deploy = item => ({
        type: 'create:deploying',
        ingredients: [{ item: SHARD }, { item: item }],
        results: [{ id: SHARD }]
    })
    event.custom({
        type: 'create:sequenced_assembly',
        ingredient: { item: SHARD },
        results: [{ id: 'hazennstuff:insania_aeternus' }],
        loops: 1,
        sequence: [
            deploy('discerning_the_eldritch:eldritch_soul_shard'),
            deploy('discerning_the_eldritch:eldritch_upgrade_template'),
            deploy('archaion:echo_charge')
        ],
        transitional_item: { id: SHARD }
    }).id('kubejs:sequenced/insania_aeternus')

    // Астралитовый секач.
    //  L A .
    //  L A C
    //  L D .
    // L - латунный стержень (Create Crafts & Additions), A - полированный аметист (Northstar), D - тёмный стержень (Born in Chaos),
    // C - руна космоса (Hazentouvelib: руна из Hazen N' Stuff забанена).
    event.remove({ output: 'hazennstuff:astralite_cleaver' })
    event.shaped('hazennstuff:astralite_cleaver', [
        'LA ',
        'LAC',
        'LD '
    ], {
        L: 'createaddition:brass_rod',
        A: 'northstar:polished_amethyst',
        D: 'born_in_chaos_v1:dark_rod',
        C: 'hazentouvelib:cosmic_rune'
    }).id('kubejs:astralite_cleaver')
})
