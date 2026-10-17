// Подсказка Алтаря Возрождения (reviveraltar:ritual_altar). Отдельный файл, tooltip.js не трогаем.
ItemEvents.modifyTooltips(event => {
    event.add('reviveraltar:ritual_altar', [
        Text.of('§7§oЧтобы вернуть погибшего игрока к жизни, нужны:'),
        Text.of('§b- 1000 mB Маны'),
        Text.of('§c- 1000 mB Крови'),
        Text.of('§6- Голова Умершего §7§o(кладётся на алтарь)')
    ])
})
