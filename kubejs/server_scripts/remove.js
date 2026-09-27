//.minecraft/kubejs/server_scripts/remove.js
ServerEvents.recipes(event => {

  event.remove({ output: 'minecraft:end_crystal' })

  event.remove({ mod: 'endrem' })
  event.remove({ output: 'born_in_chaos_v1:bone_heart' })
  event.remove({ output: 'born_in_chaos_v1:dark_atrium' })
  event.remove({ output: 'born_in_chaos_v1:death_totem' })

  event.remove({ output: 'cataclysm:mechanical_fusion_anvil' })
  event.remove({ output: 'cataclysm:meat_shredder' })
  event.remove({ output: 'cataclysm:laser_gatling' })
  event.remove({ output: 'cataclysm:wither_assault_shoulder_weapon' })
  event.remove({ output: 'cataclysm:ancient_spear' })
  event.remove({ output: 'cataclysm:bone_reptile_helmet' })
  event.remove({ output: 'cataclysm:bone_reptile_chestplate' })
  event.remove({ output: 'cataclysm:ceraunus' })
  event.remove({ output: 'cataclysm:astrape' })
  event.remove({ output: 'cataclysm:cursed_bow' })
  event.remove({ output: 'cataclysm:the_annihilator' })
  event.remove({ output: 'cataclysm:soul_render' })
  event.remove({ output: 'cataclysm:cursium_upgrade_smithing_template' })
  event.remove({ output: 'cataclysm:the_incinerator' })
  event.remove({ output: 'cataclysm:bulwark_of_the_flame' })
  event.remove({ output: 'cataclysm:blazing_grips' })
  event.remove({ output: 'cataclysm:ignitium_upgrade_smithing_template' })
  
  event.remove({ output: 'endrem:wither_eye' })
  event.remove({ output: 'endrem:guardian_eye' })
  event.remove({ output: 'cataclysm:mech_eye' })
  event.remove({ output: 'cataclysm:void_eye' })
  event.remove({ output: 'cataclysm:cursed_eye' })
  event.remove({ output: 'cataclysm:desert_eye' })
  event.remove({ output: 'cataclysm:burning_ashes' })
  event.remove({ output: 'cataclysm:flame_eye' })

  // Iron's Spell Books
  event.remove({ output: 'irons_spellbooks:arcane_ingot' })
  event.remove({ output: 'irons_spellbooks:rotten_spell_book' })
  event.remove({ output: 'irons_spellbooks:copper_spell_book' })
  event.remove({ output: 'irons_spellbooks:iron_spell_book' })
  event.remove({ output: 'irons_spellbooks:gold_spell_book' })
  event.remove({ output: 'irons_spellbooks:diamond_spell_book' })
  event.remove({ output: 'irons_spellbooks:netherite_spell_book' })

  event.remove({ output: 'irons_spellbooks:ice_spell_book' })
  event.remove({ output: 'irons_spellbooks:blaze_spell_book' })
  event.remove({ output: 'spellclasses:storm_atlas' })
  event.remove({ output: 'irons_spellbooks:druidic_spell_book' })
  event.remove({ output: 'irons_spellbooks:dragonskin_spell_book' })
  event.remove({ output: 'irons_spellbooks:cursed_doll_spell_book' })
  event.remove({ output: 'irons_spellbooks:villager_spell_book' })

  event.remove({ output: 'irons_spellbooks:portal_frame' })
  //event.remove({ output: 'irons_spellbooks:dragonskin_spell_book' })

  // Cataclysm: Spellbooks - elytra fusions
  event.remove({ output: 'cataclysm_spellbooks:ignis_chestplate_elytra' })
  event.remove({ output: 'cataclysm_spellbooks:cursium_mage_elytra' })

  // Полное удаление: Spirit Sunderer + весь сет Excelsius
  event.remove({ output: 'cataclysm_spellbooks:spirit_sunderer' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_speed_visors' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_speed_chestplate' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_power_visors' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_power_chestplate' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_resist_visors' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_resist_chestplate' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_leggings' })
  event.remove({ output: 'cataclysm_spellbooks:excelsius_greaves' })

  // Полное удаление: Вечная Говядина / Вечный Стейк (Artifacts)
  event.remove({ output: 'artifacts:eternal_steak' })

})
//LootJS.modifiers(event => {
//  event.addLootTypeModifier([LootType.CHEST, LootType.ENTITY])
//    .removeLoot(ItemFilter.mod('endrem'))
//})