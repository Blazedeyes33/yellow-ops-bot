# Shared toolchain locations. Override with environment variables.
: "${ANDROID_HOME:=$HOME/android-sdk}"
: "${PLATFORM:=35}"
: "${BUILD_TOOLS:=35.0.1}"
ANDROID_JAR="$ANDROID_HOME/platforms/android-$PLATFORM/android.jar"
BT="$ANDROID_HOME/build-tools/$BUILD_TOOLS"
for f in "$ANDROID_JAR" "$BT/aapt2" "$BT/d8" "$BT/zipalign" "$BT/apksigner"; do
  [ -e "$f" ] || { echo "missing toolchain file: $f (set ANDROID_HOME)"; exit 2; }
done
