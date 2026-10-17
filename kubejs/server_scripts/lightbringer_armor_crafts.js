// Броня светоносца (Lightbringer): нагрудник теперь делается из Незеритового нагрудника мага (раньше - из нагрудника жреца),
// шлем, поножи и ботинки (spellclasses:paladin_*) - тем же рецептом из соответствующих частей незеритовой брони мага.
ServerEvents.recipes(event => {
  event.remove({ id: 'irons_spellbooks:paladin_chestplate' })

  const pieces = {
    helmet: 'spellclasses:paladin_helmet',
    chestplate: 'irons_spellbooks:paladin_chestplate',
    leggings: 'spellclasses:paladin_leggings',
    boots: 'spellclasses:paladin_boots'
  }
  Object.keys(pieces).forEach(piece => {
    event.shaped(pieces[piece], [
      'GDG',
      'WAW',
      'GWG'
    ], {
      G: '#c:ingots/gold',
      D: 'irons_spellbooks:divine_soulshard',
      W: 'irons_spellbooks:mithril_weave',
      A: `irons_spellbooks:netherite_mage_${piece}`
    }).id(`generation:paladin_${piece}`)
  })
})
