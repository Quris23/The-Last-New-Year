// Cold Sweat: температурный урон (жара/холод) отнимает не больше 7 сердец (14 HP) от максимума,
// т.е. оставляет минимум 3 сердца здоровья. Ниже этого порога температура игрока больше не бьёт.
// Остальные источники урона не затрагиваются.
const LOSS_LIMIT = 14 // 7 сердец (оставляет 3 сердца)

EntityEvents.beforeHurt(event => {
  const entity = event.entity
  if (!entity.isPlayer()) return
  // toString у DamageSource выглядит как "DamageSource (cold_sweat:cold)"
  if (!/cold_sweat:(cold|hot)/.test(String(event.source))) return

  const floor = entity.maxHealth - LOSS_LIMIT
  const allowed = entity.health - floor
  if (allowed <= 0) {
    event.setDamage(0)
  } else if (event.damage > allowed) {
    event.setDamage(allowed)
  }
})
