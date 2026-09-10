# CP16 — source recovery, level-4 gate, regression harness (2026-09-10)

Status: source tree restored and buildable. Not a graphics or animation checkpoint. No device test.

## What happened to CP10–CP15
The original repository, Git bundle, Blender scripts and CP15 Jojo study were not present in any reachable location. The only surviving game artifact was the shipped `BachkeGoaCP9StreetPrototype.apk` (0.4.0-alpha, versionCode 4, SHA256 `7a7e0160…6143`). This checkpoint rebuilds the project from that binary.

## Done
- Decompiled `classes.dex` with jadx 1.5.1 into `app/src/main/java/com/bachke/goa` (19 classes, ~3,500 lines). Nine decompiler artifacts fixed by hand (untyped boolean/int temporaries in `Scene`, `RigAssets`, `Stage3D`; a lost loop index in `Scene.obstacle`; a cast error in `MainActivity`; an uninitialised temporary in `RunnerCore.step`). Original comments and local names are lost; logic is preserved.
- Reconstructed `AndroidManifest.xml`, launcher icon vector and all 13 assets from the APK.
- **Level-4 difficulty gate implemented** (`RunnerCore`): street speed 6 m/s, cart speed 0.7 m/s and 4.2 s spawn interval are constant through levels 1–3 and ramp continuously from 60 s to caps of 9 m/s, 1.5 m/s and 2.6 s. First cart at 1.6 s, 26 m. Lane mode row gap and hazard-per-row count also gated at level 4. The shipped 0.4.0 binary ramped from second one; this rule was not in it.
- Close-call scoring block reconstructed so a close call, a wide pass and a lane-mode clear each emit exactly one CLEAR event.
- `scripts/test.sh` + `tests/com/bachke/goa/CoreTests.java`: 52 deterministic checks (state machine, jump apex/airtime, slide, steer clamps, level-4 gate and caps, continuity at the gate, first-cart timing, collision, close-call band, 24 seeded 4-minute driver courses, seed determinism, 60/165 Hz agreement, Routes). All pass: `qa/core-tests.txt`.
- `scripts/build-apk.sh`: Gradle-free build (aapt2 → javac → d8 → zip → zipalign → apksigner). Produces `build/Bachke-Goa-0.5.0-dev.apk`, 16,012,883 bytes, v2+v3 signed, 20 entries mirroring the original.
- Manifest now declares GLES 3.0 (`0x00030000`) and version 0.5.0-dev / code 5 (approved: minSdk 26 guarantees ES 3.0).

## Signing
The original alpha signing identity is lost. `signing/bachke-dev-test.p12` (git-ignored) is a fresh throwaway dev-test key. **Installing 0.5.0 over the old 0.4.0 alpha will fail signature check; uninstall the old build first.** Never reuse this key for a store release.

## Not done / unchanged
- Renderer is still the CP9 single vertex-colour shader. No textures, no skinning. The code still requests an ES2 context; ES3 features are not yet used.
- Environments remain CP7 vertex-colour blockouts.
- Jojo / Maya GLBs are not integrated. See `status-2026-09-10/STATUS.md` for asset analysis.
- Home screen and HUD unchanged.
- No emulator or physical-device run. No performance claim.

## Reproduce
```
export ANDROID_HOME=/path/to/sdk   # needs platforms;android-35 and build-tools;35.0.1
./scripts/test.sh
./scripts/build-apk.sh
```
