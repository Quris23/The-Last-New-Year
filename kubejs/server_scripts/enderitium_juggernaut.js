// .minecraft/kubejs/server_scripts/enderitium_juggernaut.js
// Эндеритиумная броня джаггернаута: усиленная броня джаггернаута (Echoing Magic) на кузнечном столе
// с шаблоном «Улучшение аннигилятора» (Legendary Monsters) и сферой улучшения аннигиляции (Legendary Spellbooks).
// Чары и прочие компоненты переносятся с исходного предмета.
ServerEvents.recipes(event => {
    ['helmet', 'chestplate', 'leggings', 'boots'].forEach(piece => {
        event.recipes.minecraft.smithing_transform(
            'witherstormmod:enderitium_juggernaut_' + piece,
            'legendary_monsters:annihilator_upgrade_smithing_template',
            'echoing_magic:reinforced_juggernaut_' + piece,
            'legendary_spellbooks:upgrade_orb_annihilation'
        ).id('kubejs:enderitium_juggernaut_' + piece)
    })
})
