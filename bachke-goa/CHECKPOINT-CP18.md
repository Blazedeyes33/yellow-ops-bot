# CP18 — two cosmetic skins per character, camera aspect fix, decompile audit (2026-09-10)

## Done
- **Camera aspect ratio bug fixed.** The original bytecode converts width and height to double before dividing; jadx dropped both casts, so the CP16/CP17 rebuild divided two ints (portrait → 0 → infinite projection). `GlRenderer3D` now uses `(double) width / height` at both call sites. CP17's APK would have shown nothing but sky and HUD; do not test it.
- **Decompile fidelity audit** (`tools/` notes): per-method arithmetic/conversion opcode histograms of the original `classes.dex` vs the rebuilt one, 452 methods. After the fix, the only differences in unedited classes are javac re-encodings (add/sub with negated operand, cmpl/cmpg swaps, 2addr forms, constant folding of `(double) 8`). No other dropped casts.
- **Skins (approved scope: two per character, cosmetic only).** Everyday = Meshy albedo. Carnival = garment recolour following the CP10 palette: Jojo saffron print overshirt + plum trousers; Maya plum hoodie + gold pattern on teal joggers. Recolour is masked by **UV-space bone-group segmentation** (`tools/uvmask.py`: every UV triangle labelled by dominant bone group → head/torso/arms/legs/feet), then by colour distance within the region. Head/skin/hair/shoes untouched by construction. Renders: `qa/cp18/board-skins.png`.
- `SkinnedRenderer.Instance` holds one albedo per skin; `GlRenderer3D` binds `ui.skin`. Home-screen Everyday/Carnival buttons already existed and now change the 3D character.
- Tests 52 + 42 pass. APK 26,417,279 bytes, SHA256 `7b9f4b8b…9b8f`.

## Pending
- Environment quality pass (painted panorama backdrop prototype in progress; preview harness `tests/ExportWorld.java` + `tools/gameplay.html`).
- Texture compression (four 1024² PNGs ≈ 9.5 MB in APK).
- Jojo backpack (source asset), device test.
