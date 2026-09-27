// Pharaoh armor (cataclysm_spellbooks) reworked to use the Bone Reptile pieces as ingredients.
ServerEvents.recipes(event => {
    event.remove({ output: 'cataclysm_spellbooks:pharaoh_helmet' })
    event.remove({ output: 'cataclysm_spellbooks:pharaoh_chestplate' })

    let cloth = 'irons_spellbooks:magic_cloth'
    let rune = 'irons_spellbooks:nature_rune'

    // Skull -> Bone Reptile Helmet
    event.shaped('cataclysm_spellbooks:pharaoh_helmet', [
        'csc',
        'crc'
    ], {
        c: cloth,
        r: rune,
        s: 'cataclysm:bone_reptile_helmet'
    })

    // Center cloth -> Bone Reptile Chestplate
    event.shaped('cataclysm_spellbooks:pharaoh_chestplate', [
        'ara',
        'bxb',
        'ccc'
    ], {
        c: cloth,
        r: rune,
        a: 'cataclysm:ancient_metal_ingot',
        b: 'cataclysm:koboleton_bone',
        x: 'cataclysm:bone_reptile_chestplate'
    })
})
