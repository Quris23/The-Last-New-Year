# Our logo layer in place of MOJANG STUDIOS: the cinematic's closing logo on black (../tools/make_still.py), as big and
# where it is on the screen there, so that this intro begins where the cinematic ends
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'tools'))
import make_still

SIZE = (2560, 1440)
C = (1281.5, 728.5)  # the middle of MOJANG STUDIOS, which the layer is moved about


def layer():
    return make_still.layer(SIZE)[0]
