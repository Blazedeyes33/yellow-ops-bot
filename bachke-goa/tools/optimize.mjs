import { NodeIO } from '@gltf-transform/core';
import { ALL_EXTENSIONS } from '@gltf-transform/extensions';
import { simplify, weld, prune, dedup, textureCompress, resample, join, flatten } from '@gltf-transform/functions';
import { MeshoptSimplifier } from 'meshoptimizer';
import sharp from 'sharp';
const [,, inPath, outPath, targetTris, texSize] = process.argv;
const KEEP = new Set(['Idle_A','Idle_Subtle','Jog','Run_Anime','Sprint','Run_Female','Jump_Start','Jump_air','Jump_Land','Slide_Start','Slide','Slide_Exit','Dodge_left','Dodge_right','Hit_Knockback','Dizzy','Idle_ShakeOff','Land_Three_Point','Victory','Victory_Fist_Pump','Roll','Idle_Hurt','Death_A']);
const io = new NodeIO().registerExtensions(ALL_EXTENSIONS);
const doc = await io.read(inPath);
const root = doc.getRoot();
// 1. drop animations we don't ship
let dropped = 0;
for (const a of root.listAnimations()) if (!KEEP.has(a.getName())) { a.dispose(); dropped++; }
// 2. drop metallicRoughness texture (mobile uses constants), keep albedo + normal
for (const m of root.listMaterials()) { m.setMetallicRoughnessTexture(null); m.setMetallicFactor(0.0); m.setRoughnessFactor(0.85); m.setDoubleSided(false); }
// 3. geometry: weld, simplify to target
const before = root.listMeshes().reduce((n, m) => n + m.listPrimitives().reduce((k, p) => k + p.getIndices().getCount() / 3, 0), 0);
await MeshoptSimplifier.ready;
await doc.transform(weld({ tolerance: 0.0001 }));
const ratio = Math.min(1, (+targetTris) / before);
await doc.transform(simplify({ simplifier: MeshoptSimplifier, ratio, error: 0.01, lockBorder: false }));
// 4. textures
await doc.transform(textureCompress({ encoder: sharp, targetFormat: 'png', resize: [+texSize, +texSize] }));
await doc.transform(resample(), prune(), dedup());
// 5. indices to u16 if possible
for (const m of root.listMeshes()) for (const p of m.listPrimitives()) {
  const idx = p.getIndices(); const arr = idx.getArray(); const vc = p.getAttribute('POSITION').getCount();
  if (vc < 65535) idx.setArray(Uint16Array.from(arr));
}
const after = root.listMeshes().reduce((n, m) => n + m.listPrimitives().reduce((k, p) => k + p.getIndices().getCount() / 3, 0), 0);
const verts = root.listMeshes().reduce((n, m) => n + m.listPrimitives().reduce((k, p) => k + p.getAttribute('POSITION').getCount(), 0), 0);
await io.write(outPath, doc);
const st = (await import('fs')).statSync(outPath);
console.log(JSON.stringify({ inPath, outPath, trisBefore: before, trisAfter: after, verts, ratio: +ratio.toFixed(4), animsKept: root.listAnimations().length, animsDropped: dropped, textures: root.listTextures().map(t => t.getSize()), bytes: st.size }));
