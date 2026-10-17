ServerEvents.recipes(event => {

  // Портальная жидкость (Rift Essence) - Леветит + 4 Хоруса в Смешивателе при супернагреве
  event.recipes.createMixing(Fluid.of('aerowarptics:rift_essence', 1000), [
      ['aeronautics:levitite', 'aeronautics:pearlescent_levitite'],
      '4x minecraft:chorus_fruit'
  ]).superheated()

})
