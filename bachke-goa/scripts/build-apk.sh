#!/usr/bin/env bash
# Builds a signed test APK without Gradle: aapt2 -> javac -> d8 -> zip -> zipalign -> apksigner.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
source scripts/env.sh
KEYSTORE="${KEYSTORE:-signing/bachke-dev-test.p12}"; KEYPASS="${KEYPASS:-bachkedev}"; ALIAS="${KEYALIAS:-bachke-dev}"
OUT=build/apk; rm -rf "$OUT"; mkdir -p "$OUT/classes" "$OUT/res" "$OUT/gen"
echo "[1/6] aapt2 compile+link"
"$BT/aapt2" compile --dir app/src/main/res -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$ANDROID_JAR" --manifest app/src/main/AndroidManifest.xml \
  --java "$OUT/gen" "$OUT/res.zip" --min-sdk-version 26 --target-sdk-version 35
echo "[2/6] javac"
find app/src/main/java "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -nowarn -source 8 -target 8 -Xlint:none -encoding UTF-8 -bootclasspath "$ANDROID_JAR" -d "$OUT/classes" @"$OUT/sources.txt" 2>&1 | grep -v "Picked up" || true
[ -f "$OUT/classes/com/bachke/goa/MainActivity.class" ] || { echo "javac failed"; exit 1; }
echo "[3/6] d8"
"$BT/d8" --release --min-api 26 --lib "$ANDROID_JAR" --output "$OUT" $(find "$OUT/classes" -name '*.class') 2>&1 | grep -v "Picked up" || true
[ -f "$OUT/classes.dex" ] || { echo "d8 failed"; exit 1; }
echo "[4/6] package"
cp "$OUT/base.apk" "$OUT/unaligned.apk"
( cd "$OUT" && zip -q -u unaligned.apk classes.dex )
# assets stored uncompressed (-0): SoundPool needs openFd on uncompressed audio; PNG/GLB gain nothing from deflate
( cd app/src/main && zip -q -0 -r -u "$ROOT/$OUT/unaligned.apk" assets )
echo "[5/6] zipalign"
"$BT/zipalign" -p -f 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"
echo "[6/6] apksigner"
VER=$(grep -o 'versionName="[^"]*"' app/src/main/AndroidManifest.xml | cut -d'"' -f2)
FINAL="build/Bachke-Goa-$VER.apk"
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass "pass:$KEYPASS" --ks-key-alias "$ALIAS" --key-pass "pass:$KEYPASS" --v2-signing-enabled true --v3-signing-enabled true --out "$FINAL" "$OUT/aligned.apk" 2>&1 | grep -v "Picked up" || true
"$BT/apksigner" verify --print-certs "$FINAL" 2>&1 | grep -v "Picked up" | head -3
ls -l "$FINAL"; sha256sum "$FINAL"
