#!/usr/bin/env python3
"""Stone implant for TaCZ: the Create Armorer double-barrelled shotgun (shotgun_db_stone) slung under the forearm.

The hull is the same as the Crane implant's (gen_crane.py, whose helpers are used here), turned over: its top doors stay
shut, the bottom ones open and the shotgun drops out of the forearm with its barrels folded back, then the barrels are
thrown forward. For a reload the barrels break forward and down, as on the original. No grip and no stock.

usage: gen_stone.py <stone_geo.json> <out_dir> <stone_texture.rgba raw 256x256> <stone data json> <stone display json>
"""
import copy
import json
import os
import sys

import gen_crane as gc

NS = "lny_gun_implants"
NAME = "stone"

T = 0.27                     # hull plate thickness (just clear of the arm's second skin layer)
W = 2.27                     # hull half width
INSET = 1.0                  # rim between the hull edge and a door hinge
GUN_TOP = 9.97               # top of the shotgun (its receiver), which lies against the hull's underside
BOT = GUN_TOP + T            # underside of the forearm
TOP = BOT + 4.0              # its outer (upper) face
HAND_END_Z = -2.0            # knuckles: the barrels reach 6 px past them
BRACER_Z = [-1.65, 7.35]
SHOULDER = [0.5, 11.0, 15.0]
EYE = [-6.5, 10.8, 10.0]  # idle camera: left of the arm, a little above it
IRON = [-3.5, 10.3, 7.5]     # aiming: beside the hull, level with the barrels
STOW_Y = 3.75                # the shotgun lifted into the forearm
DOOR_REST = 100.0            # bottom doors when the shotgun is out (bind pose)
FOLD = 178.0                 # barrels folded back while stowed


class Atlas(gc.Atlas):
    def __init__(self, raw_path, w=256, h=256):
        d = open(raw_path, "rb").read()
        used_x = max(x for y in range(h) for x in range(w) if d[(y * w + x) * 4 + 3] > 0)
        used_y = max(y for y in range(h) for x in range(w) if d[(y * w + x) * 4 + 3] > 0)
        self.bins = [[0, used_y + 2, w, h], [used_x + 2, 0, w, used_y + 1]]
        self.pending = []
        self.paint = []


def main():
    src, out, raw, data_json, display_json = sys.argv[1:6]
    gc.ATLAS = Atlas(raw)
    uv = gc.swatch_uv
    geo = json.load(open(src, encoding="utf-8"))["minecraft:geometry"][0]
    stone = {b["name"]: b for b in geo["bones"]}
    bones = []

    def bone(name, parent, pivot3, rotation=None, cubes=None):
        b = {"name": name}
        if parent:
            b["parent"] = parent
        b["pivot"] = gc.bed(pivot3)
        if rotation:
            b["rotation"] = rotation
        if cubes:
            b["cubes"] = cubes
        bones.append(b)

    bone("root", None, [0.0, 9.0, 6.0])
    bone("gun", "root", [0, 0, 0])
    bone("right", "gun", SHOULDER)
    O, R, local = gc.place_arm(True, [0, 0, -1], [0, 1, 0], [-3, 10, 0], [0.0, TOP, HAND_END_Z])
    full = {f: {"uv": [0, 0], "uv_size": [4, 12]} for f in ("north", "east", "south", "west")} | {"up": {"uv": [4, 4], "uv_size": [-4, -4]}, "down": {"uv": [4, 4], "uv_size": [-4, -4]}}
    bone("righthand_pos", "right", O, rotation=gc.bed_rot(R), cubes=[gc.cube([O[0] - 8, O[1], O[2] - 2], [4, 12, 4], full)])
    bone("implant", "right", [0.0, TOP, 0.0])

    # ---------------- hull (see gen_crane.py); here the bottom doors are the working pair
    z0, z1 = BRACER_Z
    L = z1 - z0
    hx = W - INSET
    wall = W - 2.0
    H = 4.0 + 2 * T
    bc = []

    def box(mat, o3, s3):
        bc.append(gc.cube(o3, s3, uv(mat, s3)))

    for sx in (-1, 1):
        box(gc.SW_BRASS, [2.0 if sx > 0 else -W, BOT - T, z0], [wall, H, L])
        box(gc.SW_BRASS, [hx if sx > 0 else -2.0, TOP, z0], [2.0 - hx, T, L])
        box(gc.SW_BRASS, [hx if sx > 0 else -2.0, BOT - T, z0], [2.0 - hx, T, L])
    box(gc.SW_BAY, [-hx, BOT - 0.12, z0], [2 * hx, 0.12, L])                      # bay ceiling, seen through the open doors
    for zc in (z0 - 0.35, z1):
        for sx in (-1, 1):
            box(gc.SW_BRASS_DARK, [hx if sx > 0 else -W, BOT - T, zc], [W - hx, T, 0.35])
            box(gc.SW_BRASS_DARK, [2.0 if sx > 0 else -W, BOT, zc], [wall, 4.0, 0.35])
        box(gc.SW_BRASS_DARK, [-W, TOP, zc], [2 * W, T, 0.35])
    box(gc.SW_BRASS_DARK, [-2.0, BOT, z0 - 1.1], [4.0, 4.0, 0.75])                # front plate
    for zz in (z0 + 1.2, z0 + L * 0.55):
        for sx in (-1, 1):
            box(gc.SW_COPPER, [hx - 0.01 if sx > 0 else -W - 0.06, BOT - T - 0.06, zz], [W - hx + 0.07, H + 0.12, 0.45])
    bone("bracer", "implant", [0, TOP, 0], cubes=bc)
    bone("status_light_illuminated", "bracer", [-W, TOP - 0.9, z0 + 2.6],
         cubes=[gc.cube([-W - 0.1, TOP - 0.9, z0 + 2.6], [0.25, 0.25, 0.25], uv(gc.SW_LIGHT, [0.25, 0.25, 0.25]))])
    dw = hx - 0.02
    for side, sfx in ((-1, "l"), (1, "r")):
        x_min = -hx if side < 0 else 0.02
        ribs = [z0 + 0.9 + k * (L - 1.8) / 2 for k in range(3)]
        dt = [gc.cube([x_min, TOP, z0 + 0.15], [dw, T, L - 0.3], uv(gc.SW_BRASS, [dw, T, L - 0.3]))]
        dt += [gc.cube([x_min + 0.2, TOP + T, zz], [dw - 0.4, 0.18, 0.5], uv(gc.SW_BRASS_DARK, [dw - 0.4, 0.18, 0.5])) for zz in ribs]
        bone("door_top_" + sfx, "bracer", [side * hx, TOP + T, 0], cubes=dt)
        db = [gc.cube([x_min, BOT - T, z0 + 0.15], [dw, T, L - 0.3], uv(gc.SW_BRASS, [dw, T, L - 0.3]))]
        db += [gc.cube([x_min + 0.2, BOT - T - 0.18, zz], [dw - 0.4, 0.18, 0.5], uv(gc.SW_BRASS_DARK, [dw - 0.4, 0.18, 0.5])) for zz in ribs]
        db.append(gc.cube([(-0.45 if side < 0 else 0.1), BOT - 0.02, z0 + 0.5], [0.35, 0.16, L - 1.0], uv(gc.SW_COPPER, [0.35, 0.16, L - 1.0])))
        # bind rotation = open (TaCZ shows the bind pose after "draw" and in third person)
        bone("door_" + sfx, "bracer", [side * hx, BOT - T, 0], rotation=[0, 0, side * DOOR_REST], cubes=db)

    # ---------------- the shotgun: barrels ("front") hinge on the receiver ("body"); the grip ("cover") is left out
    bone("rifle", "implant", [0.0, 9.0, 3.0])
    for nm in ("front", "front_main", "bullet_1", "bullet_2", "body", "metal", "deco", "body_main", "button"):
        b = copy.deepcopy(stone[nm])
        if b.get("parent") == "gun":
            b["parent"] = "rifle"
        bones.append(b)
    # hangers that hold the receiver under the hull
    hang = [gc.cube([-0.5, GUN_TOP - 0.4, zz], [1.0, T + 0.5, 0.8], uv(gc.SW_IRON_DARK, [1.0, T + 0.5, 0.8])) for zz in (2.2, 6.4)]
    bone("hanger", "rifle", [0, GUN_TOP, 4.0], cubes=hang)
    bone("muzzle_flash", "rifle", [0, 8.875, -12])
    bone("constraint", "rifle", IRON)

    bone("left", "root", [0, 0, 0])
    fl = {f: {"uv": [4, 0], "uv_size": [-4, 12]} for f in ("north", "east", "south", "west")} | {"up": {"uv": [0, 4], "uv_size": [4, -4]}, "down": {"uv": [0, 4], "uv_size": [4, -4]}}
    bone("lefthand_pos", "left", [0, 0, 0], cubes=[gc.cube([4, 0, -2], [4, 12, 4], fl)])

    bone("views", None, [0, 0, 0])
    bone("iron_view", "views", IRON)
    bone("idle_view", "views", EYE)
    bone("refit_view", "views", [-25, 9, 0], rotation=[0, 90, 0])
    bone("positioning", None, [0, 0, 0])
    # third person: the hand's item anchor is the end of the real arm, on its front face; the model's arm goes there
    bone("thirdperson_hand", "positioning", [0, TOP, HAND_END_Z])
    bone("ground", "positioning", [0, 7.0, 2.0])
    bone("fixed", "positioning", [0, 10.5, 2.5], rotation=[0, 90, 0])
    bone("camera", None, EYE)

    gc.ATLAS.finalize()
    A = os.path.join(out, "assets", NS)
    D = os.path.join(out, "data", NS)
    os.makedirs(os.path.join(A, "geo_models", "gun"), exist_ok=True)
    json.dump({"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": f"geometry.{NS}.{NAME}", "texture_width": 64, "texture_height": 64,
                        "visible_bounds_width": 6, "visible_bounds_height": 4, "visible_bounds_offset": [0, 0, 0]},
        "bones": bones}]}, open(os.path.join(A, "geo_models", "gun", NAME + "_geo.json"), "w", encoding="utf-8"), indent=1)
    json.dump({"palette": gc.PALETTE, "rects": gc.ATLAS.paint}, open(os.path.join(out, "paint_stone.json"), "w", encoding="utf-8"), indent=1)
    animations(A)
    pack(A, D, data_json, display_json)
    print("stone bones", len(bones))


def animations(A):
    kf = gc.kf
    S = gc.ARM_SCALE
    rest = gc.left_pose([-14, -6, 14], [0.45, 0.2, -1], [0.3, 1, 0])
    An = {}
    An["static_idle"] = {"loop": True, "bones": {
        "righthand_pos": {"scale": S}, "lefthand_pos": {"scale": S},
        "left": {"position": rest[0], "rotation": rest[1]}}}
    shut = DOOR_REST  # door_l value when closed, relative to the open bind pose (door_r mirrored)
    up = [0, STOW_Y, 0]

    An["draw"] = {"animation_length": 0.9, "loop": "hold_on_last_frame", "bones": {
        "right": {"rotation": kf([(0.0, [55, 16, -8]), (0.12, [34, 10, -5]), (0.26, [-1.5, -0.5, 0.5]), (0.34, [0.5, 0, 0]), (0.42, [0, 0, 0]),
                                  (0.66, [0, 0, 0]), (0.72, [2.2, 0, 0.3]), (0.9, [0, 0, 0])]),
                  "position": kf([(0.0, [-2, -6, 3]), (0.14, [-1, -2.5, 1.2]), (0.28, [0, 0.2, -0.1]), (0.4, [0, 0, 0]),
                                  (0.68, [0, 0, 0]), (0.73, [0, 0, -0.45]), (0.9, [0, 0, 0])])},
        "door_l": {"rotation": kf([(0.0, [0, 0, shut]), (0.2, [0, 0, shut]), (0.32, [0, 0, -8]), (0.4, [0, 0, 0])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, -shut]), (0.2, [0, 0, -shut]), (0.32, [0, 0, 8]), (0.4, [0, 0, 0])])},
        "rifle": {"position": kf([(0.0, up), (0.3, up), (0.44, [0, -0.25, 0]), (0.5, [0, 0, 0])])},
        "front": {"rotation": kf([(0.0, [FOLD, 0, 0]), (0.46, [FOLD, 0, 0]), (0.58, [70, 0, 0]), (0.68, [-7, 0, 0]), (0.74, [1.5, 0, 0]), (0.8, [0, 0, 0])])},
        "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.3, [-0.5, 0.3, 0]), (0.44, [0.3, 0, 0]), (0.68, [0.9, 0, 0.2]), (0.8, [0, 0, 0])])},
    }}
    An["put_away"] = {"animation_length": 0.6, "loop": "hold_on_last_frame", "bones": {
        "front": {"rotation": kf([(0.0, [0, 0, 0]), (0.05, [0, 0, 0]), (0.22, [FOLD, 0, 0])])},
        "rifle": {"position": kf([(0.0, [0, 0, 0]), (0.2, [0, 0, 0]), (0.32, up)])},
        "door_l": {"rotation": kf([(0.0, [0, 0, 0]), (0.28, [0, 0, 0]), (0.4, [0, 0, shut - 5]), (0.44, [0, 0, shut])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, 0]), (0.28, [0, 0, 0]), (0.4, [0, 0, -shut + 5]), (0.44, [0, 0, -shut])])},
        "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.38, [0, 0, 0]), (0.5, [28, 8, -4]), (0.6, [55, 16, -8])]),
                  "position": kf([(0.0, [0, 0, 0]), (0.38, [0, 0, 0]), (0.5, [-1, -2.5, 1.2]), (0.6, [-2, -6, 3])])},
    }}
    An["shoot"] = {"animation_length": 0.5, "bones": {
        "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.05, [-4.5, 0.6, 0.8], "linear"), (0.5, [0, 0, 0])]),
                  "position": kf([(0.0, [0, 0, 0]), (0.05, [0, 0.3, 1.3], "linear"), (0.5, [0, 0, 0])])},
        "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.06, [-1.2, 0.3, 0]), (0.3, [0, 0, 0])])},
    }}

    # reload: the barrels break forward and down, the spent shells drop out, the left hand brings two new ones
    under = gc.left_pose([-0.4, 5.6, 6.2], [0.3, 0.75, -0.6], [0.0, 1, 0.3])
    feed = gc.left_pose([-0.3, 7.4, 4.4], [0.25, 0.7, -0.67], [0.0, 1, 0.3])
    low = gc.left_pose([-7, -2, 9], [0.5, 0.4, -0.8], [0.3, 1, 0])

    def lkeys(seq):
        return {"position": kf([(t, p[0]) for t, p in seq]), "rotation": kf([(t, p[1]) for t, p in seq])}

    gone = [0, -14, 6]
    for nm in ("reload_tactical", "reload_empty"):
        shells = {}
        for b, late in (("bullet_1", 0.0), ("bullet_2", 0.06)):
            shells[b] = {"position": kf([(0.0, [0, 0, 0]), (0.36 + late, [0, 0, 0]), (0.5 + late, [0, 0.4, 3.4]), (0.72 + late, gone, "linear"),
                                         (1.0, gone, "linear"), (1.02, [0, -3.5, 7.5], "linear"), (1.22 + late, [0, 0.4, 3.4]), (1.34 + late, [0, 0, 0])])}
        An[nm] = {"animation_length": 2.0, "loop": "hold_on_last_frame", "bones": {
            "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.22, [-6, 3, -10]), (1.55, [-6, 3, -10]), (1.64, [-3, 2, -8]), (1.85, [0, 0, 0])]),
                      "position": kf([(0.0, [0, 0, 0]), (0.22, [0.3, 1.2, -0.4]), (1.55, [0.3, 1.2, -0.4]), (1.85, [0, 0, 0])])},
            "front": {"rotation": kf([(0.0, [0, 0, 0]), (0.16, [0, 0, 0]), (0.3, [40, 0, 0]), (0.36, [36, 0, 0]), (1.48, [36, 0, 0]),
                                      (1.56, [-3, 0, 0], "linear"), (1.62, [0, 0, 0])])},
            "left": lkeys([(0.0, rest), (0.6, rest), (0.9, low), (1.02, under), (1.22, feed), (1.36, feed), (1.5, under), (1.8, rest), (2.0, rest)]),
            "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.3, [0.8, 0.3, -0.4]), (0.7, [0.3, 0, 0]), (1.3, [0.5, 0, 0.2]), (1.57, [-1.0, 0, 0]), (1.8, [0, 0, 0])])},
        } | shells}
    An["inspect"] = {"animation_length": 2.6, "loop": "hold_on_last_frame", "bones": {
        "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.45, [-14, -20, -48]), (1.3, [-14, -22, -50]), (2.2, [-14, -22, -50]), (2.6, [0, 0, 0])]),
                  "position": kf([(0.0, [0, 0, 0]), (0.45, [1.5, 3.5, -2]), (2.2, [1.5, 3.5, -2]), (2.6, [0, 0, 0])])},
        "front": {"rotation": kf([(0.0, [0, 0, 0]), (1.3, [0, 0, 0]), (1.45, [38, 0, 0]), (1.9, [38, 0, 0]), (2.0, [0, 0, 0])])},
        "door_l": {"rotation": kf([(0.0, [0, 0, 0]), (0.7, [0, 0, 0]), (0.85, [0, 0, 18]), (1.0, [0, 0, 0])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, 0]), (0.7, [0, 0, 0]), (0.85, [0, 0, -18]), (1.0, [0, 0, 0])])},
    }}
    An["inspect_empty"] = copy.deepcopy(An["inspect"])
    os.makedirs(os.path.join(A, "animations"), exist_ok=True)
    json.dump({"format_version": "1.8.0", "animations": An}, open(os.path.join(A, "animations", NAME + ".animation.json"), "w", encoding="utf-8"), indent=1)


def pack(A, D, data_json, display_json):
    def dump(path, obj):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        json.dump(obj, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)

    d = json.load(open(display_json, encoding="utf-8"))
    d["model"] = NS + ":gun/" + NAME + "_geo"
    d["texture"] = NS + ":gun/uv/" + NAME
    d["animation"] = NS + ":" + NAME
    d["transform"] = {"scale": {"thirdperson": [1, 1, 1], "ground": [0.6, 0.6, 0.6], "fixed": [1.2, 1.2, 1.2]}}
    d.pop("lod", None)   # the far model is the hand-held shotgun with its grip
    dump(os.path.join(A, "display", "guns", NAME + "_display.json"), d)
    g = json.load(open(data_json, encoding="utf-8"))
    g["draw_time"] = 0.9
    g["put_away_time"] = 0.6
    g["reload"]["feed"] = {"empty": 1.36, "tactical": 1.36}
    g["reload"]["cooldown"] = {"empty": 1.9, "tactical": 1.9}
    dump(os.path.join(D, "data", "guns", NAME + "_data.json"), g)
    dump(os.path.join(D, "index", "guns", NAME + ".json"),
         {"name": NS + ".gun." + NAME, "display": NS + ":" + NAME + "_display", "data": NS + ":" + NAME + "_data", "type": "shotgun", "sort": 352})


if __name__ == "__main__":
    main()
