# Bachke! Goa — where you actually are (10 Sep 2026)

Evidence-only. Every claim below was verified in this session from the files you uploaded: the CP9 APK, three GLBs, and BACHKE-GOA-THREAD.md. Nothing is inferred from concept art or the thread's prose.

## 0. The source code is gone

- `Blazedeyes33/yellow-ops-bot` (this repo) is a Telegram/Notion bot. 14 commits, no `project/`, no commit `4a6152a`, no `release/0.4`, no bundle.
- Your GitHub account exposes two repos; neither is the game.
- Not uploaded / not found anywhere: `project/` source, `CHECKPOINT-CP15.md`, `AGENTS.md`, QA reports, `art/blender/*.py`, `Jojo-Reference-Asset.blend`, `Jojo-Rigged.glb`, `15932.png`, `My_first_board (1).zip`, `Bachke-Goa-CP15.bundle`.
- **What survives of the game code is exactly one artifact: the compiled CP9 APK.** Everything CP10–CP15 built on top of it (Java changes, Blender scripts, Jojo study) exists only as prose in the thread file.

Consequence: CP10–CP15 cannot be "continued". The Java can be partially recovered by decompiling the APK (dex → Java; loses comments/formatting but keeps logic). The Blender work is unrecoverable.

## 1. What the shipped APK really is (decompiled, `logs/dexstrings.txt`)

| Fact | Value |
|---|---|
| File | `BachkeGoaCP9StreetPrototype.apk`, 16,012,571 B, SHA256 `7a7e0160…6143` |
| Package / version | `com.bachke.goa.alpha`, **0.4.0-alpha**, versionCode 4 |
| SDK | min 26, target/compile 35, GLES 2.0 required, portrait |
| Permissions | **none** — genuinely offline, no INTERNET |
| Code | `classes.dex` 84 KB, 33 classes, 430 methods, all `com.bachke.goa.*` |
| Assets | 5 district PNGs (2.5–3.0 MB each) + `rig-matte.png` 1.5 MB + 7 WAVs = **15.9 MB of 16 MB** |

Classes present: `RunnerCore`, `SwipeInput`, `Mesh3D`, `Model3D`, `World3D`, `Stage3D`, `GlRenderer3D`, `Camera3D`, `Shaders3D`, `Hud3D`, `HomeScreen`, `MainActivity`, plus legacy 2D `GameCore`, `Scene`, `Painter`, `AndroidPainter`, `CharacterRig`, `RigAssets`, `Routes`.

Gameplay strings confirm: drag-to-steer, swipe up/down, tap jump, double-tap boost, close-call chain up to 5×, "Goa changes every 20 seconds", five stops (Panjim, Margao, Mapusa, Old Goa, Calangute), day/night, Jojo/Maya, Everyday/Carnival, Crew/Goa Tour/How to Play/Settings screens, personal best saved on device.

### The renderer — this is the number that matters

The whole 3D pipeline is **one shader program**:

- vertex: `aPosition, aNormal, aColor, aWind` + `uCamera, uOffset, uTime` (vertex wind wobble)
- fragment: one hardcoded directional light, hemi term, day/night mix, distance fog, `gl_FragColor = vertex colour`
- **Zero texture calls** in the binary: no `glGenTextures`, `glBindTexture`, `glTexImage2D`, `glActiveTexture`, `sampler2D`, no UV attribute
- `glDrawArrays` only — no index buffers
- one `mat4` uniform — no bone matrices, no `JOINTS/WEIGHTS` attributes

So the 5.5 MB district PNGs and rig-matte are consumed by the Canvas-drawn home screen/HUD, not by the GL scene. The gameplay world is untextured vertex-coloured procedural geometry. **The shipped engine cannot draw a textured or skinned character at all.** That is not a tuning gap; it is a missing subsystem.

## 2. What the three GLBs really are (`logs/budget-report.txt`, `logs/motion-report.txt`)

| File | Identity | Tris / verts | Rig | Anims | Textures |
|---|---|---|---|---|---|
| `Meshy_AI_Tropical_Embrace…texture.glb` | **Jojo**, static T-pose | 114,818 / 67,386 | none | 0 | 3×2048² (albedo 2.9 MB, MR 1.4 MB, normal 2.1 MB) |
| `exportedmodel_1.glb` | **Jojo, rigged** (same vertex count as above) | 114,818 / 67,386 | 66 joints, 51 used | 161 | 3×2048² recompressed (albedo 1.0 MB) |
| `exportedmodel.glb` | **Female character, rigged** (pink ponytail, coral crop hoodie, teal print joggers) | 74,748 / 43,909 | 66 joints, 51 used | 162 | 3×2048² recompressed (albedo 0.9 MB) |

Provenance: generator `THREE.GLTFExporter r183`; UE-style bone names (`pelvis, spine_01, clavicle_l, upperarm_l…`); animation names (`Idle_A, Sword_Regular_Combo, Zombie_Walk_2…`) match a stock universal animation library applied through an auto-rigger. [Likely] Meshy text-to-3D → Meshy auto-rig + animation library → web export. Check the licence terms of that library before shipping.

### Jojo vs reference 15932 (renders in `renders/`)

Present and reading well: bun, beard/moustache, sunglasses on head, cream Goan-print overshirt, grey tee, layered necklaces, bracelets, forearm tattoos, olive rolled chinos, patterned socks, cream/brown/blue sneakers. Face is a stylised adult, not toy-like. **This is the first Jojo that is in the same league as the reference.** It is far beyond CP15.

Gaps against the reference:
- **No backpack.** None. The rear silhouette is a plain shirt. The brief calls the rear view "especially important" — this is the biggest single miss.
- Rigged export's textures are re-JPEG'd at ~⅓ the size; shirt print is visibly muddier than the static file.
- Auto-rig hands: fingers splay into a claw during motion (see `jojo-rigged-front.png`, `rear34.png`).
- Sunglasses lens, hair strands and beard are painted-on, single-mesh; fine at gameplay distance, soft in close-up.
- 114k triangles, uint32 indices, 3×2048² textures — a desktop asset, not a mobile one.

### Motion — measured, then watched (never approved from stills)

Forward-kinematics analysis of the actual keyframes, then rear-view renders at 30 fps (`motion/*.gif`, `motion/sheet-*.png`):

| Clip | Loop seam | Planted-foot skate index* | Hip yaw | Hip↔shoulder | Verdict |
|---|---|---|---|---|---|
| `Jog` | 0.3 cm | **0.05** (clean) | 2.8° | co-rotate (+0.97) | Cleanest loop; feet plant. Upright, stiff arms (elbow 97–116° only), almost no torso drive. Reads as a jog, not a chase. |
| `Run_Anime` | 10.7 cm | 0.30 | 21.8° | **counter-rotate (−0.98)** | Best body mechanics; visible weight transfer and arm drive. Needs seam fix (last→first frame pop). |
| `Run_Female` | 8.5 cm | 0.37 | 36.3° | counter-rotate (−1.00) | Good mechanics, seam issue, one foot never reaches floor. |
| `Sprint` | 0.0 cm | 0.78 | 3.2° | co-rotate | Foot slides during contact. Not usable as-is. |
| `Jump_Start / Jump_air / Jump_Land` | — | — | — | — | All three exist, 1.7 s / 3.1 s / 1.6 s; `Jump_Land` has 0.5 m pelvis compression — believable squash. Too slow for a runner; need time-scaling ~1.6×. |
| `Slide_Start / Slide / Slide_Exit` | — | — | — | — | Full set exists. `Slide_Exit` ends 1.7 m from where `Slide` sits — needs root correction on blend. |
| `Dodge_left / Dodge_right` | 22 cm | — | 29° | — | Exist. 0.83 s. Usable as lane-change layers after seam blend. |
| `Hit_Knockback`, `Dizzy`, `Idle_ShakeOff`, `Roll`, `Land_Three_Point` | — | — | — | — | Stumble/recovery vocabulary exists. |

\*std/|mean| of forward foot velocity while planted; <0.1 = no skate.

All clips are in-place (root travel 0) — correct for a runner; `_RM` variants carry root motion if wanted.

What the renders show that numbers don't:
- **No secondary motion anywhere.** Bun, ponytail, shirt hem, hood are rigid. Only `head_leaf` exists; there is no hair/cloth chain in the rig.
- Hands claw on both characters (auto-weights on 15 finger bones each).
- Shirt and joggers deform acceptably at hip/knee at gameplay distance. Shoulder junction is fine.
- No foot skating visible at 30 fps in `Jog`; feet clearly plant and push.

Nothing here is approved. `Jog` + `Run_Anime` are both candidate bases; neither is shippable without edits.

### Mobile budget (per character)

- Vertex buffer 3.5 MB + index 1.4 MB (Jojo) — target ≤ 12k tris, 16-bit indices.
- 3×2048² textures = 50 MB GPU-resident uncompressed — target 1×1024² albedo + 1×1024² normal in ASTC/ETC2 → ≈ 2.7 MB.
- 66 bones × mat4 = 264 vec4 uniforms > GLES2 guaranteed 128. Must drop unused finger chains (51 used) and pack 3×4, or go GLES 3.0 with UBO. Recommend GLES 3.0 (min SDK 26 already guarantees it).
- 161 animations ≈ 3.6 MB keyframes; a runner needs ~14 clips.

## 3. Scorecard against release scope

| Requirement | Status | Evidence |
|---|---|---|
| Offline Android APK, no dashboard | **Shipped** (0.4.0-alpha, no permissions) | manifest |
| Drag steer / swipe / tap / double-tap boost | **Shipped** | dex strings |
| Close-call scoring, 5× chain | **Shipped** | dex strings |
| Levels every 20 s, gentle 1–3, ramp from 4 | [Likely] shipped — thread says CP10 added level-4 onset; APK is CP9-labelled but version 0.4 with `LEVEL_SECONDS`; needs decompile to confirm the 60 s gate |
| 5 Goan districts, day/night | **Shipped as untextured vertex-colour blockouts** (CP7 art) | shader has no textures |
| Environmental motion | **Shipped, minimal** — vertex wind only | shader |
| Jojo model at reference quality | **Asset exists, not integrated.** Missing backpack. | renders |
| Second character (Maya) | **Rigged asset exists**, identity not confirmed against any approved design | renders |
| Two skins each | **Nothing** | — |
| Production run animation | **Library exists; nothing approved** | motion/ |
| Skinned + textured rendering in engine | **Not implemented** — engine has no texture or skinning path | dex |
| Mobile-optimised character | **Not started** | budget |
| Regression suite | **Lost with source** (last evidence: CP13, 289 checks — prose only) | — |
| OnePlus 15 / NVIDIA validation | **None** | — |

## 4. Honest summary

You have: a playable, offline, correctly-scoped prototype engine with primitive graphics, and — new since the thread — a **near-reference Jojo and a rigged second character with a full stock animation library**. The gap between them is a missing renderer subsystem (textures + skinning), a missing backpack, mobile optimisation, hand/secondary-motion fixes, and a lost source tree.

Progress by phase, roughly: mechanics 85 %, product scope 80 %, environment art 25 %, character art 60 %, animation 35 %, engine 45 %, QA 0 % recoverable, device validation 0 %.

## 5. What is in this folder

- `renders/` — actual three.js/WebGL renders of the uploaded GLBs (not concept art): front/rear/rear-¾/face/gameplay for Jojo static, Jojo rigged, female rigged; extracted 2048² albedos.
- `motion/` — rear-view captures at 30 fps as GIF + 12-frame contact sheets for Jog, Run_Anime, Sprint, Jump_Start, Jump_Land, Slide_Start, Dodge_left, Hit_Knockback.
- `logs/` — FK motion metrics, mobile budget, full dex string dump.
- `tools/` — the GLB inspector, FK analyser and headless render harness used to produce all of the above.

Raw GLBs (43 MB) are not committed; you hold the originals. Renders here are desktop SwiftShader output — they prove asset content and motion, not Android performance.

---

## Addendum — progress in this session (CP16 → CP21, all on `claude/bachke-goa-cp15-dev-lxi0t9`)

| Requirement | Was (audit) | Now | Evidence |
|---|---|---|---|
| Source tree | lost | **recovered** from the APK, buildable; 9 decompiler artifacts + 1 dropped-cast bug fixed; opcode audit vs original | CP16, CP18 |
| Regression suite | lost | **59 core + 42 animation checks**, deterministic, `scripts/test.sh` | `qa/*.txt` |
| Level-4 difficulty gate | not in binary | **implemented and tested** | CP16 |
| Skinned + textured rendering | none | **GLES 3.0 skinned path**, BGM format, 23 bones, 4 influences | CP17 |
| Jojo / Maya in game | not integrated | **integrated**, 14k / 12k tris, 1024² albedo, speed-matched run, jump/slide/dodge/hit state machine | CP17 |
| Two skins each | none | **Everyday + Carnival** for both, UV-mask recolour | CP18 |
| Environment | vertex-colour blockouts, grey road | **sky gradient, painted Goa skyline per route (day/night), paved road, leaf-frond palms, roof tiles, plaster** | CP19–20 |
| Encounters | crossing carts only | **carts, crate stacks, barriers, scooters, paired carts**, gated by level | CP21 |
| Camera | runner clipped at street edge | **follows 60 %** | CP21 |
| Desktop proof | none | headless WebGL harness renders the actual renderer's geometry and shaders; caught a link-time shader error | CP19 |

Open, in priority order:
1. **Physical OnePlus 15 test** — first real device evidence. Uninstall 0.4.0, install `Bachke-Goa-0.5.0-dev.apk` (CP21).
2. **Jojo backpack** — needs a regenerated source model with the backpack; pipeline ready (`tools/README.md`).
3. Modelled street props through the same pipeline (facades, cart, scooter, lamp): the step from blockout to production.
4. Run-loop polish: `Run_Anime` 10.7 cm loop seam, residual foot slide; approve only from device capture. No secondary motion (rig has no hair/cloth bones).
5. ASTC/ETC2 texture compression and APK trim after the device check.
6. Animation-library licence check before any store release.

Estimate: mechanics 90 %, scope 90 %, engine 80 %, character art 75 % (backpack missing), animation 55 %, environment 55 %, QA suite restored, device validation 0 %.
