# Asset pipeline (offline)

Source GLBs (Meshy-generated characters, auto-rigged, stock animation library) are **not** committed (43 MB). Keep them in `art/source-glb/`.

```
npm i @gltf-transform/cli@4.1.1 @gltf-transform/core@4.1.1 @gltf-transform/extensions@4.1.1 @gltf-transform/functions@4.1.1 meshoptimizer@0.22.0 sharp
pip install numpy pillow

node tools/optimize.mjs art/source-glb/jojo-rigged.glb build/jojo-mobile.glb 14000 1024   # decimate, 1024² textures, keep 23 clips
node tools/optimize.mjs art/source-glb/maya-rigged.glb build/maya-mobile.glb 12000 1024
python3 tools/glb2bgm.py build/jojo-mobile.glb app/src/main/assets/jojo.bgm             # collapse finger/leaf bones, resample 30 fps, i16 quats; writes *_albedo.png
python3 tools/glb2bgm.py build/maya-mobile.glb app/src/main/assets/maya.bgm
python3 tools/bgm_preview.py app/src/main/assets/jojo.bgm app/src/main/assets/jojo_albedo.png Run_Anime 0.3 build/preview.glb   # numpy re-skin for visual QA
```

`rig.py` / `motion.py` are the GLB inspector and forward-kinematics motion analyser (foot contact, skate index, hip/shoulder counter-rotation, loop seam). `view.html` + `render.js` are the headless three.js/Playwright render harness used for every render in `qa/`.

BGM format is documented at the top of `glb2bgm.py` and mirrored by `BgmModel.java`.
