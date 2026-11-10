import json, os, shutil, sys
# usage: pack.py <out dir of gen_crane> <profile kubejs dir> <crane data json (pack override)>
out, kjs, crane_data = sys.argv[1], sys.argv[2], sys.argv[3]
NS = "lny_gun_implants"
A = os.path.join(out, "assets", NS)
D = os.path.join(out, "data", NS)

def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)

snd = "create_armorer:rifle_assult_crane/rifle_assult_crane_"
dump(os.path.join(A, "display", "guns", "crane_display.json"), {
    "model": NS + ":gun/crane_geo", "texture": NS + ":gun/uv/crane",
    "lod": {"model": "create_armorer:gun/lod/rifle_assult_crane_lod", "texture": "create_armorer:gun/lod/rifle_assult_crane_lod"},
    "hud": "create_armorer:gun/hud/rifle_assult_crane", "slot": "create_armorer:gun/slot/rifle_assult_crane",
    "animation": NS + ":crane", "use_default_animation": "rifle", "third_person_animation": "default",
    "transform": {"scale": {"thirdperson": [1, 1, 1], "ground": [0.6, 0.6, 0.6], "fixed": [1.2, 1.2, 1.2]}},
    "muzzle_flash": {"texture": "tacz:flash/common_muzzle_flash", "scale": 1},
    "shell": {"initial_velocity": [5, 2, 1], "random_velocity": [1, 1, 0.25], "acceleration": [0.0, -10, 0.0],
              "angular_velocity": [360, -1200, 90], "living_time": 1.0},
    "sounds": {"shoot": snd + "shoot", "shoot_3p": snd + "shoot_3p", "reload_empty": snd + "reload",
               "reload_tactical": snd + "reload", "inspect": snd + "inspect", "inspect_empty": snd + "inspect",
               "draw": snd + "draw", "dry_fire": "create_armorer:misc/dry_fire", "put_away": snd + "put_away",
               "silence": snd + "silence", "silence_3p": snd + "silence_3p"}})

d = json.load(open(crane_data, encoding="utf-8"))
d["draw_time"] = 1.0
d["put_away_time"] = 0.7
d["reload"]["feed"] = {"empty": 1.92, "tactical": 1.92}
d["reload"]["cooldown"] = {"empty": 2.5, "tactical": 2.45}
d["allow_attachment_types"] = ["scope", "muzzle", "extended_mag"]
dump(os.path.join(D, "data", "guns", "crane_data.json"), d)
dump(os.path.join(D, "index", "guns", "crane.json"), {
    "name": NS + ".gun.crane", "display": NS + ":crane_display", "data": NS + ":crane_data", "type": "rifle", "sort": 351})
dump(os.path.join(D, "tacz_tags", "attachments", "allow_attachments", "crane.json"),
     ["#tacz:muzzle", "#create_armorer:create_armorer_extended_mag", "create_armorer:muzzle_refit_lava_perfusion_bottle", "#tacz:scope"])
STONE = {"ru_ru": "Имплант: дробовик «Камень»", "rpr": "Имплантъ: дробовикъ «Камень»", "en_us": "Implant: \"Stone\" Shotgun"}
for lang, name in (("ru_ru", "Имплант: штурмовая винтовка «Кран»"), ("rpr", "Имплантъ: штурмовая винтовка «Кранъ»"),
                   ("en_us", "Implant: \"Crane\" Assault Rifle")):
    dump(os.path.join(A, "lang", lang + ".json"), {NS + ".gun.crane": name, NS + ".gun.stone": STONE[lang]})

for kind, src in (("assets", A), ("data", D)):
    dst = os.path.join(kjs, kind, NS)
    shutil.copytree(src, dst, dirs_exist_ok=True)
    n = sum(len(f) for _, _, f in os.walk(dst))
    print("installed", dst, n, "files")
