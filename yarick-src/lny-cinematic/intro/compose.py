# Replaces MOJANG STUDIOS in the intro with our logo: frames up to 76 (up to the hit in the music at 3.1 s where the grass
# breaks in) show it on black, as the cinematic ends; in the transition (77-93) it sits in the hole behind the grass, moving
# like the original. Frames before FIRST are cut by pack.py.
import json
import os
import sys
import numpy as np
from PIL import Image, ImageFilter
import layer

SRC = 'assets/reimagined-intro/textures/intro/frame_%04d.jpg'
OUT = 'out'
C = layer.C
FIRST = 26  # pack.py's CUT + 1
LAST_STILL = 76
LAST = 93  # the old text is gone from here on

est = {int(k): v for k, v in json.load(open('refined.json')).items()}  # refine.py, frames 77-92
fit = range(85, 93)
curves = [np.polyfit(fit, [est[i][j] for i in fit], 1) for j in range(3)]

def motion(i):
    return est[i][:3] if i in est else [float(np.polyval(c, i)) for c in curves]

def warp(img, s, dx, dy):
    a = (1 / s, 0, C[0] - (C[0] + dx) / s, 0, 1 / s, C[1] - (C[1] + dy) / s)
    return img.transform(img.size, Image.AFFINE, a, resample=Image.BICUBIC)

def arr(img):
    return np.asarray(img, dtype=np.float64)

def img(a):
    return Image.fromarray(np.clip(a + 0.5, 0, 255).astype(np.uint8))

def save(a, i):
    img(a).save(os.path.join(OUT, 'frame_%04d.jpg' % i), quality=93, subsampling=2)

os.makedirs(OUT, exist_ok=True)
ours = img(layer.layer())
mojang = Image.open(SRC % 74).convert('RGB')
text = Image.fromarray(((arr(mojang).max(2) > 100) * 255).astype(np.uint8))

frames = sys.argv[1:] and [int(a) for a in sys.argv[1:]] or range(FIRST, LAST + 1)
for i in frames:
    if i <= LAST_STILL:
        save(arr(ours), i)
        continue
    f = arr(Image.open(SRC % i).convert('RGB'))
    s, dx, dy = motion(i)
    lw = arr(warp(mojang, s, dx, dy))
    tw = arr(warp(text, s, dx, dy)) / 255
    ow = arr(warp(ours, s, dx, dy))
    # tint of the layer here (it turns pink as the camera flies into the cave)
    tint = np.array(est[i][3] if i in est else est[92][3])
    diff = np.abs(f - lw * tint).max(2)
    m = (diff < 55) & ((tw > 0.5) | (f.max(2) < 24))
    # the letters' edges (a pixel or two off, blurred): in a thin band around them, take what is the layer's colour
    # (black to tinted white) and touches the layer
    t8 = Image.fromarray((tw * 255).astype(np.uint8))
    band = (arr(t8.filter(ImageFilter.MaxFilter(5))) > 0) & (arr(t8.filter(ImageFilter.MinFilter(5))) < 255)
    g = f / tint
    grey = (g.max(2) - g.min(2)) < 28
    touch = arr(Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(5))) > 0
    m = m | band & grey & touch
    if tint[0] - tint[1] > 0.08:
        # once the layer is pink its light is uneven and the track less sure: near the letters take anything of the
        # layer's colours, black or pinkish white (grey stone, grass and the red cave are not)
        near = arr(t8.filter(ImageFilter.MaxFilter(41))) > 0
        m = m | near & ((f.max(2) < 24) | grey & (g.max(2) > 40))
    # only where the old layer or ours shows something: elsewhere both are black and the source stays as it is
    shows = Image.fromarray((((ow * tint).max(2) > 4) | (tw > 0.02)).astype(np.uint8) * 255).filter(ImageFilter.MaxFilter(9))
    m = m & (arr(shows) > 0)
    # close the thin gaps left along the letters' edges (the source's colour bleeds there)
    m8 = Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(7)).filter(ImageFilter.MinFilter(7))
    m = arr(m8) / 255
    a = arr(Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.2)))[..., None] / 255
    save(f * (1 - a) + ow * tint * a, i)
    print(i, 's %.2f dx %.0f dy %.0f' % (s, dx, dy), 'tint', np.round(tint, 2), 'layer px', int(m.sum()), flush=True)
