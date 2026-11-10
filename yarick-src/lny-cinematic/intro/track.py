# Tracks the MOJANG STUDIOS layer (frame 74) through the transition (coarse, refine.py refines it): for each scale about the text centre, every shift at
# once by FFT correlation of the text mask with (white text visible) - 1.5 (black void)
import json
import numpy as np
from PIL import Image

SRC = 'assets/reimagined-intro/textures/intro/frame_%04d.jpg'
D = 4
C = (1281.5, 728.5)

def load(i):
    im = Image.open(SRC % i).convert('RGB')
    return np.asarray(im.resize((im.width // D, im.height // D), Image.BOX), dtype=np.float64)

base = load(74)
textim = Image.fromarray(((base.max(2) > 100) * 255).astype(np.uint8))
H, W = base.shape[:2]
cx, cy = C[0] / D, C[1] / D

def scaled(s):
    a = (1 / s, 0, cx - cx / s, 0, 1 / s, cy - cy / s)
    return np.asarray(textim.transform(textim.size, Image.AFFINE, a, resample=Image.BILINEAR), dtype=np.float64) / 255

def maps(f):
    mx, mn = f.max(2), f.min(2)
    return ((mx > 100) & (mx - mn < 90)).astype(np.float64) - 1.5 * (mx < 22)

PH, PW = 2 * H, 2 * W
est = {}
prev = None
for i in range(75, 100):
    m = maps(load(i))
    M = np.fft.rfft2(m, (PH, PW))
    best = None
    for s in np.arange(0.30, 1.051, 0.01):
        t = scaled(s)
        T = np.fft.rfft2(t[::-1, ::-1], (PH, PW))
        corr = np.fft.irfft2(M * T, (PH, PW))
        # corr[y, x] = sum m(p) t(p - d) with d = (y - (H - 1), x - (W - 1))
        sub = corr[H - 1 - H // 2:H - 1 + H // 2, W - 1 - W // 2:W - 1 + W // 2]
        k = np.unravel_index(np.argmax(sub), sub.shape)
        v = sub[k]
        if best is None or v > best[0]:
            best = (v, s, k[1] - W // 2, k[0] - H // 2, t.sum())
    v, s, dx, dy, n = best
    print(i, 'score %.0f of %.0f' % (v, n), 's %.2f dx %d dy %d' % (s, dx * D, dy * D), flush=True)
    est[i] = (float(s), float(dx * D), float(dy * D), float(v / n))
json.dump(est, open('track.json', 'w'))
