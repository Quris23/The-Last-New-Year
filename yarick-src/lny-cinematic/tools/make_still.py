# The closing logo (tools/logo.png, the user's own render: transparent, any size) for the still it is shown on: 1920x1080
# dark space like the trailer's, the logo about as big and where the trailer's own logo is, with a glow of its own colours.
# The two are kept apart, as the space fades out under the logo at the end: writes build/assets/lnycinematic/stars.png and
# logo.png (premultiplied: the glow adds light where the logo itself is transparent). The logo on black is also how
# reimagined-intro's intro begins (../intro/layer.py), so that one follows on from the other.
#
#   python make_still.py
import os

import numpy as np
from PIL import Image, ImageFilter

STILL = (1920, 1080)
WIDTH = 1382            # of the logo on the still (the trailer's own logo takes about two thirds of the width)
CENTRE = (960, 560)     # where the middle of the logo goes; the loading bars under the held logo are placed for this
GLOW = (24, 0.9)        # blur radius, strength

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
out = os.path.join(root, 'build', 'assets', 'lnycinematic')
rng = np.random.default_rng(2026)


def c(hex_colour):
    return np.array([int(hex_colour[i:i + 2], 16) for i in (1, 3, 5)], dtype=np.float64)


def space():
    w, h = STILL
    ys, xs = np.mgrid[0:h, 0:w]
    img = np.zeros((h, w, 3)) + np.array([4.0, 3.0, 9.0])
    for gx, gy, rx, ry, colour, strength in ((960, 470, 700, 250, c('#FF8C28'), 0.10),
                                             (960, 690, 780, 300, c('#9628DC'), 0.14)):
        img += np.exp(-(((xs - gx) / rx) ** 2 + ((ys - gy) / ry) ** 2))[..., None] * colour * strength
    stars = np.zeros((h, w, 3))
    n = 900
    sx, sy = rng.integers(0, w, n), rng.integers(0, h, n)
    bright = 25 + 230 * rng.random(n) ** 5
    tint = np.where(rng.random(n)[:, None] < 0.8, np.array([0.85, 0.9, 1.0]), np.array([1.0, 0.75, 0.5]))
    stars[sy, sx] = bright[:, None] * tint
    big = bright > 150
    for dy, dx in ((0, 1), (1, 0), (0, -1), (-1, 0)):
        stars[np.clip(sy[big] + dy, 0, h - 1), np.clip(sx[big] + dx, 0, w - 1)] += bright[big, None] * tint[big] * 0.35
    stars = np.asarray(Image.fromarray(np.clip(stars, 0, 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(0.6)), dtype=np.float64) * 1.6
    vignette = 1 - 0.45 * (((xs - w / 2) / (w / 2)) ** 2 * 0.6 + ((ys - h / 2) / (h / 2)) ** 2 * 0.4)
    return (img + stars) * vignette[..., None]


# The logo with its glow on a picture of the given size (16:9, scaled from the still's), premultiplied: colour, alpha 0..255
def layer(size=STILL):
    f = size[0] / STILL[0]
    logo = Image.open(os.path.join(root, 'tools', 'logo.png')).convert('RGBA')
    logo = logo.crop(logo.getchannel('A').getbbox())
    # placed to a fraction of a pixel, the same at any size: the cinematic's still and reimagined-intro's frames match
    k = WIDTH * f / logo.width
    x0, y0 = CENTRE[0] * f - logo.width * k / 2, CENTRE[1] * f - logo.height * k / 2
    pad = 8
    padded = Image.new('RGBA', (logo.width + 2 * pad, logo.height + 2 * pad), (0, 0, 0, 0))
    padded.paste(logo, (pad, pad))
    left, top = int(np.floor(x0)), int(np.floor(y0))
    right, bottom = int(np.ceil(x0 + logo.width * k)), int(np.ceil(y0 + logo.height * k))
    box = (pad + (left - x0) / k, pad + (top - y0) / k, pad + (right - x0) / k, pad + (bottom - y0) / k)
    logo = padded.resize((right - left, bottom - top), Image.LANCZOS, box=box)
    offset = (left, top)
    rgba = Image.new('RGBA', size, (0, 0, 0, 0))
    rgba.paste(logo, offset)
    rgba = np.asarray(rgba, dtype=np.float64)
    a = rgba[..., 3:] / 255
    pre = rgba[..., :3] * a
    glow = np.asarray(Image.fromarray(pre.astype(np.uint8)).filter(ImageFilter.GaussianBlur(GLOW[0] * f)), dtype=np.float64) * GLOW[1]
    return glow * (1 - a) + pre, rgba[..., 3], logo.size, offset


def main():
    colour, alpha, size, offset = layer()
    os.makedirs(out, exist_ok=True)
    Image.fromarray(np.clip(space(), 0, 255).astype(np.uint8)).save(os.path.join(out, 'stars.png'))
    logo = np.dstack([np.clip(colour + 0.5, 0, 255), alpha]).astype(np.uint8)
    Image.fromarray(logo, 'RGBA').save(os.path.join(out, 'logo.png'))
    if os.path.exists(os.path.join(out, 'still.png')):  # the two in one, as it was before
        os.remove(os.path.join(out, 'still.png'))
    print('logo', size, 'at', offset)


if __name__ == '__main__':
    main()
