// Правки атрибутов существующих предметов (Cataclysm: Spellbooks / Iron's Spellbooks).
// ItemEvents.modification - STARTUP-событие (в отличие от server_scripts/ServerEvents.recipes,
// см. крафты в server_scripts/spell_crafts.js и spellbooks_crafts.js) - грузится только при
// полном перезапуске игры, /kubejs reload не подхватит.
//
// item.setAttributeModifiers(list) с готовым списком объектов оказался ненадёжным - структура
// ItemAttributeModifiers.Entry неоднозначна между Codec-полем "type" и record-полем "attribute"
// (плюс AttributeModifier то ли инлайнится, то ли вкладывается) - конвертер молча возвращал
// пустой/битый список без ошибки в логе. Вместо этого: очищаем список (пустой массив - тривиален
// для конвертации) и добавляем модификаторы ПОШТУЧНО через addAttributeModifier(attribute, mod,
// slot) - так KubeJS сам строит Entry из уже проверенных типов, а не из сырого списка.
//
// Ванильные атрибуты в 1.21.1 всё ещё называются с префиксом "generic." (minecraft:generic.
// attack_damage), а НЕ просто minecraft:attack_damage.
function setMods(item, mods) {
    item.setAttributeModifiers([])
    mods.forEach(m => item.addAttributeModifier(m.attribute, { id: m.id, amount: m.amount, operation: m.operation }, m.slot))
}

ItemEvents.modification(event => {
    // Fake Wadjet's Staff - убраны баффы Природы и Света, добавлен бафф Природы 25%
    // (у школы Песка нет отдельного атрибута силы - она считается через тот же
    // irons_spellbooks:nature_spell_power, что и Природа, см. CSSchoolRegistry.SAND)
    event.modify('cataclysm_spellbooks:fake_wudjets_staff', item => {
        setMods(item, [
            { attribute: 'minecraft:generic.attack_damage', id: 'minecraft:base_attack_damage', amount: 3.0, operation: 'add_value', slot: 'mainhand' },
            { attribute: 'minecraft:generic.attack_speed', id: 'minecraft:base_attack_speed', amount: -3.0, operation: 'add_value', slot: 'mainhand' },
            { attribute: 'irons_spellbooks:cooldown_reduction', id: 'irons_spellbooks:mainhand_cooldown_reduction_modifier', amount: 0.25, operation: 'add_multiplied_base', slot: 'mainhand' },
            { attribute: 'irons_spellbooks:nature_spell_power', id: 'irons_spellbooks:mainhand_nature_spell_power_modifier', amount: 0.25, operation: 'add_multiplied_base', slot: 'mainhand' }
        ])
    })

    // Bloom Stone Staff - бафф Природы поднят с 15% до 25%
    event.modify('cataclysm_spellbooks:bloom_stone_staff', item => {
        setMods(item, [
            { attribute: 'minecraft:generic.attack_damage', id: 'minecraft:base_attack_damage', amount: 3.0, operation: 'add_value', slot: 'mainhand' },
            { attribute: 'minecraft:generic.attack_speed', id: 'minecraft:base_attack_speed', amount: -3.0, operation: 'add_value', slot: 'mainhand' },
            { attribute: 'irons_spellbooks:cooldown_reduction', id: 'irons_spellbooks:mainhand_cooldown_reduction_modifier', amount: 0.15, operation: 'add_multiplied_base', slot: 'mainhand' },
            { attribute: 'irons_spellbooks:nature_spell_power', id: 'irons_spellbooks:mainhand_nature_spell_power_modifier', amount: 0.25, operation: 'add_multiplied_base', slot: 'mainhand' }
        ])
    })

    // Трость Изобретателя и Броня Инженера - перенесены на чистую Java (StaffAttributeOverrides.java
    // в SpellClasses), т.к. этот KubeJS-путь (setAttributeModifiers/addAttributeModifier) ломает
    // отображение тултипа именно на предметах со smithing_transform-крафтом - см. фикс для Друида,
    // который по той же причине тоже сделан через Java, а не здесь.

    // Desert Spellbook (Гримуар Песка) - убран бафф Света, Природа/Мана без изменений
    event.modify('cataclysm_spellbooks:desert_spell_book', item => {
        setMods(item, [
            { attribute: 'irons_spellbooks:max_mana', id: 'cataclysm_spellbooks:mainhand_max_mana_modifier', amount: 300.0, operation: 'add_value', slot: 'mainhand' },
            { attribute: 'irons_spellbooks:nature_spell_power', id: 'cataclysm_spellbooks:mainhand_nature_spell_power_modifier', amount: 0.3, operation: 'add_multiplied_base', slot: 'mainhand' }
        ])
    })

    // Полное Одеяние Фараона (Pharaoh) - убран бафф Света на всех 4 частях, Природа/Мана/Общая сила без изменений
    // (базовые значения защиты/прочности/КБ из CSArmorMaterialRegistry.warlockArmorMap()/CURSIUM_WARLOCK_ARMOR: 4/9/7/4, toughness 3.0, kb 0.1)
    const pharaohCommon = [
        { attribute: 'irons_spellbooks:max_mana', amount: 150.0, operation: 'add_value' },
        { attribute: 'irons_spellbooks:nature_spell_power', amount: 0.2, operation: 'add_multiplied_base' },
        { attribute: 'irons_spellbooks:spell_power', amount: 0.05, operation: 'add_multiplied_base' }
    ]
    const pharaohPieces = [
        { id: 'cataclysm_spellbooks:pharaoh_helmet', defense: 4, slot: 'head' },
        { id: 'cataclysm_spellbooks:pharaoh_chestplate', defense: 9, slot: 'chest' },
        { id: 'cataclysm_spellbooks:pharaoh_leggings', defense: 7, slot: 'legs' },
        { id: 'cataclysm_spellbooks:pharaoh_greaves', defense: 4, slot: 'feet' }
    ]
    pharaohPieces.forEach(piece => {
        event.modify(piece.id, item => {
            const mods = [
                { attribute: 'minecraft:generic.armor', id: 'minecraft:armor.' + piece.slot, amount: piece.defense, operation: 'add_value', slot: piece.slot },
                { attribute: 'minecraft:generic.armor_toughness', id: 'minecraft:armor.' + piece.slot, amount: 3.0, operation: 'add_value', slot: piece.slot },
                { attribute: 'minecraft:generic.knockback_resistance', id: 'minecraft:armor.' + piece.slot, amount: 0.1, operation: 'add_value', slot: piece.slot }
            ]
            pharaohCommon.forEach(c => mods.push({
                attribute: c.attribute,
                id: 'cataclysm_spellbooks:' + piece.slot + '_' + c.attribute.split(':')[1] + '_modifier',
                amount: c.amount,
                operation: c.operation,
                slot: piece.slot
            }))
            setMods(item, mods)
        })
    })

})
