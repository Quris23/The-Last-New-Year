// FTB Quests custom tasks that check "does the player have an Iron's Spellbooks Scroll whose
// contained spell belongs to school X (and optionally has rarity R at its current level)".
// Scrolls are a single item (irons_spellbooks:scroll) with the actual spell + level stored as a
// data component, so a plain item task can't distinguish "Ice scroll" from "Fire scroll" - only
// real code can read the spell out of the item and check it, hence this custom-task workaround.
// Rarity is NOT fixed per spell - AbstractSpell.getRarity(level) computes it from the spell's
// current level (every spell reaches Legendary at its own max level), so the check below asks
// the spell itself for its rarity at the level actually stored on that scroll, rather than
// comparing against a hardcoded per-spell rarity.
const CustomTaskEvent = Java.loadClass('dev.ftb.mods.ftbquests.events.CustomTaskEvent')
const ISpellContainer = Java.loadClass('io.redspace.ironsspellbooks.api.spells.ISpellContainer')
const BuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
const JLong = Java.loadClass('java.lang.Long')
const EventResult = Java.loadClass('dev.architectury.event.EventResult')

// task hex id (NOT the quest id - the id of the task itself, one level deeper) ->
// { school: "<namespace:path>", rarity: "COMMON"|"UNCOMMON"|"RARE"|"EPIC"|"LEGENDARY"|null }
// rarity: null means any rarity counts (used for the single "any scroll of this school" quests).
const SCROLL_SCHOOL_CHECKS = {
    // Ice Mage
    '365CF225A08970DD': { school: 'irons_spellbooks:ice', rarity: null }, // Q3: any Ice scroll
    // Infernal (Fire)
    '7DCAC4D5F483CB03': { school: 'irons_spellbooks:fire', rarity: null }, // Q3: any Fire scroll
}

function hexId(task) {
    // String(...) forces a real JS string (Java's String.toUpperCase() otherwise stays a host
    // object in Rhino, whose .length isn't the JS string length, breaking the padding loop below).
    let hex = String(JLong.toHexString(task.getId())).toUpperCase()
    while (hex.length < 16) hex = '0' + hex
    return hex
}

// task hex id -> array of item ids; complete when the player has ANY one of them (FTB Quests has
// no native "one item out of a list" item task, so this is the same custom-task workaround).
const ANY_ITEM_CHECKS = {
    '5A0F359B1D9B7D65': [ // Ice Mage: any grimoire
        'irons_spellbooks:copper_spell_book',
        'irons_spellbooks:iron_spell_book',
        'irons_spellbooks:gold_spell_book',
    ],
    '0428702D1AFA1817': [ // Infernal (Fire): any grimoire
        'irons_spellbooks:copper_spell_book',
        'irons_spellbooks:iron_spell_book',
        'irons_spellbooks:gold_spell_book',
    ],
}

function hasAnyItem(player, itemIds) {
    const items = player.getInventory().items
    for (let i = 0; i < items.size(); i++) {
        const stack = items.get(i)
        if (stack.isEmpty()) continue
        const itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
        if (itemIds.includes(itemId)) return true
    }
    return false
}

function hasMatchingScroll(player, config) {
    const items = player.getInventory().items
    for (let i = 0; i < items.size(); i++) {
        const stack = items.get(i)
        if (stack.isEmpty()) continue
        const itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
        if (itemId !== 'irons_spellbooks:scroll') continue
        const container = ISpellContainer.get(stack)
        if (!container) continue
        const slots = container.getAllSpells()
        for (let j = 0; j < slots.length; j++) {
            const spell = slots[j].getSpell()
            if (!spell) continue
            const schoolId = spell.getSchoolType().getId().toString()
            if (schoolId !== config.school) continue
            if (config.rarity) {
                const rarity = spell.getRarity(slots[j].getLevel()).name()
                if (rarity !== config.rarity) continue
            }
            return true
        }
    }
    return false
}

CustomTaskEvent.EVENT.register(event => {
    const task = event.getTask()
    const id = hexId(task)
    console.log('[ftbquests_scroll_checks] CustomTaskEvent fired for task ' + id)

    const scrollConfig = SCROLL_SCHOOL_CHECKS[id]
    if (scrollConfig) {
        console.log('[ftbquests_scroll_checks] wiring scroll check for ' + id + ' -> ' + JSON.stringify(scrollConfig))
        task.setCheck((data, player) => {
            if (data.getProgress() > 0) return
            if (hasMatchingScroll(player, scrollConfig)) {
                data.setProgress(1)
            }
        })
        return EventResult.pass()
    }

    const itemList = ANY_ITEM_CHECKS[id]
    if (itemList) {
        console.log('[ftbquests_scroll_checks] wiring any-item check for ' + id)
        task.setCheck((data, player) => {
            if (data.getProgress() > 0) return
            if (hasAnyItem(player, itemList)) {
                data.setProgress(1)
            }
        })
    }
    return EventResult.pass()
})

// FTB Quests: кастомные задачи "получить любой из нескольких предметов" для главы "Акт 1".
// (тот же приём, что выше: у FTB нет задачи "один из списка").
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
