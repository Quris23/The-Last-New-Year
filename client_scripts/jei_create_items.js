// Те же три предмета Create: чтобы находились в поиске JEI/EMI (не только в рецептах).
RecipeViewerEvents.addEntries('item', event => {
    event.add('create:chromatic_compound')
    event.add('create:refined_radiance')
    event.add('create:shadow_steel')
})
