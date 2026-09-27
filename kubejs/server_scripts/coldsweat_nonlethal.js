// Cold Sweat: температурный урон (жара/холод) отнимает не больше 2 сердец (4 HP) от максимума.
// Ниже этого порога температура игрока больше не бьёт. Остальные источники урона не затрагиваются.
const LOSS_LIMIT = 4 // 2 сердца

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
