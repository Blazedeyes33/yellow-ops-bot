#!/usr/bin/env bash
# Compiles the pure-Java simulation core and runs the deterministic regression checks. No Android SDK needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
OUT=build/tests; rm -rf "$OUT"; mkdir -p "$OUT"
SRC=app/src/main/java/com/bachke/goa
javac -nowarn -Xlint:none -encoding UTF-8 -d "$OUT" $SRC/RunnerCore.java $SRC/GameCore.java $SRC/Routes.java $SRC/BgmModel.java $SRC/Pose.java $SRC/Skeleton.java $SRC/CharacterAnimator.java tests/com/bachke/goa/CoreTests.java tests/com/bachke/goa/AnimTests.java 2>&1 | grep -v "Picked up" || true
java -cp "$OUT" com.bachke.goa.CoreTests 2>&1 | grep -v "Picked up" | tee qa/core-tests.txt
java -cp "$OUT" com.bachke.goa.AnimTests app/src/main/assets/jojo.bgm app/src/main/assets/maya.bgm 2>&1 | grep -v "Picked up" | tee qa/anim-tests.txt
