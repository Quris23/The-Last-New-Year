//.minecraft/kubejs/client_scripts/tooltip.js
ItemEvents.modifyTooltips(event => {
    // Для осколка вечной мерзлоты
    event.add('born_in_chaos_v1:permafrost_shard', [
        Text.of('§7§oПринадлежит §8§oКрампусу'),
        Text.of('§7§oОн является за игроками,'),
        Text.of('§7§oдостигшими §8§oСотни шаловливости')
    ])

    event.add('legendary_monsters:primal_ice_shard', [
        Text.of('§7§oРазрушаясь §8§oОбмороженный Голем'),
        Text.of('§7§oсбрасывает свои холодные осколки'),
    ])

})