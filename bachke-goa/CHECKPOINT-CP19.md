# CP19 — environment pass 1: sky, painted Goa skyline, paved road (2026-09-10)

## Done
- **Desktop preview harness for the actual 3D scene** (`tests/ExportWorld.java` → `tools/gameplay.html` + `tools/render-gameplay.js`): exports the exact `World3D`/`Stage3D` vertex streams, `Camera3D` matrix and `Shaders3D` sources, renders them in headless WebGL. This is the CP9 renderer's output, not concept art. It caught a real defect: a vertex/fragment precision mismatch on `uOffset` that would have failed program link on the phone (fixed: fragment shader is now `highp`).
- **`BackdropRenderer`** (GLES 3.0): full-screen sky gradient (horizon = fog colour → zenith, day/night), then the route's panorama (existing `panjim.png` … `calangute.png`, day on top half, night below) on a 150 × 25 m billboard at z = −135 with distance scroll, distance fog and a top edge that fades into the sky. Only the current route's texture is resident. Optional side walls exist but default off (no visible gain).
- **World shader**: `aWind < 0` now marks paving; road and pavements get a procedural tile grid with per-tile tone variation, plus ground-contact darkening on all geometry. Foliage sway unchanged for `aWind > 0`.
- Before/after: `qa/cp19/before-cp9-street.png` vs `qa/cp19/after-backdrop-sky-paving.png` (all five routes, day and night).
- Tests 52 + 42 pass. APK 26,421,375 bytes.

## Still weak (next)
- CP7 procedural houses and palm "arcs" remain the near-field art. Next pass: material ids for roof tiles/plaster, leaf-shaped fronds, facade variation. Real modelled props via the Meshy pipeline would be the step change (see STATUS asks).
- Character not yet composited in the desktop preview; device capture still the only proof of the full frame.
