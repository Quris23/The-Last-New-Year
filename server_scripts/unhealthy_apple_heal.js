// Unhealthy Dying: зачарованное золотое яблоко возвращает 1 сердце потерянного максимума здоровья.
// Выше 10 сердец (20 HP, стандартного значения) не лечит.
const DEFAULT_MAX_HEALTH = 20
const HEAL_PER_APPLE = 2 // 1 сердце

ItemEvents.foodEaten('minecraft:enchanted_golden_apple', event => {
  const player = event.entity
  if (!player.isPlayer()) return

  const missing = DEFAULT_MAX_HEALTH - player.maxHealth
  const restore = Math.floor(Math.min(HEAL_PER_APPLE, missing))
  if (restore < 1) return

  player.server.runCommandSilent('unhealthydying hearts add ' + player.gameProfile.name + ' ' + restore)
})
