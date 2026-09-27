ServerEvents.recipes(event => {
  // Удаляем стандартные ванильные рецепты для глаз End Remastered (если они есть)
  event.remove({ mod: 'endrem' })
})