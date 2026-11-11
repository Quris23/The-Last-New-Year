// Hazen N' Stuff: мобы и боссы мода заблокированы (не появляются ни от спавна, ни от команд, ни от яиц).
// Сущности заклинаний (снаряды и т.п.) не трогаем: заклинания мода остаются.
const HAZEN_MOBS = [
    'aptos', 'aegis', 'pyromus', 'electromancer', 'bishop_of_deceit', 'servant_of_ender',
    'the_recluse', 'dryad', 'piglin_pyromancer', 'necromancer', 'nameless_one', 'void_wanderer'
]

HAZEN_MOBS.forEach(id => {
    EntityEvents.spawned('hazennstuff:' + id, event => {
        event.cancel()
    })
})
