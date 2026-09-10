"""Independent re-implementation of the Java BGM runtime path in numpy: parse BGM, sample a clip,
evaluate the skeleton, skin the vertices, and write a static textured GLB for visual review.
If this render looks right, the bytes the phone loads are right."""
import struct, sys, json, numpy as np
def read(path):
    d=open(path,'rb').read(); o=0
    assert d[:4]==b'BGM1'; o=4
    vc,ic,bc,cc=struct.unpack_from('<4I',d,o); o+=16
    bbox=struct.unpack_from('<6f',d,o); o+=24
    rootT=np.array(struct.unpack_from('<16f',d,o),np.float64).reshape(4,4).T; o+=64  # column-major -> matrix
    vt=np.dtype([('p','<f4',3),('n','<f4',3),('uv','<f4',2),('j','u1',4),('w','u1',4)])
    V=np.frombuffer(d,vt,vc,o); o+=vc*40
    I=np.frombuffer(d,'<u2',ic,o); o+=ic*2
    bones=[]
    for i in range(bc):
        parent=struct.unpack_from('<h',d,o)[0]; o+=2
        ibm=np.array(struct.unpack_from('<16f',d,o)).reshape(4,4).T; o+=64
        t=np.array(struct.unpack_from('<3f',d,o)); o+=12; r=np.array(struct.unpack_from('<4f',d,o)); o+=16; s=np.array(struct.unpack_from('<3f',d,o)); o+=12
        n=d[o]; o+=1; name=d[o:o+n].decode(); o+=n
        bones.append(dict(parent=parent,ibm=ibm,t=t,r=r,s=s,name=name))
    clips={}
    for i in range(cc):
        n=d[o]; o+=1; name=d[o:o+n].decode(); o+=n
        nf,fps,loop=struct.unpack_from('<Hf?',d,o); o+=7
        ft=np.dtype([('q','<i2',4),('t','<f4',3)]); F=np.frombuffer(d,ft,nf*bc,o).reshape(nf,bc); o+=nf*bc*20
        clips[name]=dict(nf=nf,fps=fps,loop=loop,F=F)
    assert o==len(d), (o,len(d))
    return dict(V=V,I=I,bones=bones,clips=clips,rootT=rootT,bbox=bbox)
def qmat(q,t,s):
    x,y,z,w=q; M=np.eye(4)
    M[:3,:3]=np.array([[1-2*(y*y+z*z),2*(x*y-z*w),2*(x*z+y*w)],[2*(x*y+z*w),1-2*(x*x+z*z),2*(y*z-x*w)],[2*(x*z-y*w),2*(y*z+x*w),1-2*(x*x+y*y)]])*s; M[:3,3]=t; return M
def skin(m,clip,time):
    c=m['clips'][clip]; f=time*c['fps']; f0=int(f)%c['nf']; f1=(f0+1)%c['nf']; u=f-int(f)
    world=[]; rows=[]
    for i,b in enumerate(m['bones']):
        qa=c['F'][f0,i]['q']/32767.0; qb=c['F'][f1,i]['q']/32767.0
        if np.dot(qa,qb)<0: qb=-qb
        q=qa+(qb-qa)*u; q/=np.linalg.norm(q); t=c['F'][f0,i]['t']*(1-u)+c['F'][f1,i]['t']*u
        L=qmat(q,t,b['s']); W=(m['rootT'] if b['parent']<0 else world[b['parent']])@L; world.append(W); rows.append(W@b['ibm'])
    V=m['V']; P=np.c_[V['p'],np.ones(len(V))]; out=np.zeros((len(V),3)); nrm=np.zeros((len(V),3))
    for k in range(4):
        w=V['w'][:,k]/255.0; j=V['j'][:,k]
        Ms=np.stack([rows[i] for i in range(len(rows))])[j]  # (n,4,4)
        out+=w[:,None]*np.einsum('nij,nj->ni',Ms[:,:3,:],P); nrm+=w[:,None]*np.einsum('nij,nj->ni',Ms[:,:3,:3],V['n'])
    return out.astype(np.float32),(nrm/np.maximum(1e-6,np.linalg.norm(nrm,axis=1))[:,None]).astype(np.float32)
def write_glb(path,pos,nrm,uv,idx,png):
    bufs=[pos.tobytes(),nrm.tobytes(),uv.astype('<f4').tobytes(),idx.astype('<u2').tobytes(),png]
    views=[];off=0
    for b in bufs:
        views.append(dict(buffer=0,byteOffset=off,byteLength=len(b))); off+=len(b); pad=(-len(b))%4; bufs[bufs.index(b)]=b+b'\0'*pad if pad else b; off+=pad
    bin_=b''.join(bufs)
    js=dict(asset=dict(version="2.0",generator="bgm_preview"),scene=0,scenes=[dict(nodes=[0])],nodes=[dict(mesh=0)],
        meshes=[dict(primitives=[dict(attributes=dict(POSITION=0,NORMAL=1,TEXCOORD_0=2),indices=3,material=0)])],
        materials=[dict(pbrMetallicRoughness=dict(baseColorTexture=dict(index=0),metallicFactor=0,roughnessFactor=0.85))],
        textures=[dict(source=0,sampler=0)],samplers=[dict(magFilter=9729,minFilter=9987,wrapS=10497,wrapT=10497)],images=[dict(bufferView=4,mimeType="image/png")],
        accessors=[dict(bufferView=0,componentType=5126,count=len(pos),type="VEC3",min=pos.min(0).tolist(),max=pos.max(0).tolist()),
                   dict(bufferView=1,componentType=5126,count=len(nrm),type="VEC3"),dict(bufferView=2,componentType=5126,count=len(uv),type="VEC2"),
                   dict(bufferView=3,componentType=5123,count=len(idx),type="SCALAR")],
        bufferViews=views,buffers=[dict(byteLength=len(bin_))])
    j=json.dumps(js).encode(); j+=b' '*((-len(j))%4)
    out=b'glTF'+struct.pack('<II',2,12+8+len(j)+8+len(bin_))+struct.pack('<I4s',len(j),b'JSON')+j+struct.pack('<I4s',len(bin_),b'BIN\0')+bin_
    open(path,'wb').write(out)
if __name__=='__main__':
    bgm,png,clip,t,out=sys.argv[1],sys.argv[2],sys.argv[3],float(sys.argv[4]),sys.argv[5]
    m=read(bgm); pos,nrm=skin(m,clip,t); write_glb(out,pos,nrm,m['V']['uv'],m['I'],open(png,'rb').read())
    print(json.dumps(dict(out=out,clip=clip,t=t,verts=len(pos),bbox=[pos.min(0).round(3).tolist(),pos.max(0).round(3).tolist()])))
