# Refines the tracked layer motion at half resolution: robust (capped) difference between the frame and the moved
# Mojang layer around its text, by coordinate descent from the coarse track
import json
import numpy as np
from PIL import Image, ImageFilter

SRC = 'assets/reimagined-intro/textures/intro/frame_%04d.jpg'
D = 2
C = (1281.5 / D, 728.5 / D)

def load(i):
    im = Image.open(SRC % i).convert('RGB')
    return im.resize((im.width // D, im.height // D), Image.BOX)

mojang = load(74)
text = Image.fromarray(((np.asarray(mojang).max(2) > 100) * 255).astype(np.uint8))
region = text.filter(ImageFilter.MaxFilter(9))

def warp(img, s, dx, dy):
    dx, dy = dx / D, dy / D
    a = (1 / s, 0, C[0] - (C[0] + dx) / s, 0, 1 / s, C[1] - (C[1] + dy) / s)
    return img.transform(img.size, Image.AFFINE, a, resample=Image.BILINEAR)

est = {int(k): v for k, v in json.load(open('track.json')).items()}
est[82] = [(a + b) / 2 for a, b in zip(est[81], est[83])]
out = {}
for i in range(77, 93):
    f = np.asarray(load(i), dtype=np.float64)
    s, dx, dy = est[i][:3]
    tw = np.asarray(warp(text, s, dx, dy), dtype=np.float64) / 255
    seen = (tw > 0.9) & (f.max(2) > 90) & (f.max(2) - f.min(2) < 100)
    tint = np.median(f[seen], axis=0) / 253 if seen.sum() > 50 else np.ones(3)

    def cost(s, dx, dy):
        lw = np.asarray(warp(mojang, s, dx, dy), dtype=np.float64) * tint
        r = np.asarray(warp(region, s, dx, dy)) > 0
        return np.minimum(np.abs(f - lw).max(2), 60)[r].mean()

    best = cost(s, dx, dy)
    for step_s, step in ((0.01, 4.0), (0.004, 2.0), (0.002, 1.0), (0.001, 0.5)):
        moved = True
        while moved:
            moved = False
            for cand in ((s + step_s, dx, dy), (s - step_s, dx, dy), (s, dx + step, dy), (s, dx - step, dy),
                         (s, dx, dy + step), (s, dx, dy - step)):
                c = cost(*cand)
                if c < best - 1e-6:
                    best, (s, dx, dy), moved = c, cand, True
    out[i] = (s, dx, dy, [float(t) for t in tint])
    print(i, 's %.3f dx %.1f dy %.1f cost %.1f' % (s, dx, dy, best), 'coarse', [round(v, 2) for v in est[i][:3]], flush=True)
json.dump(out, open('refined.json', 'w'))
