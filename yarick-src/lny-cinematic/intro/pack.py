# Writes the reimagined-intro jar with our intro: the first CUT frames and as much of the music are cut, the rest of the
# frames renumbered from 1, ours (out/frame_NNNN.jpg, numbered as in the original) in place of the originals; the music is
# silent up to the hit where the grass breaks in (all before it is the Mojang chime), so our logo shows in silence. The
# mod's lengths are constants compiled into its classes, so those are changed to match; and its intro neither fades in from
# black nor is black under the loading overlay, as it now follows on from the cinematic's last frame.
import os
import struct
import subprocess
import sys
import zipfile

import numpy as np

src, dst = sys.argv[1], sys.argv[2]
CUT = 25  # frames of 40 ms: 1 s
RATE = 48000  # the music's sample rate
HIT = 3.095  # seconds into the original music: silent before, faded in over FADE
FADE = 0.01
FRAMES = 'assets/reimagined-intro/textures/intro/'
MUSIC = 'assets/reimagined-intro/sounds/intro.ogg'
CLIENT = 'noobtrieu/reimaginedintro/client/'
INTRO_FRAMES = 161
INTRO_MS = 6440
PREFETCH_AT = 153  # where the intro starts loading the loop: 8 frames before its end


def sipush(v):
    return b'\x11' + struct.pack('>h', v)


def ldc_long(v):  # a CONSTANT_Long in the constant pool
    return b'\x05' + struct.pack('>q', v)


def ldc_int(v):  # a CONSTANT_Integer
    return b'\x03' + struct.pack('>i', v)


# class: [(old bytes, new bytes, how many there are)]
PATCHES = {
    CLIENT + 'FrameStreamer.class': [
        (sipush(INTRO_FRAMES), sipush(INTRO_FRAMES - CUT), 6),
        (sipush(PREFETCH_AT), sipush(PREFETCH_AT - CUT), 1),
        (ldc_int(INTRO_FRAMES), ldc_int(INTRO_FRAMES - CUT), 1),  # INTRO_FRAME_COUNT's value
        (ldc_long(INTRO_MS), ldc_long(INTRO_MS - CUT * 40), 1),
    ],
    CLIENT + 'IntroManager.class': [(ldc_long(INTRO_MS), ldc_long(INTRO_MS - CUT * 40), 1)],
    CLIENT + 'IntroScreen.class': [
        (ldc_long(INTRO_MS), ldc_long(INTRO_MS - CUT * 40), 1),
        # black over the first 400 ms, 1 - elapsed / 400: fconst_1 -> fconst_0 (0 - elapsed / 400, never above 0)
        (b'\x0c\x16\x09\x89\x12\x67\x6e\x66', b'\x0b\x16\x09\x89\x12\x67\x6e\x66', 1),
        # black while the loading overlay is up (its last second crossfades to this screen): Minecraft.getInstance()
        # .getOverlay() -> pop, aconst_null, nop, so the intro (its first frame, the cinematic's last) shows under it
        (b'\xb8\x00\x17\xb6\x00\x1d\xc6\x00\x14', b'\xb8\x00\x17\x57\x01\x00\xc6\x00\x14', 1),
    ],
}


def patch(name, data):
    for old, new, count in PATCHES[name]:
        found = data.count(old)
        assert found == count, f'{name}: {old.hex()} found {found} times, not {count}: another version of the mod?'
        data = data.replace(old, new)
    return data


def music(data):
    with open('intro.original.ogg', 'wb') as f:
        f.write(data)
    raw = subprocess.run(['ffmpeg', '-v', 'error', '-i', 'intro.original.ogg', '-f', 'f32le', '-ac', '2', '-ar', str(RATE), '-'],
                         capture_output=True, check=True).stdout
    pcm = np.frombuffer(raw, np.float32).reshape(-1, 2)
    t = np.arange(len(pcm)) / RATE
    pcm = (pcm * np.clip((t - HIT) / FADE, 0, 1)[:, None])[CUT * RATE // 25:].astype(np.float32)
    subprocess.run(['ffmpeg', '-y', '-v', 'error', '-f', 'f32le', '-ac', '2', '-ar', str(RATE), '-i', '-',
                    '-c:a', 'libvorbis', '-q:a', '6', 'intro.ogg'], input=pcm.tobytes(), check=True)
    with open('intro.ogg', 'rb') as f:
        return f.read()


with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED) as zout:
    ours = cut = 0
    for item in zin.infolist():
        name = item.filename
        data = zin.read(name)
        if name.startswith(FRAMES) and name.endswith('.jpg'):
            n = int(name[len(FRAMES):].split('_')[1].split('.')[0])
            if n <= CUT:
                cut += 1
                continue
            mine = os.path.join('out', 'frame_%04d.jpg' % n)
            if os.path.isfile(mine):
                with open(mine, 'rb') as f:
                    data = f.read()
                ours += 1
            item = zipfile.ZipInfo(FRAMES + 'frame_%04d.jpg' % (n - CUT), item.date_time)
            item.compress_type = zipfile.ZIP_DEFLATED
        elif name == MUSIC:
            data = music(data)
        elif name in PATCHES:
            data = patch(name, data)
        zout.writestr(item, data)
print('cut', cut, 'frames, ours in', ours)
