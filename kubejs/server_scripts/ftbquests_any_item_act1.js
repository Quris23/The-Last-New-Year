// FTB Quests: кастомные задачи "получить любой из нескольких предметов" для главы "Акт 1".
// Отдельный файл, чтобы не трогать ftbquests_scroll_checks.js (тот же приём: у FTB нет задачи "один из списка").
const CustomTaskEventAct1 = Java.loadClass('dev.ftb.mods.ftbquests.events.CustomTaskEvent')
const BuiltInRegistriesAct1 = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
const JLongAct1 = Java.loadClass('java.lang.Long')
const EventResultAct1 = Java.loadClass('dev.architectury.event.EventResult')

// id задачи (не квеста) -> список предметов, подходит любой
const AEROWORKS_CONTROLS = [
    'aeroworks:control_desk', 'aeroworks:control_stand', 'aeroworks:copycat_control_desk', 'aeroworks:copycat_control_stand',
    'aeroworks:joystick',
    'aeroworks:wheel_module', 'aeroworks:copper_wheel_module',
    'aeroworks:black_wheel_module', 'aeroworks:blue_wheel_module', 'aeroworks:brown_wheel_module', 'aeroworks:cyan_wheel_module',
    'aeroworks:gray_wheel_module', 'aeroworks:green_wheel_module', 'aeroworks:light_blue_wheel_module', 'aeroworks:light_gray_wheel_module',
    'aeroworks:lime_wheel_module', 'aeroworks:magenta_wheel_module', 'aeroworks:orange_wheel_module', 'aeroworks:pink_wheel_module',
    'aeroworks:purple_wheel_module', 'aeroworks:red_wheel_module', 'aeroworks:white_wheel_module', 'aeroworks:yellow_wheel_module',
    'aeroworks:button_module', 'aeroworks:copper_button_module', 'aeroworks:button_keypad_module', 'aeroworks:copper_button_keypad_module',
    'aeroworks:button_panel_module', 'aeroworks:copper_button_panel_module',
    'aeroworks:joystick_module', 'aeroworks:copper_joystick_module', 'aeroworks:lever_module', 'aeroworks:copper_lever_module',
    'aeroworks:pedal_module', 'aeroworks:copper_pedal_module', 'aeroworks:throttle_quadrant_module', 'aeroworks:copper_throttle_quadrant_module',
    'aeroworks:yoke_module', 'aeroworks:copper_yoke_module',
]

const ACT1_ANY_ITEM_CHECKS = {
    '4A7D2C91E5B03F86': AEROWORKS_CONTROLS, // Aeroworks: любой элемент управления
    '2C0A5E71D49B3F68': [ // Левитит или Жемчужный левитит
        'aeronautics:levitite',
        'aeronautics:pearlescent_levitite',
    ],
}

function act1HexId(task) {
    let hex = String(JLongAct1.toHexString(task.getId())).toUpperCase()
    while (hex.length < 16) hex = '0' + hex
    return hex
}

function act1HasAnyItem(player, itemIds) {
    const items = player.getInventory().items
    for (let i = 0; i < items.size(); i++) {
        const stack = items.get(i)
        if (stack.isEmpty()) continue
        if (itemIds.includes(String(BuiltInRegistriesAct1.ITEM.getKey(stack.getItem())))) return true
    }
    return false
}

CustomTaskEventAct1.EVENT.register(event => {
    const task = event.getTask()
    const itemList = ACT1_ANY_ITEM_CHECKS[act1HexId(task)]
    if (itemList) {
        task.setCheck((data, player) => {
            if (data.getProgress() > 0) return
            if (act1HasAnyItem(player, itemList)) data.setProgress(1)
        })
    }
    return EventResultAct1.pass()
})
