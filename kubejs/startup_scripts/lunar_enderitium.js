// Лунная Эндеритиумовая Руда: местный вариант эндеритиумовой руды Legendary Monsters
// (та же зелёная жила), но на текстуре лунного камня, добывается на Луне.
StartupEvents.registry('block', event => {
  event.create('lunar_enderitium_ore')
    .texture('kubejs:block/lunar_enderitium_ore')
    .hardness(2.5)
    .resistance(2.5)
    .stoneSoundType()
    .requiresTool()
    .tagBlock('minecraft:mineable/pickaxe')
    .noDrops() // лут-таблица своя, см. kubejs/data/kubejs/loot_table/blocks/lunar_enderitium_ore.json
})
