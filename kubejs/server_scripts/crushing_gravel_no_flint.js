// Дробильные колёса (Create): Гравий -> Песок + 5% Глиняный шарик (убран шанс 10% на Кремень).
ServerEvents.recipes(event => {
  event.remove({ id: 'create:crushing/gravel' });
  event.recipes.create.crushing([
    'minecraft:sand',
    CreateItem.of('minecraft:clay_ball', 0.05)
  ], 'minecraft:gravel').processingTime(250).id('generation:crushing/gravel_no_flint');
});
