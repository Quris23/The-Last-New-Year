#!/bin/bash
# regenerate the Stone implant: model + animations + texture + mask + gun pack files, into the lny_octo_tilt mod's resources
set -e
S="/c/Users/3CDE~1/AppData/Local/Temp/claude/d9fd4f36cf2d7fd1/c696ee7b-3622-4b79-bceb-dd9e524e1792/scratchpad"
PR="/c/Users/Ярослав/AppData/Roaming/ModrinthApp/profiles/Last New Year"
GSON="$(cygpath -w /c/gh/caches/modules-2/files-2.1/com.google.code.gson/gson/2.10.1/b3add478d4382b78ea20b1671390a858002feb6c/gson-2.10.1.jar)"
OUT="$S/implant/out"
CA="$S/ca_full/assets/create_armorer"
cd "$S/implant"
py -3.12 -I -c "import sys; sys.path.insert(0, '.'); import gen_stone; gen_stone.main()" "$(cygpath -w "$CA/geo_models/gun/shotgun_db_stone_geo.json")" "$(cygpath -w "$OUT")" "$(cygpath -w "$S/implant/stone_rgba.raw")" "$(cygpath -w "$PR/kubejs/data/create_armorer/data/guns/shotgun_db_stone_data.json")" "$(cygpath -w "$CA/display/guns/shotgun_db_stone_display.json")" </dev/null
T="$OUT/assets/lny_gun_implants/textures/gun/uv"
cd "$S/tools"
java -cp "$GSON" TexPaint.java "$(cygpath -w "$CA/textures/gun/uv/shotgun_db_stone.png")" "$(cygpath -w "$OUT/paint_stone.json")" "$(cygpath -w "$T/stone.png")"
java -cp "$GSON" MaskPaint.java "$(cygpath -w "$T/stone.png")" "$(cygpath -w "$OUT/paint_stone.json")" "$(cygpath -w "$T/stone_parts.png")"
cp -r "$OUT/assets/lny_gun_implants/." /c/wsp/src/octotilt/resources/assets/lny_gun_implants/
cp -r "$OUT/data/lny_gun_implants/." /c/wsp/src/octotilt/resources/data/lny_gun_implants/
echo "stone installed"
