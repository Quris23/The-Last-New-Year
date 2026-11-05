// Ремонт ценного оружия и брони на наковальне: один материал чинит предмет полностью, сколько бы прочности ни осталось.
// Список "предмет: материал" — в kubejs/config/anvil_full_repair.json (его же читает witherstormmod, чтобы показать ремонт в JEI).
// Цена в опыте — накопленная цена ремонта предмета + 1, и она не растёт от этого ремонта (не дойдёт до "Слишком дорого").
// Переименовать можно в том же действии. Прочность самим предметам задана в items.js. Правки — после перезапуска игры.
const $AnvilUpdateEvent = Java.loadClass('net.neoforged.neoforge.event.AnvilUpdateEvent')
const $DataComponents = Java.loadClass('net.minecraft.core.component.DataComponents')
const $Component = Java.loadClass('net.minecraft.network.chat.Component')
const $BuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
const $DataComponentPatch = Java.loadClass('net.minecraft.core.component.DataComponentPatch')

const FULL_REPAIR = {}
try {
  let json = JsonIO.read('kubejs/config/anvil_full_repair.json')
  json.forEach((item, material) => { FULL_REPAIR[String(item)] = String(material) })
} catch (e) {
  console.error('anvil_full_repair.json не прочитан: ' + e)
}

const idOf = stack => String($BuiltInRegistries.ITEM.getKey(stack.getItem()))

NativeEvents.onEvent($AnvilUpdateEvent, event => {
  // Ошибка здесь выкидывает игрока с сервера, поэтому всё под try
  try {
    let left = event.getLeft()
    let right = event.getRight()
    if (left.isEmpty() || right.isEmpty() || !left.isDamaged()) return
    let material = FULL_REPAIR[idOf(left)]
    if (!material || idOf(right) != material) return

    let output = left.copy()
    output.setDamageValue(0)
    // У самого ItemStack методы компонентов перегружены (тип / Supplier) и Rhino не может выбрать, поэтому через карту и патч
    let repairCost = left.getComponents().get($DataComponents.REPAIR_COST)
    let cost = (repairCost == null ? 0 : Number(repairCost)) + 1

    let name = event.getName()
    let hasName = output.getComponents().has($DataComponents.CUSTOM_NAME)
    if (name == null || String(name).trim() == '') {
      if (hasName) {
        output.applyComponents($DataComponentPatch.builder().remove($DataComponents.CUSTOM_NAME).build())
        cost++
      }
    } else if (String(name) != String(left.getHoverName().getString())) {
      output.applyComponents($DataComponentPatch.builder().set($DataComponents.CUSTOM_NAME, $Component.literal(String(name))).build())
      cost++
    }

    event.setOutput(output)
    event.setCost(cost)
    event.setMaterialCost(1)
  } catch (e) {
    console.error('anvil_repair: ' + e)
  }
})
