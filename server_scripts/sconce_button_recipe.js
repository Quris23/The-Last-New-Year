// Бра-кнопка (SpellClasses): рецепт Бра-рычага (Бра над Редстоуном) + Каменная кнопка снизу
ServerEvents.recipes(event => {
  event.shaped('spellclasses:sconce_button', [
    ' A ',
    ' B ',
    ' C '
  ], {
    A: 'supplementaries:sconce',
    B: 'minecraft:redstone',
    C: 'minecraft:stone_button'
  }).id('generation:sconce_button');
});
