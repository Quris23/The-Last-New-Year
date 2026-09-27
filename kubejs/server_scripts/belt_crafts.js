// kubejs/server_scripts/belt_crafts.js
ServerEvents.recipes(event => {

  // Латунный пояс — улучшение из Пояса Ученика (cataclysm:belt_of_beginner), +2 слота талисмана
  event.shaped('spellclasses:brass_belt', [
    'BAB',
    'AXA',
    'BAB'
  ], {
    A: 'create:brass_ingot',
    B: 'create:brass_nugget',
    X: 'cataclysm:belt_of_beginner'
  })

  // Пояс Чёрной Стали — улучшение из Латунного пояса, +3 слота талисмана
  event.shaped('spellclasses:black_steel_belt', [
    'BAB',
    'AXA',
    'BAB'
  ], {
    A: 'cataclysm:black_steel_ingot',
    B: 'cataclysm:black_steel_nugget',
    X: 'spellclasses:brass_belt'
  })

})
