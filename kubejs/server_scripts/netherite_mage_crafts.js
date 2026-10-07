// .minecraft/kubejs/server_scripts/netherite_mage_crafts.js
// Незеритовая броня боевого мага (netherite_mage_*) больше не делается из Робы волшебника:
// основой служит любая из семи классовых роб (смитинг с незеритовым слитком и шаблоном незеритового улучшения).
// Друид носит Чумной плащ (plagued_*), Эндер - броню тенеходца (shadowwalker_*).
ServerEvents.recipes(event => {
    const classRobes = ['priest', 'cultist', 'plagued', 'electromancer', 'pyromancer', 'cryomancer', 'shadowwalker']
    const pieces = ['helmet', 'chestplate', 'leggings', 'boots']

    pieces.forEach(piece => {
        event.remove({ output: `irons_spellbooks:netherite_mage_${piece}` })
        event.custom({
            type: 'minecraft:smithing_transform',
            template: { item: 'minecraft:netherite_upgrade_smithing_template' },
            base: classRobes.map(robe => ({ item: `irons_spellbooks:${robe}_${piece}` })),
            addition: { tag: 'c:ingots/netherite' },
            result: { id: `irons_spellbooks:netherite_mage_${piece}`, count: 1 }
        })
    })
})
