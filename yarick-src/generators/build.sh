#!/bin/bash
# regenerate the Crane implant: model + animations + texture
set -e
S="/c/Users/3CDE~1/AppData/Local/Temp/claude/d9fd4f36cf2d7fd1/c696ee7b-3622-4b79-bceb-dd9e524e1792/scratchpad"
GSON="$(cygpath -w /c/gh/caches/modules-2/files-2.1/com.google.code.gson/gson/2.10.1/b3add478d4382b78ea20b1671390a858002feb6c/gson-2.10.1.jar)"
OUT="$S/implant/out"
cd "$S/implant"
py -3.12 -I gen_crane.py "$(cygpath -w "$S/ca_full/assets/create_armorer/geo_models/gun/rifle_assult_crane_geo.json")" "$(cygpath -w "$OUT")" "$(cygpath -w "$S/implant/crane_rgba.raw")"
mkdir -p "$OUT/assets/lny_gun_implants/textures/gun/uv"
cd "$S/tools" && java -cp "$GSON" TexPaint.java "$(cygpath -w "$S/ca_full/assets/create_armorer/textures/gun/uv/rifle_assult_crane.png")" "$(cygpath -w "$OUT/paint.json")" "$(cygpath -w "$OUT/assets/lny_gun_implants/textures/gun/uv/crane.png")"
# part mask for recolouring, then gun display/data/index/lang, all into the lny_octo_tilt mod's resources
cd "$S/tools" && java -cp "$GSON" MaskPaint.java "$(cygpath -w "$OUT/assets/lny_gun_implants/textures/gun/uv/crane.png")" "$(cygpath -w "$OUT/paint.json")" "$(cygpath -w "$OUT/assets/lny_gun_implants/textures/gun/uv/crane_parts.png")"
PR="/c/Users/Ярослав/AppData/Roaming/ModrinthApp/profiles/Last New Year"
py -3.12 -I "$(cygpath -w "$S/implant/pack.py")" "$(cygpath -w "$OUT")" 'C:\wsp\src\octotilt\resources' "$(cygpath -w "$PR/kubejs/data/create_armorer/data/guns/rifle_assult_crane_data.json")" </dev/null
