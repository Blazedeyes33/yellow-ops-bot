# CP21 — street encounter variety, camera follow (2026-09-10)

## Gameplay (RunnerCore, all deterministic, level-gated)
- Level 1: crossing carts only (unchanged).  Level 2 adds a **static crate stack** to jump. Level 3 adds a **low barrier** to slide under. Level 4+ adds a **scooter** crossing at 1.9× cart speed. Level 6+ may pair two carts from opposite sides, staggered 9 m so a gap always exists.
- Static obstacles sit at ±1.4 m so at least one third of the street is always open; a three-coin lure marks the open side. Coins carry a lateral position in street mode.
- New `Kind.SCOOTER` with rider/helmet geometry in `Stage3D`; carts, crates, barriers reuse existing geometry.
- Tests: 59 core checks (7 new: no static before L2, no barrier before L3, no scooter before L4, no pairs before L6, kinds actually appear, escape lane always exists) + 24 seeded 4-minute driver courses using jump/slide + 42 animation checks. All pass.

## Camera
- `Camera3D` follows 60 % of the runner's lateral position. Before, a runner at the street edge was half outside the frame (CP9 defect, `qa/cp21/before-camera-follow-runner-clipped.png`). After: `qa/cp21/composite-level2-and-level5.png`.

## Build
APK 26,421,375 bytes, dev-test signature. No device evidence yet.
