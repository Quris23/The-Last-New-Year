// Починка тегов, которые ломают целые цепочки.
// В нашем jar Discerning the Eldritch убран echo_greatsword, а его тег minecraft:swords по-прежнему на него ссылается.
// Тег с несуществующим предметом не грузится целиком, а за ним и все minecraft:enchantable/* (острота, прочность, починка...):
// книги переставали накладываться на наковальне почти на всё.
ServerEvents.tags('item', event => {
  event.remove('minecraft:swords', 'discerning_the_eldritch:echo_greatsword')
})
