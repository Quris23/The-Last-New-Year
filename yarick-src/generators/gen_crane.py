#!/usr/bin/env python3
"""Crane implant for TaCZ: model + animations built from the Create Armorer Crane (rifle_assult_crane).

Coordinates: design math is done in "three" space (Blockbench viewport: x right, y up, z toward the viewer).
Bedrock files store x mirrored: bedrock = (-x, y, z). Bone/animation rotations: three Euler ZYX (ex, ey, ez)
<-> bedrock (-ex, -ey, ez) degrees (TaCZ BedrockModel/Animations apply them as Rz*Ry*Rx in its Y-down space).

TaCZ draws the vanilla arm at righthand_pos/lefthand_pos: in the locator's three-space frame the arm box is
PlayerModel space unchanged: shoulder pivot (-5,2,0) (left (5,2,0)), bob zRot +0.1 (left -0.1), box
x[-3,1] (left [-1,3]) y[-2,10] z[-2,2], scaled by the locator's scale. Camera = idle_view pivot, looking -z.

usage: gen_crane.py <crane_geo.json> <out_dir> <crane_texture.rgba raw 256x256>
"""
import copy
import json
import math
import os
import sys

NS = "lny_gun_implants"
NAME = "crane"

# ------------------------------------------------------------------ math (three space)


def mm(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def mv(a, v):
    return [sum(a[i][k] * v[k] for k in range(3)) for i in range(3)]


def tr(a):
    return [[a[j][i] for j in range(3)] for i in range(3)]


def rx(d):
    c, s = math.cos(math.radians(d)), math.sin(math.radians(d))
    return [[1, 0, 0], [0, c, -s], [0, s, c]]


def ry(d):
    c, s = math.cos(math.radians(d)), math.sin(math.radians(d))
    return [[c, 0, s], [0, 1, 0], [-s, 0, c]]


def rz(d):
    c, s = math.cos(math.radians(d)), math.sin(math.radians(d))
    return [[c, -s, 0], [s, c, 0], [0, 0, 1]]


def add(a, b):
    return [a[i] + b[i] for i in range(3)]


def sub(a, b):
    return [a[i] - b[i] for i in range(3)]


def mul(a, s):
    return [x * s for x in a]


def dot(a, b):
    return sum(a[i] * b[i] for i in range(3))


def cross(a, b):
    return [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]


def norm(a):
    l = math.sqrt(dot(a, a))
    return [x / l for x in a]


def cols(c0, c1, c2):
    return [[c0[i], c1[i], c2[i]] for i in range(3)]


def euler_zyx(R):
    """R = Rz(ez) Ry(ey) Rx(ex) -> (ex, ey, ez) degrees"""
    sy = max(-1.0, min(1.0, -R[2][0]))
    ey = math.asin(sy)
    if abs(math.cos(ey)) > 1e-6:
        ex = math.atan2(R[2][1], R[2][2])
        ez = math.atan2(R[1][0], R[0][0])
    else:
        ex = math.atan2(-R[1][2], R[1][1])
        ez = 0.0
    return [math.degrees(ex), math.degrees(ey), math.degrees(ez)]


def rot_three(e):
    """three Euler ZYX (degrees) -> matrix"""
    return mm(rz(e[2]), mm(ry(e[1]), rx(e[0])))


def bed_rot(R):
    ex, ey, ez = euler_zyx(R)
    return [r5(-ex), r5(-ey), r5(ez)]


def bed(p):
    return [r5(-p[0]), r5(p[1]), r5(p[2])]


def r5(x):
    v = round(x, 5)
    return 0.0 if v == 0 else v


# ------------------------------------------------------------------ layout (three space, px)
# The rifle keeps the Crane's own coordinates and camera, so ADS/HUD/attachments line up as on the Crane.
EYE = [-3.0, 9.5, 10.0]          # idle_view (Crane: bedrock [3, 9.5, 10])
IRON = [0.0, 6.875, 7.0]         # iron_view (Crane: bedrock [0, 6.875, 7])
ARM_SCALE = [1.0, 1.5, 1.0]      # TaCZ arms are stretched like this on every gun
ARM_TOP = 3.35                   # y of the forearm's top (outer) face
ARM_X = 0.0                      # x of the forearm's centre line (under the rifle)
HAND_END_Z = -5.75               # z of the knuckles (end face of the arm)
SHOULDER = [0.5, 0.0, 13.0]      # pivot of the "right" bone (arm + implant swing around it)

BRACER_Z = [-5.4, 3.6]           # bracer length along the forearm
BRACER_W = 2.27                  # half width (arm is 2)
BRACER_T = 0.27                  # thickness of the top doors
STOW = [0.0, -4.9, 3.6]          # rifle offset inside the arm when folded
LIFT_Z = (-3.4, 0.9)             # z of the two lift rods under the rifle
DOOR_INSET = 1.0                 # rim left between the hull edge and the door hinge
TP_SHIFT = [0.0, 0.15, 8.25]     # third person (bind pose): the hull sits back on the real forearm, the rifle stays where the hand-held Crane is
DOOR_REST = 118.0  # deployed door angle (bind pose); closed = 0
RELOAD_LIFT = 2.6                # the rifle rises on the lift for the reload so its rear can drop


# ------------------------------------------------------------------ arm solver
def arm_frame(right, axis, up, scale):
    """Rotation of a hand locator so the (bobbed, scaled) arm points along `axis` with its outer face
    (right arm) / inner face (left arm) towards `up`. Returns (R, local(q) function)."""
    b = 0.1 if right else -0.1
    S = scale
    pivot = [-5.0, 2.0, 0.0] if right else [5.0, 2.0, 0.0]

    def bob(v):
        c, s = math.cos(b), math.sin(b)
        return [c * v[0] - s * v[1], s * v[0] + c * v[1], v[2]]

    def local(q):  # box point (relative to the shoulder pivot) -> locator frame
        p = add(pivot, bob(q))
        return [p[0] * S[0], p[1] * S[1], p[2] * S[2]]

    a_l = norm(sub(local([0, 10, 0]), local([0, -2, 0])))
    side = -1.0 if right else -1.0   # right: outer face = -x ; left: inner (palm) face = -x
    n_l = norm(sub(local([side * 3 if right else -1, 4, 0]), local([1 if right else 3, 4, 0])))
    m_l = norm(cross(n_l, a_l))
    n_l = norm(cross(a_l, m_l))
    up = norm(sub(up, mul(axis, dot(up, axis))))
    W = cols(up, axis, norm(cross(up, axis)))
    L = cols(n_l, a_l, m_l)
    return mm(W, tr(L)), local


def place_arm(right, axis, up, anchor_box, anchor_world, scale=ARM_SCALE):
    R, local = arm_frame(right, axis, up, scale)
    O = sub(anchor_world, mv(R, local(anchor_box)))
    return O, R, local


# ------------------------------------------------------------------ geo helpers
def cube(o3, s3, uv, inflate=0.0, pivot=None, rotation=None):
    """o3/s3 in three space (min corner + size); stored mirrored"""
    c = {"origin": [r5(-(o3[0] + s3[0])), r5(o3[1]), r5(o3[2])], "size": [r5(s3[0]), r5(s3[1]), r5(s3[2])], "uv": uv}
    if inflate:
        c["inflate"] = inflate
    if rotation:
        c["pivot"] = bed(pivot)
        c["rotation"] = rotation
    return c


class Atlas:
    """Per-face UV packer into the free area of the Crane texture (256 px = 64 uv units, 4 texels per unit).
    Every face gets its own rectangle (1 texel per 1/4 px), painted later by TexPaint with its material.
    Faces are collected first and shelf-packed at the end (tallest first) into the two free bins."""
    K = 4  # texels per uv unit

    def __init__(self, raw_path, w=256, h=256):
        d = open(raw_path, "rb").read()
        used_x = max((x for y in range(h) for x in range(w) if d[(y * w + x) * 4 + 3] > 0), default=0)
        assert used_x < 164, "Crane texture layout changed"
        # free bins (texels): below the used area, and right of it
        self.bins = [[0, 165, 256, 256], [165, 0, 256, 164]]
        self.pending = []
        self.paint = []

    def face(self, mat, a, b):
        tw = max(1, int(math.ceil(a * self.K - 1e-6)))
        th = max(1, int(math.ceil(b * self.K - 1e-6)))
        f = {}
        self.pending.append((f, tw, th, mat))
        return f

    def box(self, mat, size3):
        sx, sy, sz = size3
        return {"north": self.face(mat, sx, sy), "south": self.face(mat, sx, sy), "east": self.face(mat, sz, sy),
                "west": self.face(mat, sz, sy), "up": self.face(mat, sx, sz), "down": self.face(mat, sx, sz)}

    def finalize(self):
        items = sorted(self.pending, key=lambda it: (-it[2], -it[1]))
        shelves = []  # [bin index, x cursor, y, height]
        for f, tw, th, mat in items:
            placed = None
            for sh in shelves:
                bx0, by0, bx1, by1 = self.bins[sh[0]]
                if th <= sh[3] and sh[1] + tw <= bx1:
                    placed = (sh[1], sh[2])
                    sh[1] += tw + 1
                    break
            if placed is None:
                for bi, (bx0, by0, bx1, by1) in enumerate(self.bins):
                    y = max([sh[2] + sh[3] + 1 for sh in shelves if sh[0] == bi], default=by0)
                    if y + th <= by1 and bx0 + tw <= bx1:
                        shelves.append([bi, bx0 + tw + 1, y, th])
                        placed = (bx0, y)
                        break
            if placed is None:
                raise SystemExit("texture atlas full")
            x, y = placed
            f["uv"] = [r5(x / self.K), r5(y / self.K)]
            f["uv_size"] = [r5(tw / self.K), r5(th / self.K)]
            self.paint.append({"rect": [x, y, tw, th], "mat": mat})


ATLAS = None


def swatch_uv(mat, size3):
    return ATLAS.box(mat, size3)


# materials (TexPaint palette): fill, border, highlight, rivets
SW_BRASS = "hull"
SW_BRASS_DARK = "hull_dark"
SW_IRON = "steel"
SW_IRON_DARK = "steel_dark"
SW_COPPER = "brass"
SW_BAY = "bay"
SW_LIGHT = "light"
PALETTE = {
    "hull": {"fill": "3e3348", "border": "231e2b", "hi": "574a66", "rivets": "2a2433"},
    "hull_dark": {"fill": "2f2737", "border": "1c1823", "hi": "3e3348"},
    "steel": {"fill": "7a7f8c", "border": "4a4e58", "hi": "a4a9b4"},
    "steel_dark": {"fill": "4a4e58", "border": "2e3138", "hi": "62666f"},
    "brass": {"fill": "dca55b", "border": "806035", "hi": "fbd178"},
    "bay": {"fill": "1a1720", "border": "120f16", "hi": "221e29"},
    "light": {"fill": "8cff5a", "border": "4fc93a", "hi": "d4ffc0"},
}


def main():
    global ATLAS
    src, out, raw = sys.argv[1], sys.argv[2], sys.argv[3]
    ATLAS = Atlas(raw)
    g = json.load(open(src, encoding="utf-8"))
    geo = g["minecraft:geometry"][0]
    crane = {b["name"]: b for b in geo["bones"]}
    bones = []

    def bone(name, parent, pivot3, rotation=None, cubes=None, **extra):
        b = {"name": name}
        if parent:
            b["parent"] = parent
        b["pivot"] = bed(pivot3)
        if rotation:
            b["rotation"] = rotation
        if cubes:
            b["cubes"] = cubes
        b.update(extra)
        bones.append(b)
        return b

    # ---------------- skeleton
    bone("root", None, [0.0, 3.5, 2.5])
    bone("gun", "root", [0, 0, 0])
    bone("right", "gun", SHOULDER)

    # right arm: along -z, outer face (back of the forearm) up, knuckles at HAND_END_Z
    O, R, local = place_arm(True, [0, 0, -1], [0, 1, 0], [-3, 10, 0], [ARM_X, ARM_TOP, HAND_END_Z])
    ref = cube([O[0] - 8, O[1], O[2] - 2], [4, 12, 4], {f: {"uv": [0, 0], "uv_size": [4, 12]} for f in ("north", "east", "south", "west")} | {"up": {"uv": [4, 4], "uv_size": [-4, -4]}, "down": {"uv": [4, 4], "uv_size": [-4, -4]}})
    bone("righthand_pos", "right", O, rotation=bed_rot(R), cubes=[ref])
    # report where the arm really is
    for nm, q in (("knuckles top", [-3, 10, 0]), ("elbow top", [-3, 4, 0]), ("shoulder top", [-3, -2, 0]), ("palm end", [1, 10, 0])):
        print("arm", nm, [round(x, 2) for x in add(O, mv(R, local(q)))])

    bone("implant", "right", [ARM_X, ARM_TOP, 0.0])

    # bracer: a closed hull around the forearm (at most 1 px wider than the arm). The top has two doors that open
    # into wings flanking the rifle, the bottom has the same doors, closed.
    z0, z1 = BRACER_Z
    L = z1 - z0
    top = ARM_TOP
    bot = ARM_TOP - 4.0
    T = BRACER_T
    w = BRACER_W
    hx = w - DOOR_INSET          # hinge x
    wall = w - 2.0
    H = 4.0 + 2 * T
    bc = []

    def box(mat, o3, s3):
        bc.append(cube(o3, s3, swatch_uv(mat, s3)))

    for sx in (-1, 1):
        box(SW_BRASS, [2.0 if sx > 0 else -w, bot - T, z0], [wall, H, L])                    # side wall
        box(SW_BRASS, [hx if sx > 0 else -2.0, top, z0], [2.0 - hx, T, L])                     # top rim
        box(SW_BRASS, [hx if sx > 0 else -2.0, bot - T, z0], [2.0 - hx, T, L])                 # bottom rim
    box(SW_BAY, [-hx, top, z0], [2 * hx, 0.12, L])                                           # bay floor
    for zc in (z0 - 0.35, z1):                                                               # wrist / elbow frames
        for sx in (-1, 1):
            box(SW_BRASS_DARK, [hx if sx > 0 else -w, top, zc], [w - hx, T, 0.35])
            box(SW_BRASS_DARK, [2.0 if sx > 0 else -w, bot, zc], [wall, 4.0, 0.35])
        box(SW_BRASS_DARK, [-hx, top, zc], [2 * hx, 0.12, 0.35])
        box(SW_BRASS_DARK, [-w, bot - T, zc], [2 * w, T, 0.35])
    box(SW_BRASS_DARK, [-2.0, bot, z0 - 1.1], [4.0, 4.0, 0.75])                            # front plate
    for zz in (z0 + 1.2, z0 + L * 0.55):  # copper bands: straps around the hull, 0.06 proud of it on every side
        for sx in (-1, 1):
            box(SW_COPPER, [hx - 0.01 if sx > 0 else -w - 0.06, bot - T - 0.06, zz], [w - hx + 0.07, H + 0.12, 0.45])
    bone("bracer", "implant", [0, top, 0], cubes=bc)
    bone("status_light_illuminated", "bracer", [-w, top - 0.9, z0 + 2.6],
         cubes=[cube([-w - 0.1, top - 0.9, z0 + 2.6], [0.25, 0.25, 0.25], swatch_uv(SW_LIGHT, [0.25, 0.25, 0.25]))])

    # doors. Cubes are modelled closed; the bind rotation of the top pair is the deployed pose, because TaCZ falls
    # back to the bind pose after "draw" ends (and uses it in third person / GUI). The bottom pair stays closed.
    dw = hx - 0.02
    for side, sfx in ((-1, "l"), (1, "r")):
        x_min = -hx if side < 0 else 0.02
        dc = [cube([x_min, top, z0 + 0.15], [dw, T, L - 0.3], swatch_uv(SW_BRASS, [dw, T, L - 0.3]))]
        for k in range(3):  # ribs on the outer face
            zz = z0 + 0.9 + k * (L - 1.8) / 2
            dc.append(cube([x_min + 0.2, top + T, zz], [dw - 0.4, 0.18, 0.5], swatch_uv(SW_BRASS_DARK, [dw - 0.4, 0.18, 0.5])))
        # inner face: a brass rail along the free edge and two steel latch teeth
        dc.append(cube([(-0.45 if side < 0 else 0.1), top - 0.14, z0 + 0.5], [0.35, 0.16, L - 1.0], swatch_uv(SW_COPPER, [0.35, 0.16, L - 1.0])))
        for zz in (z0 + 1.4, z0 + L - 3.0):
            dc.append(cube([(-1.2 if side < 0 else 0.7), top - 0.25, zz], [0.5, 0.27, 1.2], swatch_uv(SW_IRON_DARK, [0.5, 0.27, 1.2])))
        bone("door_" + sfx, "bracer", [side * hx, top + T, 0], rotation=[0, 0, -side * DOOR_REST], cubes=dc)

        db = [cube([x_min, bot - T, z0 + 0.15], [dw, T, L - 0.3], swatch_uv(SW_BRASS, [dw, T, L - 0.3]))]
        for k in range(3):
            zz = z0 + 0.9 + k * (L - 1.8) / 2
            db.append(cube([x_min + 0.2, bot - T - 0.18, zz], [dw - 0.4, 0.18, 0.5], swatch_uv(SW_BRASS_DARK, [dw - 0.4, 0.18, 0.5])))
        bone("door_bottom_" + sfx, "bracer", [side * hx, bot - T, 0], cubes=db)

    # ---------------- the rifle (Crane geometry, no grip / trigger / stock)
    bone("rifle", "implant", [0.0, 3.6, -1.0])
    # lift pistons under the rifle: they stay inside the arm when folded
    floor = ARM_TOP + 0.12
    pc = []
    for zz in LIFT_Z:
        pc.append(cube([-0.28, 0.6, zz + 0.07], [0.56, floor - 0.6, 0.56], swatch_uv(SW_IRON, [0.56, floor - 0.6, 0.56])))
    pc.append(cube([-1.05, floor, -3.2], [2.1, 3.76 - floor, 7.6], swatch_uv(SW_IRON_DARK, [2.1, 3.76 - floor, 7.6])))  # carriage
    bone("carriage", "rifle", [0, 3.45, -1.0], cubes=pc)

    grip_cubes = {0, 1, 2, 3, 9, 10, 11, 12, 13}
    keep = ["front", "mount", "rotate", "handguard_default", "handguard_tactical", "front_main", "mag", "magazine",
            "mag_standard", "mag_extended_1", "mag_extended_2", "mag_extended_3", "bolt", "front_positioning",
            "muzzle_pos", "muzzle_flash", "scope_pos", "shell", "front_sight_illuminated", "back", "back_main",
            "back_sight_illuminated"]
    for nm in keep:
        b = copy.deepcopy(crane[nm])
        if b.get("parent") == "gun":
            b["parent"] = "rifle"
        if nm == "back_main":
            b["cubes"] = [c for i, c in enumerate(b["cubes"]) if i not in grip_cubes]
        bones.append(b)
    bone("constraint", "rifle", [-IRON[0], IRON[1], IRON[2]])

    # ---------------- left arm (only comes in for the reload / inspect)
    bone("left", "root", [0, 0, 0])
    refl = cube([4, 0, -2], [4, 12, 4], {f: {"uv": [4, 0], "uv_size": [-4, 12]} for f in ("north", "east", "south", "west")} | {"up": {"uv": [0, 4], "uv_size": [4, -4]}, "down": {"uv": [0, 4], "uv_size": [4, -4]}})
    bone("lefthand_pos", "left", [0, 0, 0], cubes=[refl])

    # ---------------- views (same as the Crane so ADS and HUD match)
    bone("view", None, [0, 0, 0])
    bone("iron_view", "view", IRON)
    bone("idle_view", "view", EYE)
    bone("refit_view", "view", [-18, 4, -1], rotation=[0, 90, 0])
    bone("refit_extended_mag_view", "view", [-5, 5, 0], rotation=[0, 90, 0])
    bone("refit_scope_view", "view", [-7, 8, 3], rotation=[0, 60, 0])
    bone("refit_muzzle_view", "view", [-6, 5, -13], rotation=[0, 107.5, 0])
    bone("positioning", None, [0, 0, 0])
    # third person: the Crane held in the hand has its anchor at z 2.5 (muzzle 11 px ahead); the implant sticks out 75% of that
    bone("thirdperson_hand", "positioning", [0, 3.5, 2.5])
    bone("ground", "positioning", [0, 0, 1])
    bone("fixed", "positioning", [1, 4, 1], rotation=[0, -90, 0])
    bone("camera", None, EYE)

    # bones list: Crane bones copied with bedrock pivots already; ours were converted by bone()
    new_geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {"identifier": f"geometry.{NS}.{NAME}", "texture_width": 64, "texture_height": 64,
                            "visible_bounds_width": 6, "visible_bounds_height": 4, "visible_bounds_offset": [0, 0, 0]},
            "bones": bones}]}
    gdir = os.path.join(out, "assets", NS, "geo_models", "gun")
    os.makedirs(gdir, exist_ok=True)
    # Bind pose = third person and GUI (TaCZ draws those without animations): the hull and the lift are moved back
    # onto the forearm. In first person static_idle moves them forward again, under the rifle.
    for b in bones:
        if b["name"] in ("bracer", "status_light_illuminated", "door_l", "door_r", "door_bottom_l", "door_bottom_r", "carriage"):
            b["pivot"] = [r5(b["pivot"][i] + TP_SHIFT[i]) for i in range(3)]
            for c in b.get("cubes", []):
                c["origin"] = [r5(c["origin"][i] + TP_SHIFT[i]) for i in range(3)]
                if "pivot" in c:
                    c["pivot"] = [r5(c["pivot"][i] + TP_SHIFT[i]) for i in range(3)]
    ATLAS.finalize()
    json.dump(new_geo, open(os.path.join(gdir, f"{NAME}_geo.json"), "w", encoding="utf-8"), indent=1)
    json.dump({"palette": PALETTE, "rects": ATLAS.paint}, open(os.path.join(out, "paint.json"), "w", encoding="utf-8"), indent=1)

    write_animations(out, O, R)
    print("geo bones", len(bones))


# ------------------------------------------------------------------ animation helpers
def kf(keys, lerp="catmullrom"):
    """keys: list of (t, [x,y,z]) or (t, [x,y,z], 'linear'); returns TaCZ/bedrock keyframe map"""
    m = {}
    for k in keys:
        t, v = k[0], k[1]
        mode = k[2] if len(k) > 2 else lerp
        vv = [r5(x) for x in v]
        if mode == "linear":
            m[f"{t:.4g}"] = vv
        else:
            m[f"{t:.4g}"] = {"post": vv, "lerp_mode": "catmullrom"}
    return m


def left_pose(palm, axis, palm_dir, scale=ARM_SCALE):
    """left arm keyframe values so the palm (inner face, near the hand end) is at `palm` (three space),
    the arm points along `axis` and the palm faces `palm_dir`. left bone pivot is the origin, no rest rotation."""
    R, local = arm_frame(False, norm(axis), palm_dir, scale)
    O = sub(palm, mv(R, local([-1, 8.5, 0])))
    rot = bed_rot(R)
    return [r5(-O[0]), r5(O[1]), r5(O[2])], rot


def write_animations(out, O, R):
    A = {}
    rest_l_pos, rest_l_rot = left_pose([-16, -16, 6], [0.45, 0.2, -1], [0.3, 1, 0])

    A["static_idle"] = {"loop": True, "bones": {
        "righthand_pos": {"scale": ARM_SCALE},
        "lefthand_pos": {"scale": ARM_SCALE},
        "left": {"position": rest_l_pos, "rotation": rest_l_rot},
        "bracer": {"position": [-TP_SHIFT[0], -TP_SHIFT[1], -TP_SHIFT[2]]},
        "carriage": {"position": [-TP_SHIFT[0], -TP_SHIFT[1], -TP_SHIFT[2]]},
    }}

    # rifle offsets are in the parent (implant) frame, bedrock x
    def p3(v):
        return [-v[0], v[1], v[2]]

    stow = p3(STOW)
    shut = -DOOR_REST  # door_l value when closed (door_r mirrored)
    # ---------------- draw = deploy from the forearm (1.0 s)
    A["draw"] = {"animation_length": 1.0, "loop": "hold_on_last_frame", "bones": {
        "right": {
            "rotation": kf([(0.0, [62, 18, -8]), (0.12, [40, 12, -5]), (0.28, [-1.5, -0.5, 0.5]), (0.36, [0.6, 0, 0]),
                            (0.44, [0, 0, 0]), (0.62, [0, 0, 0]), (0.7, [-0.8, 0, 0.3]), (0.86, [0, 0, 0]), (0.9, [-1.2, 0.2, 0]),
                            (1.0, [0, 0, 0])]),
            "position": kf([(0.0, [-2, -6, 3]), (0.14, [-1, -2.5, 1.2]), (0.3, [0, 0.2, -0.1]), (0.42, [0, 0, 0]),
                            (0.88, [0, 0, 0]), (0.92, [0, 0, 0.35]), (1.0, [0, 0, 0])]),
        },
        "door_l": {"rotation": kf([(0.0, [0, 0, shut]), (0.22, [0, 0, shut]), (0.34, [0, 0, 9]), (0.42, [0, 0, 0]),
                                   (0.7, [0, 0, 0]), (0.76, [0, 0, -4]), (0.8, [0, 0, 0])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, -shut]), (0.22, [0, 0, -shut]), (0.34, [0, 0, -9]), (0.42, [0, 0, 0]),
                                   (0.7, [0, 0, 0]), (0.76, [0, 0, 4]), (0.8, [0, 0, 0])])},
        "rifle": {"position": kf([(0.0, stow), (0.3, stow), (0.46, [stow[0], 0.25, stow[2]]), (0.52, [stow[0], 0, stow[2]]),
                                  (0.56, [0, 0, stow[2]]), (0.68, [0, 0, -0.3]), (0.74, [0, 0, 0])])},
        "rotate": {"rotation": kf([(0.0, [0, 0, 0]), (0.74, [0, 0, 0]), (0.86, [95, 0, 0]), (0.9, [90, 0, 0])])},
        "bolt": {"position": kf([(0.0, [0, 0, 0]), (0.8, [0, 0, 0]), (0.85, [0, 0, 0.75], "linear"), (0.9, [0, 0, 0], "linear")], "linear")},
        "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.3, [-0.6, 0.3, 0]), (0.42, [0.3, -0.1, 0]), (0.55, [0, 0, 0]),
                                   (0.74, [-0.4, 0, 0.2]), (0.82, [0, 0, 0]), (0.9, [-0.5, 0, 0]), (1.0, [0, 0, 0])])},
    }}

    # ---------------- put_away = fold back into the forearm (0.7 s)
    A["put_away"] = {"animation_length": 0.7, "loop": "hold_on_last_frame", "bones": {
        "right": {
            "rotation": kf([(0.0, [0, 0, 0]), (0.42, [0, 0, 0]), (0.56, [30, 9, -4]), (0.7, [62, 18, -8])]),
            "position": kf([(0.0, [0, 0, 0]), (0.42, [0, 0, 0]), (0.56, [-1, -2.5, 1.2]), (0.7, [-2, -6, 3])]),
        },
        "door_l": {"rotation": kf([(0.0, [0, 0, 0]), (0.06, [0, 0, 0]), (0.12, [0, 0, 7]), (0.34, [0, 0, 7]),
                                   (0.45, [0, 0, shut + 5]), (0.49, [0, 0, shut])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, 0]), (0.06, [0, 0, 0]), (0.12, [0, 0, -7]), (0.34, [0, 0, -7]),
                                   (0.45, [0, 0, -shut - 5]), (0.49, [0, 0, -shut])])},
        "rifle": {"position": kf([(0.0, [0, 0, 0]), (0.06, [0, 0, 0]), (0.2, [0, 0, stow[2]]), (0.24, [0, 0, stow[2]]),
                                  (0.36, stow), (0.7, stow)])},
        "rotate": {"rotation": kf([(0.0, [90, 0, 0]), (0.1, [0, 0, 0])])},
        "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.2, [0.3, 0, 0]), (0.36, [-0.3, 0, 0.2]), (0.5, [0, 0, 0])])},
    }}

    # ---------------- shoot (recoil around the shoulder + bolt)
    A["shoot"] = {"animation_length": 0.35, "bones": {
        "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.04, [-1.6, 0.3, 0.4], "linear"), (0.35, [0, 0, 0])]),
                  "position": kf([(0.0, [0, 0, 0]), (0.04, [0, 0, 0.55], "linear"), (0.35, [0, 0, 0])])},
        "bolt": {"position": kf([(0.0, [0, 0, 0]), (0.03, [0, 0, 0.75], "linear"), (0.09, [0, 0, 0], "linear")], "linear")},
        "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.05, [-0.4, 0.1, 0]), (0.2, [0, 0, 0])])},
    }}

    # ---------------- reload: rear section tilts up, left hand swaps the magazine (2.6 s, feed 1.79)
    def lp(palm, axis, palm_dir):
        return left_pose(palm, axis, palm_dir)

    away = (rest_l_pos, rest_l_rot)
    reach = lp([1.2, 4.1, 3.4], [0.35, 0.55, -0.75], [0.1, 1, -0.2])       # under the open rear, palm up
    grab = lp([0.9, 4.6, 2.9], [0.3, 0.55, -0.78], [0.1, 1, -0.2])
    pull = lp([1.6, 3.2, 7.6], [0.42, 0.45, -0.78], [0.15, 1, -0.1])      # magazine drawn out backwards
    low = lp([-6, -9, 7], [0.5, 0.3, -0.8], [0.3, 1, 0])
    push = lp([1.0, 4.4, 4.8], [0.3, 0.55, -0.78], [0.1, 1, -0.2])
    slap = lp([0.6, 7.6, 3.4], [0.25, -0.2, -0.95], [0, -1, 0.1])           # palm down on the rear section

    def lkeys(seq):
        pos = [(t, p[0]) for t, p in seq]
        rot = [(t, p[1]) for t, p in seq]
        return {"position": kf(pos), "rotation": kf(rot)}

    def mag_follow(seq):
        return kf(seq)

    for nm, extra_bolt in (("reload_tactical", False), ("reload_empty", True)):
        seq = [(0.0, away), (0.25, away), (0.55, reach), (0.68, grab), (0.82, grab), (1.05, pull), (1.25, low),
               (1.4, low), (1.6, pull), (1.82, push), (1.92, push), (2.05, slap), (2.12, slap)]
        if extra_bolt:
            seq += [(2.3, away), (2.6, away)]
        else:
            seq += [(2.4, away), (2.6, away)]
        A[nm] = {"animation_length": 2.6, "loop": "hold_on_last_frame", "bones": {
            "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.25, [3, 4, -16]), (2.1, [3, 4, -16]), (2.18, [4.5, 4, -14]),
                                      (2.4, [0, 0, 0])]),
                      "position": kf([(0.0, [0, 0, 0]), (0.25, [0.4, 0.6, -0.6]), (2.1, [0.4, 0.6, -0.6]), (2.4, [0, 0, 0])])},
            "back": {"rotation": kf([(0.0, [0, 0, 0]), (0.2, [0, 0, 0]), (0.4, [32, 0, 0]), (2.0, [32, 0, 0]),
                                     (2.08, [-2, 0, 0], "linear"), (2.14, [0, 0, 0])])},
            "rotate": {"rotation": kf([(0.0, [0, 0, 0]), (0.2, [0, 0, 0]), (0.4, [-60, 0, 0]), (2.0, [-60, 0, 0]),
                                       (2.12, [0, 0, 0])])},
            "mag": {"position": kf([(0.0, [0, 0, 0]), (0.82, [0, 0, 0]), (1.05, [0, -0.9, 4.6]), (1.25, [-7.5, -12.5, 4]),
                                    (1.4, [-7.5, -12.5, 4]), (1.6, [0, -0.9, 4.6]), (1.82, [0, 0, 1.2]), (1.92, [0, 0, 0])])},
            "left": lkeys(seq),
            "camera": {"rotation": kf([(0.0, [0, 0, 0]), (0.3, [0.5, 0.3, -0.5]), (0.7, [0.2, 0, 0]), (1.05, [0.6, 0, 0.2]),
                                       (1.82, [-0.4, 0, 0]), (2.06, [1.2, 0, -0.4]), (2.3, [0, 0, 0])])},
        }}
        if extra_bolt:
            A[nm]["bones"]["bolt"] = {"position": kf([(0.0, [0, 0, 0]), (2.16, [0, 0, 0]), (2.22, [0, 0, 0.75], "linear"),
                                                      (2.28, [0, 0, 0], "linear")], "linear")}

    # ---------------- inspect: turn the forearm to look at the rifle, flex the doors, spin the cog (3.2 s)
    A["inspect"] = {"animation_length": 3.2, "loop": "hold_on_last_frame", "bones": {
        "right": {"rotation": kf([(0.0, [0, 0, 0]), (0.45, [-8, -22, -38]), (1.4, [-8, -24, -40]), (1.8, [6, 10, 30]),
                                  (2.6, [6, 12, 32]), (3.0, [0, 0, 0])]),
                  "position": kf([(0.0, [0, 0, 0]), (0.45, [1.5, 2.5, -2]), (1.4, [1.5, 2.6, -2]), (1.8, [-1, 1.5, -1.5]),
                                  (2.6, [-1, 1.6, -1.5]), (3.0, [0, 0, 0])])},
        "door_l": {"rotation": kf([(0.0, [0, 0, 0]), (1.9, [0, 0, 0]), (2.05, [0, 0, 14]), (2.2, [0, 0, 0])])},
        "door_r": {"rotation": kf([(0.0, [0, 0, 0]), (1.9, [0, 0, 0]), (2.05, [0, 0, -14]), (2.2, [0, 0, 0])])},
        "rotate": {"rotation": kf([(0.0, [0, 0, 0]), (2.1, [0, 0, 0]), (2.4, [180, 0, 0], "linear")], "linear")},
        "bolt": {"position": kf([(0.0, [0, 0, 0]), (2.5, [0, 0, 0]), (2.56, [0, 0, 0.75], "linear"), (2.62, [0, 0, 0], "linear")], "linear")},
    }}
    A["inspect_empty"] = copy.deepcopy(A["inspect"])

    adir = os.path.join(out, "assets", NS, "animations")
    os.makedirs(adir, exist_ok=True)
    json.dump({"format_version": "1.8.0", "animations": A}, open(os.path.join(adir, f"{NAME}.animation.json"), "w", encoding="utf-8"), indent=1)


if __name__ == "__main__":
    main()
