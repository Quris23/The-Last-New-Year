// Импланты Chromeware в сундуках данжей с Create и интеграциями Create.
// Нужен мод LootJS; без него скрипт пропускается.
// Чем больше ёмкость (capacity) импланта, тем реже он выпадает: вес = 1000 / capacity.
// Импланты с ёмкостью 0 (кибер-конечности, компрессор) самые редкие: вес как у ёмкости 80.
// Шанс срабатывания задаётся для каждой таблицы лута отдельно (меньше 1 импланта на сундук, ~3-4 на структуру).
if (typeof LootJS !== 'undefined') {
  (function () {
    const IMPLANTS = [
    ['adrenaline_booster', 14],
    ['adrenaline_converter', 6],
    ['adreno_trigger', 20],
    ['atomic_sensors', 5],
    ['axolotl', 48],
    ['bioconductor', 16],
    ['biomonitor', 14],
    ['bionic_joints', 8],
    ['black_mamba', 16],
    ['blood_pump', 15],
    ['camillo_ram_manager', 8],
    ['carapace', 16],
    ['chitin', 50],
    ['chrome_compressor', 0],
    ['clairvoyant', 8],
    ['clutch_padding', 12],
    ['cockatrice', 30],
    ['cogito_lattice', 12],
    ['countershell', 12],
    ['cox_2_cybersomatic_optimizer', 50],
    ['cyber_arm', 0],
    ['cyber_leg', 0],
    ['cyber_torso', 0],
    ['cyberdeck', 16],
    ['deep_field_visual_interface', 40],
    ['dense_marrow', 16],
    ['doomsayer', 2],
    ['electromag_recycler', 40],
    ['epimorphic_skeleton', 40],
    ['ex_disk', 10],
    ['feedback_circuit', 14],
    ['feen_x', 16],
    ['fortified_ankles', 6],
    ['gorilla_arms', 4],
    ['handle_wrap', 8],
    ['heal_on_kill', 10],
    ['immovable_force', 35],
    ['isometric_stabilizer', 40],
    ['jenkins_tendons', 6],
    ['kinetic_frame', 16],
    ['kiroshi_optics', 2],
    ['leeroy_ligament_system', 8],
    ['lynx_paws', 5],
    ['mantis_blades', 4],
    ['mechatronic_core', 5],
    ['memory_boost', 18],
    ['microgenerator', 9],
    ['microrotors', 12],
    ['neofiber', 14],
    ['newton_module', 14],
    ['optical_camo', 20],
    ['pain_editor', 35],
    ['painducer', 30],
    ['para_bellum', 25],
    ['peripheral_inverse', 30],
    ['projectile_launch_system', 4],
    ['proxishield', 10],
    ['quantum_tuner', 45],
    ['ram_reallocator', 40],
    ['ram_recoup', 14],
    ['ram_upgrade', 8],
    ['rangeguard', 14],
    ['rara_avis', 45],
    ['reinforced_tendons', 8],
    ['sandevistan', 44],
    ['scar_coalescer', 20],
    ['scarab', 14],
    ['second_heart', 30],
    ['self_ice', 5],
    ['sentry', 2],
    ['shock_absorber', 12],
    ['shock_n_awe', 25],
    ['spring_joints', 16],
    ['stabber', 12],
    ['stalker', 2],
    ['subdermal_armor', 5],
    ['the_oracle', 10],
    ['threat_evac', 5],
    ['titanium_bones', 6],
    ['tyrosine_injector', 8],
    ['universal_booster', 25],
    ['visual_cortex_support', 20],
    ['zetatech_berserk', 35]
    ]

    // таблица лута -> шанс, что в сундуке окажется один имплант
    const CHANCES = {
        // create_ltab
        'create_ltab:normal/basic_loot': 0.08,
        'create_ltab:normal/basic2_loot': 0.08,
        'create_ltab:normal/basic3_loot': 0.08,
        'create_ltab:normal/basic4_loot': 0.08,
        'create_ltab:normal/rare_loot': 0.25,
        'create_ltab:normal/legend_loot': 0.35,
        'create_ltab:core/basic_loot': 0.10,
        'create_ltab:core/rare_loot': 0.25,
        'create_ltab:core/legend_loot': 0.35,
        'create_ltab:rare_loot': 0.25,
        'create_ltab:desert/basic_loot': 0.10,
        'create_ltab:snow/basic_loot': 0.10,
        'create_ltab:water/basic_loot': 0.10,
        'create_ltab:nether/basic_loot': 0.15,
        // Integrated Cataclysm (Древняя фабрика, Пылающая арена, Ледяная тюрьма, Кузница душ)
        'cataclysm:chests/ancient_factory/ancient_factory': 0.40,
        'cataclysm:chests/burning_arena/burning_arena': 0.35,
        'cataclysm:chests/frosted_prison_treasure': 0.30,
        'cataclysm:chests/soul_black_smith/soul_black_smith': 0.50,
        // Yung's: джунглевый храм и незер-крепость с Create
        'betterjungletemples:chests/treasure': 0.40,
        'betterjungletemples:chests/campsite': 0.15,
        'betterfortresses:chests/keep': 0.10,
        'betterfortresses:chests/storage': 0.10,
        'betterfortresses:chests/hall': 0.10,
        'betterfortresses:chests/extra': 0.06,
        // реактор AlexsCaves Nuclear Convergence
        'alexscaves_nuclear_convergence:chests/abandoned_reactor': 0.40,
        // Northstar: базы на Луне и Марсе
        'northstar:chests/martian_base_chest': 0.15,
        'northstar:chests/martian_base_seed_chest': 0.20,
        'northstar:chests/lunar_base_chest': 0.30
    }

    LootJS.lootTables(event => {
        Object.keys(CHANCES).forEach(id => {
            if (!event.hasLootTable(id)) return
            event.getLootTable(id).createPool(pool => {
                pool.name('chromeware_implants')
                pool.rolls(1)
                pool.getConditions().addCondition(
                    Java.loadClass('net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition')
                        .randomChance(CHANCES[id]).build())
                IMPLANTS.forEach(entry => {
                    let loot = LootEntry.of('chromeware:' + entry[0])
                    loot.setWeight(Math.max(1, Math.round(1000 / (entry[1] > 0 ? entry[1] : 80))))
                    pool.addEntry(loot)
                })
            })
            // Шприцы с иммунодепрессантом: втрое реже имплантов, военный в 4 раза реже обычного
            event.getLootTable(id).createPool(pool => {
                pool.name('chromeware_immunosuppressants')
                pool.rolls(1)
                pool.getConditions().addCondition(
                    Java.loadClass('net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition')
                        .randomChance(CHANCES[id] / 3).build())
                let standard = LootEntry.of('chromeware:standard_grade_immunosuppressant')
                standard.setWeight(4)
                pool.addEntry(standard)
                let military = LootEntry.of('chromeware:military_grade_immunosuppressant')
                military.setWeight(1)
                pool.addEntry(military)
            })
        })
    })
  })()
}
