// Дробильные колёса (Create): Базальт -> 1 Соль (Northstar), всегда (вместо 2 + шансы 75%/25%).
ServerEvents.recipes(event => {
  event.remove({ id: 'northstar:crushing/basalt' });
  event.recipes.create.crushing('northstar:salt', 'minecraft:basalt').processingTime(1000).id('generation:crushing/basalt_salt');
});
