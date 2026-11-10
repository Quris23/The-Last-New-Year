# Builds the cinematic's resources from the sources in the Last New Year profile folder:
#   Minecraft trailer.mp4, videoplayback.ogg (Mojang logo sound), Aria-Math Cinematic-Orchestra.ogg
# and Minecraft's own font from the Modrinth app's copy of the 1.21.1 client. Output goes to
# earlywindow/build/assets/lnycinematic (packed into the jar by build.sh). The closing logo (stars.png, logo.png) is make_still.py's.
#
#   python make_assets.py <profile dir> [steps]
import glob
import io
import json
import os
import shutil
import subprocess
import sys
import zipfile
from concurrent.futures import ThreadPoolExecutor

import numpy as np
from PIL import Image, ImageChops

FPS = 24
VIDEO_END = 64.0      # from here on the trailer is a single still (the movie logo) that fades in and out
STILL_AT = 70.0       # a moment where that still is at full brightness
RED_FRAMES = 103      # frames 1..103 have the red Mojang background
LOGO_SOUND_END = 4.375  # the trailer cuts to the first scene here (frame 105, 0-based); the music starts on that cut
RED = (238, 47, 60)   # the trailer's background

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
out = os.path.join(root, 'build', 'assets', 'lnycinematic')
work = os.path.join(root, 'build', 'work')
profile = sys.argv[1]
trailer = os.path.join(profile, 'Minecraft trailer.mp4')
vanilla_jar = sorted(glob.glob(os.path.join(os.environ['APPDATA'], 'ModrinthApp', 'meta', 'versions', '1.21.1-*', '1.21.1-*.jar')))[-1]


def ffmpeg(*args):
    subprocess.run(['ffmpeg', '-v', 'error', '-y', *args], check=True)


# The same keying as the one used on reimagined-intro's frames: how much of the background red a pixel holds
def red_alpha(im):
    r, g, b = im.split()
    alpha = ImageChops.subtract(r, ImageChops.lighter(g, b)).point(lambda v: min(255, v * 255 // (RED[0] - RED[2])))
    hue = ImageChops.subtract(g, b).point(lambda v: 255 if v <= 8 else 0)
    bright = r.point(lambda v: 255 if v >= 200 else 0)
    ratio = ImageChops.subtract(b, r.point(lambda v: v * 40 // 255))
    ratio_hi = ImageChops.subtract(r.point(lambda v: v * 115 // 255), b)
    shaded = ImageChops.multiply(ratio.point(lambda v: 255 if v > 0 else 0), ratio_hi.point(lambda v: 255 if v > 0 else 0))
    return ImageChops.multiply(ImageChops.multiply(alpha, hue), ImageChops.lighter(bright, shaded))


def deredden(im):
    im = im.convert('RGB')
    alpha = red_alpha(im)
    return Image.merge('RGB', [ImageChops.subtract(c, alpha.point(lambda v, k=k: v * k // 255)) for c, k in zip(im.split(), RED)])


def video():
    frames = os.path.join(out, 'video')
    shutil.rmtree(frames, ignore_errors=True)
    os.makedirs(frames)
    count = int(VIDEO_END * FPS)
    width, height = 1920, 1080  # the trailer's own
    # RGB the way the trailer is meant to be shown (BT.709, limited range), encoded by Pillow. A jpg is always read as
    # BT.601, so handing ffmpeg's YUV straight to a jpg shifts the colours (reds lose up to a few percent, greens gain).
    # 4:4:4 at quality 90: as close to the decoded trailer as 4:2:0 at 95, and smaller.
    decode = subprocess.Popen(['ffmpeg', '-v', 'error', '-i', trailer, '-frames:v', str(count), '-vf',
                               'scale=in_color_matrix=bt709:in_range=tv:out_range=pc:flags=bicubic+accurate_rnd+full_chroma_int,'
                               'format=rgb24', '-f', 'rawvideo', '-'], stdout=subprocess.PIPE)

    def save(n, im):
        if n < RED_FRAMES:
            im = deredden(im)
        im.save(os.path.join(frames, f'{n:04d}.jpg'), quality=90, subsampling=0, optimize=True)

    workers = os.cpu_count()
    with ThreadPoolExecutor(workers) as pool:
        jobs = []
        for n in range(count):
            raw = decode.stdout.read(width * height * 3)
            assert len(raw) == width * height * 3, n
            jobs.append(pool.submit(save, n, Image.frombytes('RGB', (width, height), raw)))
            if len(jobs) >= 2 * workers:  # a frame is 6 MB, don't read the whole trailer in ahead of the encoders
                jobs.pop(0).result()
        for job in jobs:
            job.result()
    assert decode.wait() == 0
    assert len(os.listdir(frames)) == count


def tail():
    # how bright each tail frame is compared with the still (shown instead of it: make_still.py's logo and stars, faded in the same way)
    raw = os.path.join(work, 'tail.raw')
    ffmpeg('-ss', str(VIDEO_END), '-i', trailer, '-vf', 'scale=320:180,format=gray', '-f', 'rawvideo', raw)
    gray = np.fromfile(raw, dtype=np.uint8).reshape(-1, 180, 320).astype(np.float64)
    still = gray[int((STILL_AT - VIDEO_END) * FPS)]
    mask = still > 24  # the logo and its glow, not the black around it
    levels = [min(1.0, float(g[mask].mean() / still[mask].mean())) for g in gray]
    with open(os.path.join(out, 'tail.txt'), 'w') as f:
        f.write('\n'.join(f'{v:.4f}' for v in levels) + '\n')
    print('tail frames', len(levels), 'video ends at', VIDEO_END + len(levels) / FPS)


def audio():
    vp = os.path.join(profile, 'videoplayback.ogg')
    aria = os.path.join(profile, 'Aria-Math Cinematic-Orchestra.ogg')
    delay = int(LOGO_SOUND_END * 1000)
    # Aria Math: +4 dB in the first cut, then at 70 % of that (-3.1 dB)
    ffmpeg('-i', vp, '-i', aria, '-filter_complex',
           f'[0:a]atrim=0:{LOGO_SOUND_END + 0.12},afade=t=out:st={LOGO_SOUND_END - 0.08}:d=0.2,volume=-1dB[a];'
           f'[1:a]volume=0.9dB,afade=t=in:d=0.03,adelay={delay}|{delay}[b];'
           '[a][b]amix=inputs=2:normalize=0:duration=longest,alimiter=limit=0.95[o]',
           '-map', '[o]', '-ar', '44100', '-ac', '2', '-c:a', 'libvorbis', '-q:a', '5', os.path.join(out, 'music.ogg'))


def minecraft_font():
    # Minecraft's own text font, the one reimagined-intro's "ESC to skip" is drawn with: the glyphs of font/ascii.png
    # (font/include/default.json), each as wide as its last non-empty column and advancing one pixel more; a space
    # advances 4 (font/include/space.json)
    jar = zipfile.ZipFile(vanilla_jar)
    sheet = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/font/ascii.png'))).convert('RGBA')
    providers = json.loads(jar.read('assets/minecraft/font/include/default.json'))['providers']
    index = next(i for i, p in enumerate(providers) if p.get('file') == 'minecraft:font/ascii.png')
    ascii_rows = providers[index]['chars']
    earlier = {c for p in providers[:index] for row in p.get('chars', []) for c in row}  # these take the glyph first
    cw, ch = sheet.width // len(ascii_rows[0]), sheet.height // len(ascii_rows)
    glyphs = {' ': (None, 4)}
    for r, row in enumerate(ascii_rows):
        for c, char in enumerate(row):
            if char == '\0' or char in glyphs or char in earlier:
                continue
            cell = sheet.crop((c * cw, r * ch, (c + 1) * cw, (r + 1) * ch))
            used = np.nonzero(np.asarray(cell)[:, :, 3].any(axis=0))[0]
            width = int(used.max()) + 1 if len(used) else 0
            glyphs[char] = (cell.crop((0, 0, width, ch)), width + 1)
    return glyphs, ch


# White text one image pixel per font pixel, as wide as Minecraft measures it; Cinematic draws it at the GUI scale
def label(font, name, text):
    glyphs, height = font
    im = Image.new('RGBA', (sum(glyphs[c][1] for c in text), height), (255, 255, 255, 0))
    x = 0
    for c in text:
        glyph, advance = glyphs[c]  # a KeyError here: a character Minecraft would take from another font
        if glyph is not None:
            im.alpha_composite(glyph, (x, 0))
        x += advance
    im.save(os.path.join(out, 'labels', name + '.png'))


def labels():
    shutil.rmtree(os.path.join(out, 'labels'), ignore_errors=True)
    os.makedirs(os.path.join(out, 'labels'))
    font = minecraft_font()
    label(font, 'skip', 'ESC to skip')
    label(font, 'loaded', 'Modpack loaded')
    label(font, 'continue', 'ESC to continue')


os.makedirs(out, exist_ok=True)
os.makedirs(work, exist_ok=True)
for step in sys.argv[2:] or ['video', 'tail', 'audio', 'labels']:
    globals()[step]()
