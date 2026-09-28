//.minecraft/kubejs/client_scripts/lang.js
ClientEvents.lang('ru_ru', event => {

    // Born in Chaos
    event.add('fluid_type.kubejs.chaos_fluid', 'Жидкость хаоса')
    event.add('block.kubejs.chaos_fluid', 'Жидкость хаоса')
    event.add('item.kubejs.chaos_fluid_bucket', 'Ведро жидкости хаоса')

    // event.renameItem('born_in_chaos_v1:permafrost_shard', 'Коготь вечной мерзлоты')
    // event.add('item.born_in_chaos_v1.permafrost_shard.description', '§7§oПринадлежит §8§oКрампусу\n§7§oОн является за игроками,\nдостигшими §8§oСотни шаловливости')

    // Endrem
    event.renameItem('endrem:black_eye', '§3Око Абсолютной Тишины')
    event.add('item.endrem.black_eye.description', '§7§oСозданное в тиши самой §8§oГлубинной Тьмы\n§7§oОно поглощает любые звуки и вибрации, засасывая их в §8§oБездну')

    event.renameItem('endrem:lost_eye', '§4Око Истинной Скверны')
    event.add('item.endrem.lost_eye.description', '§7§oСкованное из §8§oГибели§7§o и пропитанное §8§oСмертью\n§7§oОно впитывает мёртвые души и источает §8§oШёпот Забвенья')

    event.renameItem('endrem:corrupted_eye', '§cОко Потерянной Технологии')
    event.add('item.endrem.corrupted_eye.description', '§7§oСпроектированное §8§oДревним Инженером§7§o по §8§oАлгоритму\n§7§oОно стремится к §8§oВечному Двигателю')

    event.renameItem('endrem:old_eye', '§6Око Забытой Древности')
    event.add('item.endrem.old_eye.description', '§7§oОставшееся со времён §8§oВеликой Цивилизации\n§7§oОно видело §8§oЭпоху Распада§7§o и ощущает §8§oБиение Мира')

    event.renameItem('endrem:cryptic_eye', '§bОко Безграничного Знания')
    event.add('item.endrem.cryptic_eye.description', '§7§oХранящее как §8§oВысшую Истину§7§o, так и §8§oБезумие\n§7§oОно — танец §8§oЗлобного Гения')

    event.renameItem('endrem:cursed_eye', '§2Око Непростительного Проклятия')
    event.add('item.endrem.cursed_eye.description', '§7§oСодержащее §8§oНенависть§7§o, источающее §8§oГрех\n§7§oОно лишает прощения и отравляет §8§oДушу')

    event.renameItem('endrem:nether_eye', '§5Око Адского Пламени')
    event.add('item.endrem.nether_eye.description', '§7§oВпервые зажжённое в §8§oГорниле Семи Преисподних\n§7§oОно не смотрит, а сжигает в огне §8§oАпокалипсиса')

    event.renameItem('endrem:undead_eye', '§eОко Слепой Одержимости')
    event.add('item.endrem.undead_eye.description', '§7§oВыкованное из §8§oПавшей Души§7§o клятвопреступника\n§7§oОно — тяжкий §8§oОбет§7§o, лишающий §8§oРассудка')

    event.renameItem('endrem:cold_eye', '§bОко Первого Разлома')
    event.add('item.endrem.cold_eye.description', '§7§oРождённое в §8§oБездне Океана§7§o, где не светит Солнце\n§7§oОно, со дна, дышит §8§oХолодом Тьмы')

    event.renameItem('endrem:wither_eye', '§7Око Вечных Бурь')
    event.add('item.endrem.wither_eye.description', '§7§oЗастывшее в эпицентре §8§oТысячи Гроз§7§o, где разверзается небо\n§7§oОно слышит §8§oГром Богов§7§o и видит весь §8§oМир')

    event.renameItem('endrem:exotic_eye', '§1Око Семи Морей')
    event.add('item.endrem.exotic_eye.description', '§7§oЗалитое §8§oДевытым Валом§7§o Айвазовского, что бушует вечно\n§7§oОно хранит §8§oТайны Семи Морей§7§o и поет о §8§oПлаче Океана')

    event.renameItem('endrem:rogue_eye', '§dОко Фрактального Краха')
    event.add('item.endrem.rogue_eye.description', '§7§oСобранное из тысяч §8§oОсколков Реальности§7§o, рассыпанных во Вселенной\n§7§oОно видит грядущий §8§oРаспад Пространства')
    
    // Iron's Spell

    //event.rename('create_wizard:mana', 'Мана')
    event.add('fluid_type.create_wizard.mana', 'Мана')

    // Legendary monsters

    event.renameItem('kubejs:lunar_enderitium_ore', 'Лунная эндеритиумовая руда')
    event.renameEntity('legendary_monsters:the_obliterator', 'Опустошитель')
    event.renameItem('create_more_additions:electrum_jewel', 'Пластина электрума')
    event.renameItem('legendary_monsters:eye_of_annihilation', 'Око аннигиляции')
    event.renameItem('legendary_monsters:portal_shard', '§dОсколок портала')


    // L_ender_Cataclysm

    event.renameItem('cataclysm:blazing_grips', 'Пылающая перчатка силы')
    event.add('item.cataclysm.blazing_grips.desc', '§7+2 к урону в ближнем бою\n§7Поджигает цель при ударе')

    // Curios slot names
    event.add('curios.identifier.talisman', 'Талисман')

})