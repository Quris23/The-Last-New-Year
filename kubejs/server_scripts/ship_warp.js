ServerEvents.recipes(event => {

  // Эндерняк - Булыжник, обдутый Вентилятором через Огонь Душ (create:haunting)
  event.recipes.createHaunting('minecraft:end_stone', 'minecraft:cobblestone')

  // Запрет Рамки портала (Rift Gate Frame) и Рамки портала с интерфейсом (Rift Gate)
  event.remove({ output: 'aerowarptics:rift_gate_frame' })
  event.remove({ output: 'aerowarptics:rift_gate' })

  // Портальная жидкость (Rift Essence) - Леветит + 4 Хоруса в Смешивателе при супернагреве
  event.recipes.createMixing(Fluid.of('aerowarptics:rift_essence', 1000), [
      ['aeronautics:levitite', 'aeronautics:pearlescent_levitite'],
      '4x minecraft:chorus_fruit'
  ]).superheated()

})
