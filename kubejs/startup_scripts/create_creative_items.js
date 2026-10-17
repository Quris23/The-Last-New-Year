// Хроматическое соединение, Изысканное сияние и Теневая сталь (Create): добавлены во вкладку креатива Create.
// Отдельным файлом, чтобы не трогать остальные скрипты.
StartupEvents.modifyCreativeTab('create:base', event => {
    event.add('create:chromatic_compound')
    event.add('create:refined_radiance')
    event.add('create:shadow_steel')
})
