#!/bin/bash
# regenerate the ammo box models: geometry, textures, display overrides -> $S/ammo/out
set -e
S="/c/Users/3CDE~1/AppData/Local/Temp/claude/d9fd4f36cf2d7fd1/c696ee7b-3622-4b79-bceb-dd9e524e1792/scratchpad"
PR="/c/Users/Ярослав/AppData/Roaming/ModrinthApp/profiles/Last New Year"
GSON="$(cygpath -w /c/gh/caches/modules-2/files-2.1/com.google.code.gson/gson/2.10.1/b3add478d4382b78ea20b1671390a858002feb6c/gson-2.10.1.jar)"
OUT="$S/ammo/out"
rm -rf "$OUT"
py -3.12 -I "$(cygpath -w "$S/ammo/gen_ammo.py")" "$(cygpath -w "$OUT")" "$(cygpath -w "$PR/tacz/tacz_default_gun/assets/tacz")" "$(cygpath -w "$S/ca_full/assets/create_armorer")" </dev/null
cd "$S/tools"
for f in "$OUT"/paint/*.json; do
  n="$(basename "$f" .json)"
  java -cp "$GSON" AmmoPaint.java "$(cygpath -w "$f")" "$(cygpath -w "$OUT/assets/lny_ammo/textures/ammo/uv/$n.png")"
done
echo painted
