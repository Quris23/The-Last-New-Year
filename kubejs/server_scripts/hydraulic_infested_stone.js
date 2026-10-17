// Гидравлический пресс (DnDesires) обрабатывает Зараженный камень так же, как обычный Пресс
// (повторяет create_enchantment_industry:compacting/infested_stone: 4 зараженных камня -> каменные кирпичи + самородок опыта).
ServerEvents.recipes(event => {
  event.custom({
    type: 'dndesires:hydraulic_compacting',
    ingredients: [
      { item: 'minecraft:infested_stone' },
      { item: 'minecraft:infested_stone' },
      { item: 'minecraft:infested_stone' },
      { item: 'minecraft:infested_stone' }
    ],
    results: [
      { id: 'minecraft:stone_bricks' },
      { id: 'create:experience_nugget' }
    ]
  }).id('generation:hydraulic_compacting/infested_stone');
});
