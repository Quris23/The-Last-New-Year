#!/bin/sh
# Puts our logo (../tools/logo.png, as the cinematic ends: ../tools/make_still.py) into reimagined-intro's intro in place of
# MOJANG STUDIOS: the first second is cut, up to the hit in the music where the grass breaks in (2.1 s, silent: the Mojang
# chime is muted) it shows our logo on black, then the logo sits in the hole the camera flies away from, tracked from the original. Builds from
# reimagined-intro-3.0.0.original.jar and copies the result into PROFILES.
#   sh make_intro.sh [compose|pack]   (compose: keep the track of the last run; pack: only repack build/intro/out)
set -e
cd "$(dirname "$0")"
HERE="$(pwd)"
PROFILES="Last New Year;IntroLogo"
mkdir -p ../build/intro
cd ../build/intro
if [ "$1" != pack ]; then
   rm -rf assets out
   unzip -q -o "$HERE/reimagined-intro-3.0.0.original.jar" 'assets/reimagined-intro/textures/intro/*'
   if [ "$1" != compose ]; then
      python -u "$HERE/track.py"
      python -u "$HERE/refine.py"
   fi
   python -u "$HERE/compose.py"
fi
python "$HERE/pack.py" "$HERE/reimagined-intro-3.0.0.original.jar" reimagined-intro-3.0.0.jar
IFS=';'
for profile in $PROFILES; do
   cp reimagined-intro-3.0.0.jar "$APPDATA/ModrinthApp/profiles/$profile/mods/" \
      || echo "NOT copied into $profile: the jar is in use, is the game running?" >&2
done
