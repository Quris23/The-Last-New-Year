// Замена реагента школы Природы: Ядовитый картофель -> Конопля (Nirvana).
// irons_spellbooks:nature_focus используется и в рецепте Руны Природы (nature_rune.json),
// и внутри самой Кузницы свитков как реагент школы для всех свитков Природы -
// правка одного тега меняет оба места сразу.
ServerEvents.tags('item', event => {
    event.get('irons_spellbooks:nature_focus')
        .remove('minecraft:poisonous_potato')
        .add('nirvana:hemp')
})
