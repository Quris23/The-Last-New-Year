#!/usr/bin/env python3
"""3D models for TaCZ ammunition that only has a flat slot icon: an open ammo box with the slot icon as its label and a
couple of cartridges beside it, in the manner of the default pack's boxes (tacz:ammo/338 and the like).

For every entry of AMMO it writes into <out>:
  assets/lny_ammo/geo_models/ammo/<name>.json     the model (Bedrock geometry, per-face uv, 64x64 units = 256 px)
  paint/<name>.json                               what TexPaint paints into assets/lny_ammo/textures/ammo/uv/<name>.png
  assets/<ns>/display/ammo/<name>_display.json    the ammo's display with "model" and "texture" added

usage: gen_ammo.py <out dir> <default pack assets/tacz dir> <create armorer assets/create_armorer dir>
"""
import json
import math
import os
import re
import sys

K = 4          # texels per model px
TEX = 256


class Atlas:
    def __init__(self):
        self.pending = []
        self.paint = []

    def face(self, mat, a, b, image=None):
        f = {}
        self.pending.append((f, max(1, math.ceil(a * K - 1e-6)), max(1, math.ceil(b * K - 1e-6)), mat, image))
        return f

    def box(self, mat, size, images=None):
        sx, sy, sz = size
        images = images or {}
        dims = {"north": (sx, sy), "south": (sx, sy), "east": (sz, sy), "west": (sz, sy), "up": (sx, sz), "down": (sx, sz)}
        mats = mat if isinstance(mat, dict) else {k: mat for k in dims}
        return {k: self.face(mats[k], a, b, images.get(k)) for k, (a, b) in dims.items()}

    def finalize(self):
        shelves = []  # [x cursor, y, height]
        for f, tw, th, mat, image in sorted(self.pending, key=lambda it: (-it[2], -it[1])):
            placed = None
            for sh in shelves:
                if th <= sh[2] and sh[0] + tw <= TEX:
                    placed = (sh[0], sh[1])
                    sh[0] += tw + 1
                    break
            if placed is None:
                y = max([sh[1] + sh[2] + 1 for sh in shelves], default=0)
                if y + th > TEX:
                    raise SystemExit("texture atlas full")
                shelves.append([tw + 1, y, th])
                placed = (0, y)
            x, y = placed
            f["uv"] = [x / K, y / K]
            f["uv_size"] = [tw / K, th / K]
            entry = {"rect": [x, y, tw, th], "mat": mat}
            if image:
                entry["image"] = image
            self.paint.append(entry)


def r5(v):
    v = round(v, 5)
    return 0.0 if v == 0 else v


def cube(atlas, o, s, mat, images=None):
    return {"origin": [r5(x) for x in o], "size": [r5(x) for x in s], "uv": atlas.box(mat, s, images)}


def prism(atlas, base, side, height, mat):
    """square prism standing on `base` (centre of its bottom face)"""
    return cube(atlas, [base[0] - side / 2, base[1], base[2] - side / 2], [side, height, side], mat)


def cartridge(atlas, base, parts):
    """parts: list of (side, height, material), stacked bottom to top"""
    cubes = []
    y = base[1]
    for side, height, mat in parts:
        cubes.append(prism(atlas, [base[0], y, base[2]], side, height, mat))
        y += height
    return cubes, y - base[1]


# cartridge shapes: (side, height, material) from the rim up
def shape(kind, case, bullet, tip):
    if kind == "rifle":
        return [(1.15, 0.2, "case_dark"), (1.0, 4.6, case), (0.75, 0.7, case), (0.6, 1.5, bullet), (0.35, 0.7, tip)]
    if kind == "rifle_long":
        return [(1.25, 0.2, "case_dark"), (1.1, 5.4, case), (0.8, 0.8, case), (0.65, 1.7, bullet), (0.4, 0.8, tip)]
    if kind == "pistol":
        return [(1.1, 0.2, "case_dark"), (1.0, 2.5, case), (0.9, 0.8, bullet), (0.55, 0.5, tip)]
    if kind == "magnum":
        return [(1.5, 0.25, "case_dark"), (1.3, 3.8, case), (1.2, 1.0, bullet), (0.8, 0.6, tip)]
    if kind == "straight":
        return [(1.45, 0.25, "case_dark"), (1.2, 5.2, case), (1.1, 1.1, bullet), (0.75, 0.6, tip)]
    if kind == "rimfire":
        return [(0.8, 0.15, "case_dark"), (0.6, 3.2, case), (0.55, 0.8, bullet), (0.35, 0.4, tip)]
    if kind == "grenade":
        return [(3.2, 0.3, "case_dark"), (3.0, 3.0, case), (3.15, 0.4, "band"), (2.8, 1.5, bullet), (2.0, 0.9, bullet), (1.1, 0.6, tip)]
    if kind == "hand_grenade":
        return [(2.6, 0.4, "case_dark"), (3.0, 3.0, case), (2.4, 0.6, case), (1.2, 0.8, bullet), (1.6, 0.3, tip)]
    if kind == "rocket":
        return [(1.6, 0.4, "case_dark"), (0.9, 6.5, case), (1.4, 1.6, "band"), (2.6, 3.6, bullet), (1.8, 1.6, bullet), (0.9, 1.4, tip)]
    if kind == "blade":
        return [(0.5, 2.4, "case_dark"), (1.8, 0.35, "band"), (0.8, 6.0, bullet), (0.45, 0.9, tip)]
    raise SystemExit(kind)


METALS = {
    "brass": {"fill": "d9a441", "border": "8a5e23", "hi": "f7d57a"},
    "brass_dark": {"fill": "9c6c2a", "border": "5f3f16", "hi": "c08a3a"},
    "copper": {"fill": "c97a50", "border": "7e4426", "hi": "eda57c"},
    "lead": {"fill": "9aa0ae", "border": "5b606c", "hi": "c9cedb"},
    "steel": {"fill": "6f7480", "border": "3f434c", "hi": "9da2ad"},
    "steel_dark": {"fill": "3a3d44", "border": "1f2126", "hi": "565a63"},
    "lacquer": {"fill": "3c4a34", "border": "20281b", "hi": "566a4b"},
    "white": {"fill": "d8dde6", "border": "8b909b", "hi": "ffffff"},
    "olive": {"fill": "4c5a2e", "border": "2a3318", "hi": "6b7d43"},
    "red_tip": {"fill": "c2332f", "border": "7a1c1a", "hi": "ea6b62"},
}

# name: (namespace, box size [w, h, d], box colour, cartridge kind, case, bullet, tip, band)
AMMO = {
    "22wmr": ("tacz", [4.5, 8.5, 10], "2f6b2a", "rimfire", "copper", "copper", "copper", None),
    "45_70": ("tacz", [6, 9, 10], "8c2327", "straight", "copper", "lead", "lead", None),
    "500mag": ("tacz", [6, 8.5, 10], "7a1f2a", "magnum", "copper", "copper", "copper", None),
    "545x39": ("tacz", [6, 9, 10], "a3811c", "rifle", "lacquer", "copper", "copper", None),
    "792x57": ("tacz", [6, 10, 10], "157573", "rifle_long", "brass", "copper", "copper", None),
    "rpg_rocket": ("tacz", [5, 8.5, 12], "4a1570", "rocket", "steel", "olive", "steel_dark", "steel_dark"),
    "40mmhe": ("create_armorer", [7, 8.5, 10], "6b4a2f", "grenade", "brass", "steel", "steel_dark", "copper"),
    "gas_pistol_ammo": ("create_armorer", [5, 8.5, 10], "6b4a2f", "pistol", "brass", "copper", "copper", None),
    "gernade": ("create_armorer", [7, 8.5, 10], "6b4a2f", "hand_grenade", "brass", "steel", "lead", None),
    "melee_weapon": ("create_armorer", [5, 8.5, 12], "6b4a2f", "blade", "steel_dark", "lead", "white", "brass"),
    "rbapb": ("create_armorer", [6, 8.5, 10], "6b4a2f", "magnum", "brass", "copper", "lead", None),
    "slap": ("create_armorer", [6, 9, 10], "6b4a2f", "rifle", "brass", "copper", "white", None),
}


def shade(hexcol, f):
    r, g, b = (int(hexcol[i:i + 2], 16) for i in (0, 2, 4))
    return "".join(f"{max(0, min(255, round(c * f))):02x}" for c in (r, g, b))


def build(name, spec, out, slot_png):
    ns, (w, h, d), colour, kind, case, bullet, tip, band = spec
    atlas = Atlas()
    palette = dict(METALS)
    palette["box"] = {"fill": colour, "border": shade(colour, 0.55), "hi": shade(colour, 1.3)}
    palette["box_side"] = {"fill": shade(colour, 0.85), "border": shade(colour, 0.5), "hi": shade(colour, 1.15)}
    palette["box_in"] = {"fill": shade(colour, 0.3), "border": shade(colour, 0.2), "hi": shade(colour, 0.38)}
    palette["lid"] = {"fill": shade(colour, 1.12), "border": shade(colour, 0.6), "hi": shade(colour, 1.4)}
    palette["case"] = METALS[case]
    palette["case_dark"] = METALS["brass_dark"] if case in ("brass", "copper") else METALS["steel_dark"]
    palette["bullet"] = METALS[bullet]
    palette["tip"] = METALS[tip]
    palette["band"] = METALS[band or "copper"]

    bones = [{"name": "root", "pivot": [0, 0, 0], "rotation": [0, 90, 0]}]
    # the box: label (the slot icon) on both big faces, dark inside seen through the open top
    label = {"east": slot_png, "west": slot_png}
    mats = {"north": "box_side", "south": "box_side", "east": "box", "west": "box", "up": "box_in", "down": "box_side"}
    bones.append({"name": "box", "parent": "root", "pivot": [0, h / 2, 0], "rotation": [0, -10, 0],
                  "cubes": [cube(atlas, [-w / 2, 0, -d / 2], [w, h, d], mats, label)]})
    # the tops of the rounds packed inside, showing in the open top
    top_side = min(1.1, max(p[0] for p in shape(kind, "case", "bullet", "tip")[-2:]))
    if kind in ("rifle", "rifle_long", "pistol", "magnum", "straight", "rimfire"):
        tops = []
        nz = int((d - 1.6) // (top_side + 0.5))
        nx = int((w - 1.2) // (top_side + 0.5))
        for i in range(nx):
            for j in range(nz):
                cx = -(nx - 1) * (top_side + 0.5) / 2 + i * (top_side + 0.5)
                cz = -(nz - 1) * (top_side + 0.5) / 2 + j * (top_side + 0.5)
                tops.append(prism(atlas, [cx, h - 0.02, cz], top_side, 0.35, "bullet"))
        bones.append({"name": "rounds_inside", "parent": "box", "pivot": [0, h, 0], "cubes": tops})
    # the lid, thrown open over one long edge, with its lip
    lid = [cube(atlas, [-w / 2, h, -d / 2], [w, 0.3, d], {k: "lid" for k in mats} | {"down": "box_in"}),
           cube(atlas, [w / 2 - 0.3, h - 1.0, -d / 2], [0.3, 1.0, d], "lid")]
    bones.append({"name": "lid", "parent": "box", "pivot": [-w / 2, h, 0], "rotation": [0, 0, -105], "cubes": lid})
    # cartridges: one standing by the box, one lying in front of it
    parts = shape(kind, "case", "bullet", "tip")
    sx = w / 2 + 1.6
    stand, length = cartridge(atlas, [0, 0, 0], parts)
    for c in stand:
        c["origin"] = [r5(c["origin"][0] + sx), c["origin"][1], r5(c["origin"][2] + d / 2 - 1.2)]
    bones.append({"name": "round_standing", "parent": "root", "pivot": [sx, 0, d / 2 - 1.2], "rotation": [0, 22.5, 0], "cubes": stand})
    lying, _ = cartridge(atlas, [0, 0, 0], parts)
    half = max(p[0] for p in parts) / 2
    lx, lz = w / 2 + 1.3, -d / 2 + 0.6
    for c in lying:
        c["origin"] = [r5(c["origin"][0] + lx), r5(c["origin"][1] + half), r5(c["origin"][2] + lz)]
    # tipped over along +z (bedrock x rotation), resting on its side
    bones.append({"name": "round_lying", "parent": "root", "pivot": [lx, half, lz], "rotation": [90, 0, 15], "cubes": lying})

    bones += [{"name": "positioning", "pivot": [0, 0, 0]},
              {"name": "ground", "parent": "positioning", "pivot": [0, 0, 0]},
              {"name": "fixed", "parent": "positioning", "pivot": [0, h / 2, 1.5], "rotation": [0, -10, 0]},
              {"name": "thirdperson_hand", "parent": "positioning", "pivot": [0, 0, 2.2]}]
    atlas.finalize()
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.lny_ammo." + name, "texture_width": 64, "texture_height": 64,
                        "visible_bounds_width": 3, "visible_bounds_height": 2.5, "visible_bounds_offset": [0, 0.75, 0]},
        "bones": bones}]}
    gdir = os.path.join(out, "assets", "lny_ammo", "geo_models", "ammo")
    os.makedirs(gdir, exist_ok=True)
    json.dump(geo, open(os.path.join(gdir, name + ".json"), "w", encoding="utf-8"), indent=1)
    pdir = os.path.join(out, "paint")
    os.makedirs(pdir, exist_ok=True)
    json.dump({"size": TEX, "palette": palette, "rects": atlas.paint}, open(os.path.join(pdir, name + ".json"), "w", encoding="utf-8"), indent=1)


def display(name, ns, src_dir, out):
    text = open(os.path.join(src_dir, "display", "ammo", name + "_display.json"), encoding="utf-8").read()
    text = re.sub(r"^\s*//.*$", "", text, flags=re.M)
    d = json.loads(text)
    d = {"model": "lny_ammo:ammo/" + name, "texture": "lny_ammo:ammo/uv/" + name} | {k: v for k, v in d.items() if k not in ("model", "texture")}
    ddir = os.path.join(out, "assets", ns, "display", "ammo")
    os.makedirs(ddir, exist_ok=True)
    json.dump(d, open(os.path.join(ddir, name + "_display.json"), "w", encoding="utf-8"), indent=2)


def main():
    out, tacz, armorer = sys.argv[1], sys.argv[2], sys.argv[3]
    for name, spec in AMMO.items():
        src = tacz if spec[0] == "tacz" else armorer
        slot = os.path.join(src, "textures", "ammo", "slot", name + ".png")
        build(name, spec, out, slot)
        display(name, spec[0], src, out)
    print("ammo models", len(AMMO))


if __name__ == "__main__":
    main()
