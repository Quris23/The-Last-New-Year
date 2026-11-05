// Око Эшлорда: ищет Драконью башню (Block Factory's Bosses), где живёт адский дракон Эшлорд.
// Это предмет-глаз Cataclysm (DungeonEyeItem): ПКМ ищет ближайшую структуру из тега в радиусе 100 чанков
// и выпускает летящий глаз с цветным следом; сам глаз не тратится, перезарядка 3 секунды.
// Тег структур: kubejs/data/kubejs/tags/worldgen/structure/eye_of_ashlord_located.json
// Текстура: own_models/tools/ashlord_eye_gen.py, название: kubejs/assets/kubejs/lang
const $DungeonEyeItem = Java.loadClass('com.github.L_Ender.cataclysm.items.DungeonEyeItem')
const $ItemProperties = Java.loadClass('net.minecraft.world.item.Item$Properties')
const $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
const $Registries = Java.loadClass('net.minecraft.core.registries.Registries')
const $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

StartupEvents.registry('item', event => {
  event.createCustom('ashlord_eye', () => new $DungeonEyeItem(
    new $ItemProperties().stacksTo(1).fireResistant(),
    $TagKey.create($Registries.STRUCTURE, $ResourceLocation.parse('kubejs:eye_of_ashlord_located')),
    255, 98, 20
  ))
})
