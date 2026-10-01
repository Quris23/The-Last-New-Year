ServerEvents.recipes(event => {

  // Эндерняк - Булыжник, обдутый Вентилятором через Огонь Душ (create:haunting)
  event.recipes.createHaunting('minecraft:end_stone', 'minecraft:cobblestone')

  // Запрет Рамки портала (Rift Gate Frame) и Рамки портала с интерфейсом (Rift Gate)
  event.remove({ output: 'aerowarptics:rift_gate_frame' })
  event.remove({ output: 'aerowarptics:rift_gate' })

})
