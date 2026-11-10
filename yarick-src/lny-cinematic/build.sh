#!/bin/sh
# Builds lny-cinematic-1.0.0.jar (NeoForge 21.1 / fancymodloader 4.0.45 early window provider, with the trailer in it) and
# copies it into the mods folders of PROFILES. Resources come from tools/make_assets.py and tools/make_still.py (run them
# first, they write build/assets). Next to it goes lny-cinematic-options-1.0.0.jar, a small mod
# (options/: the trailer's volume slider among Minecraft's sound options, by mixin), as the early window itself can't reach
# Minecraft's classes. It can't ride inside the main jar: FML puts that one in the service layer and never looks for mods
# (jar-in-jar included) in such jars.
set -e
cd "$(dirname "$0")"
LIB="$APPDATA/ModrinthApp/meta/libraries"
PROFILES="Last New Year;IntroLogo"
CP="$LIB/net/neoforged/fancymodloader/earlydisplay/4.0.45/earlydisplay-4.0.45.jar"
CP="$CP;$LIB/net/neoforged/fancymodloader/loader/4.0.45/loader-4.0.45.jar"
CP="$CP;$LIB/cpw/mods/modlauncher/11.0.5/modlauncher-11.0.5.jar"
CP="$CP;$LIB/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar"
CP="$CP;$LIB/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"
for m in lwjgl lwjgl-glfw lwjgl-opengl lwjgl-stb lwjgl-tinyfd; do CP="$CP;$LIB/org/lwjgl/$m/3.3.3/$m-3.3.3.jar"; done
rm -rf build/classes build/jar
mkdir -p build/classes build/jar
javac --release 21 -encoding UTF-8 -cp "$CP" -d build/classes $(find src/main/java -name '*.java')
cp -r src/main/resources/. build/classes/
# the nested mod, compiled against Minecraft as NeoForm decompiles it for witherstormmod (C:/wsp)
MC=$(cygpath -m /c/wsp/build/neoForm/neoFormJoined1.21.1-20240808.144430/steps/packRecomp/output.jar)
MIXIN=$(cygpath -m "$(ls /c/gh/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin/0.15.2+mixin.0.8.7/*/sponge-mixin-0.15.2+mixin.0.8.7.jar | head -1)")
OCP="$MC;$MIXIN"
for j in $(find "$LIB/com/mojang" -name '*.jar'); do OCP="$OCP;$j"; done
rm -rf build/options
mkdir -p build/options/classes
javac --release 21 -encoding UTF-8 -proc:none -cp "$OCP" -d build/options/classes $(find options/src -name '*.java')
cp -r options/resources/. build/options/classes/
printf 'Automatic-Module-Name: lny.cinematicoptions
Implementation-Title: LNY Cinematic options
Implementation-Version: 1.0.0
' > build/options/manifest.txt
jar --create --file build/jar/lny-cinematic-options-1.0.0.jar --manifest build/options/manifest.txt -C build/options/classes .
cp -r build/assets/. build/classes/
printf 'Automatic-Module-Name: lny.cinematic\nImplementation-Title: LNY Cinematic\nImplementation-Version: 1.0.0\n' > build/manifest.txt
jar --create --file build/jar/lny-cinematic-1.0.0.jar --manifest build/manifest.txt -C build/classes .
IFS=';'
for profile in $PROFILES; do
   cp build/jar/lny-cinematic-1.0.0.jar build/jar/lny-cinematic-options-1.0.0.jar "$APPDATA/ModrinthApp/profiles/$profile/mods/" \
      || echo "NOT copied into $profile: the jar is in use, is the game running?" >&2
done
ls -la build/jar
