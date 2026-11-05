// kubejs/server_scripts/infested_argillite.js
// Заражённый чёрный аргилит: без шёлкового касания на месте блока появляется чешуйница (блок при этом ничего не роняет)
BlockEvents.broken('kubejs:infested_black_argillite', event => {
    let { player, block, level } = event
    if (player.isCreative() || player.mainHandItem.hasEnchantment('minecraft:silk_touch', 1)) return
    let silverfish = level.createEntity('minecraft:silverfish')
    silverfish.moveTo(block.x + 0.5, block.y, block.z + 0.5, 0, 0)
    silverfish.spawn()
    silverfish.spawnAnim()
})
