#!/usr/bin/env bash
# Compiles the pure-Java simulation core and runs the deterministic regression checks. No Android SDK needed.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
OUT=build/tests; rm -rf "$OUT"; mkdir -p "$OUT"
SRC=app/src/main/java/com/bachke/goa
javac -nowarn -Xlint:none -encoding UTF-8 -d "$OUT" $SRC/RunnerCore.java $SRC/GameCore.java $SRC/Routes.java tests/com/bachke/goa/CoreTests.java 2>&1 | grep -v "Picked up" || true
java -cp "$OUT" com.bachke.goa.CoreTests 2>&1 | grep -v "Picked up" | tee qa/core-tests.txt
