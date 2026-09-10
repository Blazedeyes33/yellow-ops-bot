# CP20 — environment pass 2: palms, roof tiles, plaster; composite preview (2026-09-10)

- `World3D.palm`: curved segmented trunk, coconut cluster, eight leaf-shaped fronds (swelling then pointed, alternating leaflet tones, double-sided) with wind sway. Replaces the CP7 green arcs.
- World shader material ids via the wind attribute: −1 paving (tile grid), −2 roof tiles (row/column pattern with per-tile tone), −3 plaster (grain + vertical gradient). Roofs and house bodies tagged in `World3D`.
- `tests/ExportWorld` now drives the runner with the regression driver so snapshots are live valid game states; `tools/gameplay.html` composites the numpy re-skin of the runtime character (`--char`) at the runner position with the `SkinnedRenderer` lighting formula.
- Boards: `qa/cp20/all-routes.png` (5 routes, day/night), `qa/cp20/composite-with-character.png`.
- Tests 52 + 42 pass. APK 26,421,375 bytes. No device evidence.

Remaining near-field weakness: box facades and simple props. Step change requires modelled props through the Meshy → GLB → BGM pipeline (static path: bones = 1).
