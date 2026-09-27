// Cataclysm: Spellbooks 1.1.11 ships recipes and assets for the Engineer's armor set, but its
// crafting ingredient "cataclysm_spellbooks:technomancy_rune" was never actually registered as an
// item in this build (log: "Unknown registry key ... cataclysm_spellbooks:technomancy_rune"),
// so the game silently drops all 4 armor recipes at startup. The armor pieces themselves ARE
// valid registered items - only the rune ingredient is missing - so replace the broken vanilla
// recipes with equivalent ones that swap the rune for Create's mechanical parts directly.
// ("engineers_power_glove" has the same problem but the item itself isn't registered either, so
// it can't be crafted or /given at all - nothing to do about that one until the addon is patched.)
ServerEvents.recipes(event => {
    let cloth = 'irons_spellbooks:magic_cloth'
    let core = 'create:precision_mechanism'

    event.shaped('cataclysm_spellbooks:engineer_hood', [
        'ccc',
        'crc'
    ], { c: cloth, r: core })

    event.shaped('cataclysm_spellbooks:engineer_suit', [
        'crc',
        'ccc',
        'ccc'
    ], { c: cloth, r: core })

    event.shaped('cataclysm_spellbooks:engineer_leggings', [
        'ccc',
        'crc',
        'c c'
    ], { c: cloth, r: core })

    event.shaped('cataclysm_spellbooks:engineer_boots', [
        'c c',
        'crc'
    ], { c: cloth, r: core })
})
