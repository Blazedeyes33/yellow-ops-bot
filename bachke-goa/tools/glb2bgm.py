"""GLB -> BGM (Bachke Goa Model) converter.

BGM is a flat little-endian binary the Android loader reads without JSON.
Finger and leaf joints are collapsed into their parents so the bone count fits
GLES 3.0 vertex-uniform limits and auto-rig claw hands disappear.

Layout (all little-endian):
  char[4]  "BGM1"
  u32 vertexCount, u32 indexCount, u32 boneCount, u32 clipCount
  f32[6]   bbox min xyz, max xyz  (rest pose)
  f32[16]  rootTransform, column-major: product of non-joint ancestor node transforms above the root joint
  vertices: vertexCount x { f32 pos[3], f32 normal[3], f32 uv[2], u8 joints[4], u8 weights[4] }   (40 bytes)
  indices:  indexCount  x u16
  bones:    boneCount x { i16 parent, f32 invBind[16] (column-major), f32 restT[3], f32 restR[4], f32 restS[3], u8 nameLen, name }
  clips:    clipCount x { u8 nameLen, name, u16 frameCount, f32 fps, u8 loop,
                          frameCount x boneCount x { i16 quat[4] (x,y,z,w * 32767), f32 trans[3] } }
"""
import struct, json, sys, re, numpy as np
exec(open('rig.py').read().split('g,b=load')[0])
from motion import quat_mul, quat_rot, slerp, sample  # reuse FK helpers

def run(src, dst, fps=30.0, loops=()):
    g, b = load(src); nodes = g['nodes']; skin = g['skins'][0]; joints = skin['joints']
    name = {i: nodes[i].get('name', 'n%d' % i) for i in range(len(nodes))}
    parent = {}
    for i, nd in enumerate(nodes):
        for c in nd.get('children', []): parent[c] = i
    # ---- decide which joints survive ----
    # UE-style (index_01_l, ball_leaf_l) and Mixamo-style (mixamorigLeftHandIndex1, mixamorigHeadTop_End) finger/leaf joints
    COLLAPSE = re.compile(r'^(index|middle|ring|pinky|thumb)_|_leaf|Hand(Index|Middle|Ring|Pinky|Thumb)\d|_End$')
    keep = [j for j in joints if not COLLAPSE.search(name[j])]
    def survivor(j):
        while j not in keep: j = parent[j]
        return j
    remap = {j: keep.index(survivor(j)) for j in joints}         # old joint index (node id) -> new bone index
    jidx = {j: k for k, j in enumerate(joints)}                   # node id -> old JOINTS_0 index
    old2new = np.array([remap[j] for j in joints], dtype=np.int64)
    # ---- mesh ----
    prim = g['meshes'][0]['primitives'][0]; A = prim['attributes']
    pos = acc(g, b, A['POSITION']).astype(np.float32); nrm = acc(g, b, A['NORMAL']).astype(np.float32)
    uv = acc(g, b, A['TEXCOORD_0']).astype(np.float32); J = acc(g, b, A['JOINTS_0']).astype(np.int64); W = acc(g, b, A['WEIGHTS_0']).astype(np.float64)
    idx = acc(g, b, prim['indices']).astype(np.int64)
    assert len(pos) < 65536, "vertex count exceeds u16 indices; decimate first"
    Jn = old2new[J]
    # merge duplicate joint slots after collapse
    J4 = np.zeros((len(pos), 4), np.int64); W4 = np.zeros((len(pos), 4))
    for v in range(len(pos)):
        acc_ = {}
        for k in range(4):
            if W[v, k] > 0: acc_[Jn[v, k]] = acc_.get(Jn[v, k], 0.0) + W[v, k]
        items = sorted(acc_.items(), key=lambda t: -t[1])[:4]
        s = sum(w for _, w in items) or 1.0
        for k, (jj, w) in enumerate(items): J4[v, k] = jj; W4[v, k] = w / s
    W8 = np.clip(np.round(W4 * 255), 0, 255).astype(np.uint8)
    # fix rounding so weights sum to 255
    for v in range(len(pos)):
        d = 255 - int(W8[v].sum()); W8[v, 0] = np.clip(int(W8[v, 0]) + d, 0, 255)
    # ---- bones ----
    ibm_all = acc(g, b, skin['inverseBindMatrices']).reshape(-1, 16)
    bones = []
    for k, j in enumerate(keep):
        p = parent.get(j); pk = -1
        if p is not None and p in keep: pk = keep.index(p)
        elif p is not None: pk = keep.index(survivor(p)) if p in joints else -1
        bones.append(dict(parent=pk, ibm=ibm_all[jidx[j]].astype(np.float32),
                          t=np.array(nodes[j].get('translation', [0, 0, 0]), np.float32),
                          r=np.array(nodes[j].get('rotation', [0, 0, 0, 1]), np.float32),
                          s=np.array(nodes[j].get('scale', [1, 1, 1]), np.float32), name=name[j]))
    # ---- ancestor transform above root joint ----
    def trs(nd):
        T=np.array(nd.get('translation',[0,0,0]),np.float64); R=np.array(nd.get('rotation',[0,0,0,1]),np.float64); S=np.array(nd.get('scale',[1,1,1]),np.float64)
        x,y,z,w=R; rot=np.array([[1-2*(y*y+z*z),2*(x*y-z*w),2*(x*z+y*w)],[2*(x*y+z*w),1-2*(x*x+z*z),2*(y*z-x*w)],[2*(x*z-y*w),2*(y*z+x*w),1-2*(x*x+y*y)]])
        M=np.eye(4); M[:3,:3]=rot*S; M[:3,3]=T
        if 'matrix' in nd: M=np.array(nd['matrix']).reshape(4,4).T
        return M
    rootT=np.eye(4); anc=[]; p=parent.get(keep[0])
    while p is not None:
        anc.append(name[p]); rootT=trs(nodes[p])@rootT; p=parent.get(p)
    print("root joint ancestors:",anc, "rootTransform:", np.round(rootT,4).tolist() if anc else "identity")
    # ---- clips ----
    clips = []
    for a in g['animations']:
        chans = {}
        tmax = 0.0
        for ch in a['channels']:
            s_ = a['samplers'][ch['sampler']]; n = ch['target']['node']
            if n not in keep: continue
            ti = acc(g, b, s_['input']).astype(np.float64); vo = acc(g, b, s_['output']).astype(np.float64)
            tmax = max(tmax, float(ti[-1])); chans[(n, ch['target']['path'])] = (ti, vo)
        nf = max(2, int(round(tmax * fps)) + 1)
        frames = np.zeros((nf, len(keep), 7), np.float32)
        for f in range(nf):
            t = min(tmax, f / fps)
            for k, j in enumerate(keep):
                r = chans.get((j, 'rotation')); tr = chans.get((j, 'translation'))
                q = sample(r[0], r[1], t, True) if r else bones[k]['r']
                tt = sample(tr[0], tr[1], t, False) if tr else bones[k]['t']
                frames[f, k, :4] = q; frames[f, k, 4:] = tt
        # keep quaternion sign continuous per bone across frames (avoid flips after i16 quantisation)
        for k in range(len(keep)):
            for f in range(1, nf):
                if np.dot(frames[f, k, :4], frames[f - 1, k, :4]) < 0: frames[f, k, :4] *= -1
        clips.append(dict(name=a['name'], nf=nf, frames=frames, loop=a['name'] in loops))
    # ---- write ----
    out = bytearray(); w = out.extend
    w(b'BGM1'); w(struct.pack('<4I', len(pos), len(idx), len(bones), len(clips)))
    w(struct.pack('<6f', *pos.min(0), *pos.max(0)))
    w(rootT.T.astype('<f4').tobytes())  # column-major
    vb = np.zeros(len(pos), dtype=np.dtype([('p', '<f4', 3), ('n', '<f4', 3), ('uv', '<f4', 2), ('j', 'u1', 4), ('w', 'u1', 4)]))
    vb['p'] = pos; vb['n'] = nrm; vb['uv'] = uv; vb['j'] = J4.astype(np.uint8); vb['w'] = W8
    w(vb.tobytes()); w(idx.astype('<u2').tobytes())
    for bn in bones:
        nm = bn['name'].encode(); w(struct.pack('<h', bn['parent'])); w(bn['ibm'].astype('<f4').tobytes())
        w(bn['t'].astype('<f4').tobytes()); w(bn['r'].astype('<f4').tobytes()); w(bn['s'].astype('<f4').tobytes()); w(struct.pack('<B', len(nm))); w(nm)
    for c in clips:
        nm = c['name'].encode(); w(struct.pack('<B', len(nm))); w(nm); w(struct.pack('<Hf?', c['nf'], fps, c['loop']))
        q = np.clip(np.round(c['frames'][:, :, :4] * 32767), -32767, 32767).astype('<i2'); t = c['frames'][:, :, 4:].astype('<f4')
        inter = np.zeros((c['nf'], len(bones)), dtype=np.dtype([('q', '<i2', 4), ('t', '<f4', 3)])); inter['q'] = q; inter['t'] = t
        w(inter.tobytes())
    open(dst, 'wb').write(out)
    # albedo texture -> PNG beside the model
    mat=g['materials'][0]; ti=mat['pbrMetallicRoughness']['baseColorTexture']['index']; im=g['images'][g['textures'][ti]['source']]
    bv=g['bufferViews'][im['bufferView']]; data=b[bv.get('byteOffset',0):bv.get('byteOffset',0)+bv['byteLength']]
    tex=dst.rsplit('.',1)[0]+'_albedo.'+('png' if im['mimeType'].endswith('png') else 'jpg'); open(tex,'wb').write(data); print("albedo ->",tex,len(data),"bytes",im['mimeType'])
    report = dict(src=src, dst=dst, bytes=len(out), vertices=len(pos), triangles=len(idx) // 3, bones=len(bones), bonesDropped=len(joints) - len(bones),
                  boneNames=[bn['name'] for bn in bones], clips=[(c['name'], c['nf'], c['loop']) for c in clips],
                  maxInfluences=int((W8 > 0).sum(1).max()))
    print(json.dumps(report, indent=1)); return report

if __name__ == '__main__':
    LOOPS = ('Idle_A', 'Idle_Subtle', 'Jog', 'Run_Anime', 'Sprint', 'Run_Female', 'Jump_air', 'Slide', 'Dizzy', 'Idle_Hurt')
    run(sys.argv[1], sys.argv[2], loops=LOOPS)
