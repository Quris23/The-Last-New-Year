// Codex of Malice smithing upgrade: base book swapped from Ancient Treatise (netherite_spell_book)
// to the Ice-Brand Spellbook (ice_spell_book).
ServerEvents.recipes(event => {
    event.remove({ output: 'cataclysm_spellbooks:codex_of_malice_spell_book' })

    event.recipes.minecraft.smithing_transform(
        'cataclysm_spellbooks:codex_of_malice_spell_book',
        'cataclysm:cursium_upgrade_smithing_template',
        'irons_spellbooks:ice_spell_book',
        'cataclysm:cursium_ingot'
    )
})
