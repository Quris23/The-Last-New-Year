// Броня пугала (irons_spellbooks:pumpkin_*): в рецептах кожа и кожаный шлем убраны, добавлены Незеритовая броня боевого мага
// соответствующей части и Флакон с кровью. Остальное (магическая ткань, сено, тыква) осталось как в оригинале.
ServerEvents.recipes(event => {
  const recipes = {
    helmet: [' C ', 'CNC', 'VPV'],
    chestplate: ['HVH', 'CNC', 'C C'],
    leggings: ['HNH', 'C C', 'V V'],
    boots: ['HNH', 'C C', 'V V']
  }
  Object.keys(recipes).forEach(piece => {
    event.remove({ id: `irons_spellbooks:pumpkin_${piece}` })
    const key = {
      C: 'irons_spellbooks:magic_cloth',
      N: `irons_spellbooks:netherite_mage_${piece}`,
      V: 'irons_spellbooks:blood_vial'
    }
    if (piece === 'helmet') key.P = 'minecraft:carved_pumpkin'
    else key.H = 'minecraft:hay_block'
    event.shaped(`irons_spellbooks:pumpkin_${piece}`, recipes[piece], key).id(`generation:pumpkin_${piece}`)
  })
})
