import struct,json,sys,numpy as np
def load(p):
    d=open(p,'rb').read(); off=12; js=None; bin_=b''
    while off<len(d):
        clen,ct=struct.unpack('<I4s',d[off:off+8])
        c=d[off+8:off+8+clen]
        if ct==b'JSON': js=json.loads(c)
        else: bin_=c
        off+=8+clen
    return js,bin_
CT={5120:'i1',5121:'u1',5122:'i2',5123:'u2',5125:'u4',5126:'f4'}
NC={'SCALAR':1,'VEC2':2,'VEC3':3,'VEC4':4,'MAT4':16}
def acc(g,b,i):
    a=g['accessors'][i]; bv=g['bufferViews'][a['bufferView']]
    n=NC[a['type']]; dt=np.dtype('<'+CT[a['componentType']])
    o=bv.get('byteOffset',0)+a.get('byteOffset',0)
    stride=bv.get('byteStride',0); packed=n*dt.itemsize
    if stride and stride!=packed:
        raw=np.frombuffer(b,dtype=np.uint8,count=stride*(a['count']-1)+packed,offset=o)
        arr=np.lib.stride_tricks.as_strided(raw,shape=(a['count'],packed),strides=(stride,1)).copy().view(dt).reshape(a['count'],n)
        return arr if n>1 else arr[:,0]
    arr=np.frombuffer(b,dtype=dt,count=a['count']*n,offset=o)
    return arr.reshape(a['count'],n) if n>1 else arr
g,b=load(sys.argv[1])
nodes=g['nodes']; skin=g['skins'][0]; joints=skin['joints']
name={i:nodes[i].get('name','node%d'%i) for i in range(len(nodes))}
parent={}
for i,nd in enumerate(nodes):
    for c in nd.get('children',[]): parent[c]=i
print("JOINT COUNT:",len(joints))
print("ROOT joint:",name[joints[0]])
print("Joints:",[name[j] for j in joints])
