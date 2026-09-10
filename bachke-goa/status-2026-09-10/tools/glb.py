import struct, json, sys, os
for p in sys.argv[1:]:
    d=open(p,'rb').read()
    magic,ver,length=struct.unpack('<4sII',d[:12])
    off=12; js=None; binlen=0
    while off<len(d):
        clen,ctype=struct.unpack('<I4s',d[off:off+8])
        if ctype==b'JSON': js=json.loads(d[off+8:off+8+clen])
        else: binlen=clen
        off+=8+clen
    print("="*70); print(os.path.basename(p)); print("magic",magic,"ver",ver,"declared",length,"actual",len(d),"bin",binlen)
    g=js
    print("generator:", g.get('asset',{}).get('generator'))
    print("meshes:",len(g.get('meshes',[])),"nodes:",len(g.get('nodes',[])),"materials:",len(g.get('materials',[])),
          "images:",len(g.get('images',[])),"skins:",len(g.get('skins',[])),"animations:",len(g.get('animations',[])))
    tris=0; prims=0; verts=0
    acc=g.get('accessors',[])
    for m in g.get('meshes',[]):
        for pr in m.get('primitives',[]):
            prims+=1
            if 'indices' in pr: tris+=acc[pr['indices']]['count']//3
            elif 'POSITION' in pr.get('attributes',{}): tris+=acc[pr['attributes']['POSITION']]['count']//3
            if 'POSITION' in pr.get('attributes',{}): verts+=acc[pr['attributes']['POSITION']]['count']
    print("primitives(draw calls min):",prims,"triangles:",tris,"vertices:",verts)
    for i,im in enumerate(g.get('images',[])):
        bv=im.get('bufferView'); print("  image",i,im.get('mimeType'),im.get('name'),"bytes:",g['bufferViews'][bv]['byteLength'] if bv is not None else im.get('uri'))
    for s in g.get('skins',[]): print("  skin joints:",len(s.get('joints',[])))
    for a in g.get('animations',[]): print("  anim:",a.get('name'),"channels:",len(a.get('channels',[])))
    attrs=set()
    for m in g.get('meshes',[]):
        for pr in m.get('primitives',[]): attrs|=set(pr.get('attributes',{}).keys())
    print("attributes:",sorted(attrs))
    print("mesh names:",[m.get('name') for m in g.get('meshes',[])][:12])
