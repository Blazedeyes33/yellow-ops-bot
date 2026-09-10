# CP17 — skinned, textured Jojo and Maya in the engine (2026-09-10)

Status: engine now renders real skinned characters; **not device-verified**. No claim on OnePlus performance.

## Done
- **GLES 3.0 skinned character path** (`SkinnedRenderer`, `BgmModel`, `Pose`, `Skeleton`, `CharacterAnimator`). One program, 3×4 bone rows in a `vec4[120]` uniform, 4 influences/vertex, u16 indices, albedo texture with mipmaps, same lighting/fog/night model as the world shader. `MainActivity` requests an ES 3.0 context; the CP9 world shader is unchanged and still runs on it.
- **Runtime model format BGM** (`tools/glb2bgm.py` ↔ `BgmModel.java`): flat little-endian, no JSON at runtime. Finger and leaf joints collapsed into parents (66 → 23 bones per character), which also removes the auto-rig claw hands. Clips resampled at 30 fps, quaternions i16, sign-continuous.
- **Mobile assets** from the uploaded Meshy characters via gltf-transform: Jojo 114,818 → 14,000 tris (12,407 verts), Maya 74,748 → 11,999 tris (10,041 verts); 1024² albedo each; 23 of 161 clips kept. Before/after renders in `qa/cp17/` show no visible loss at gameplay or face distance.
- **Animation state machine**: Idle_A (menu spin) · Run_Anime with playback rate = world speed / measured planted-foot speed (Jojo 5.15, Maya 3.97 u/s) so feet do not skate · Jump_Start → Jump_air → Jump_Land · Slide_Start → Slide → Slide_Exit · Dodge_left/right on swipe · Hit_Knockback on game over · 120 ms smoothstep crossfade on every transition. `RunnerCore` now records the last accepted swipe for the animator.
- `Stage3D.skinnedCharacter` disables the CP9 procedural runner while keeping its ground shadow blob; if model load fails the renderer logs and falls back to the procedural character instead of crashing.
- Tests: 52 core + 42 animation checks pass (`qa/core-tests.txt`, `qa/anim-tests.txt`). Animation checks include: bind-pose locals derived from inverse-bind matrices reproduce the mesh to 0.0000; posed Idle_A keeps a standing envelope; loop seam; state-machine sequences driven by the real `RunnerCore`; quaternion normalisation after blends.
- Independent verification of the runtime bytes: `tools/bgm_preview.py` re-implements the skinning in numpy from the `.bgm` file and renders correctly (`qa/cp17/board-bgm-runtime-path.png`).
- APK: `Bachke-Goa-0.5.0-dev.apk`, 23,041,973 bytes, SHA256 `2b4bc911…c64b7b`. Signed with the throwaway dev key; uninstall 0.4.0 first.

## Measured, not approved
Rear-view motion of the source clips (`qa/cp17/*.gif`, metrics in `maya-motion-metrics.txt`): `Run_Anime` has real hip/shoulder counter-rotation but a 10.7 cm loop pop and variable planted-foot speed (skate std 1.3–1.9 u/s); `Jog` is skate-free but stiff. The game ships `Run_Anime` at speed-matched rate. **Approve only from device capture.** No secondary motion exists (bun, ponytail, hood are rigid — the rig has no hair/cloth chains).

## Known gaps carried forward
1. **Jojo has no backpack.** The Meshy source model omits it; reference 15932 makes the rear silhouette depend on it. Needs a regenerated or sculpted source asset; a procedural box would repeat the rejected primitive approach.
2. Skins: none. Two cosmetic variants per character still required.
3. Environments: CP7 vertex-colour blockouts. Untextured.
4. Textures are PNG (2.5 MB + 2.3 MB in the APK); convert to ETC2/KTX after device check. The 15.9 MB of legacy 2D PNGs for the Canvas home screen are still bundled.
5. Home screen/HUD unchanged (Canvas). Crew screen still shows the old 2D card art.
6. Character scale is normalised to 1.85 m by bounding box; shadow blob and camera framing not yet retuned for the new silhouette.
7. Physical OnePlus 15 test: **pending — this APK is the first one worth installing.**

## Reproduce
`./scripts/test.sh` · `./scripts/build-apk.sh` · pipeline in `tools/README.md`.
