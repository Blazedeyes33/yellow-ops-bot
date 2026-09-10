"""Texture-space segmentation from the rigged mesh: every UV triangle is painted with the label of the
bone group that dominates its vertices. Gives exact garment masks independent of colour."""
import numpy as np, sys, json
from PIL import Image, ImageDraw
from bgm_preview import read
GROUPS={'head':['Head','head','Neck','neck','HeadTop'],'torso':['Spine','spine','Hips','pelvis','Shoulder','clavicle'],'arms':['Arm','ForeArm','Hand','upperarm','lowerarm','hand'],
        'legs':['UpLeg','Leg','thigh','calf'],'feet':['Foot','Toe','foot','ball']}
def group_of(name):
    for g,keys in GROUPS.items():
        if any(k in name for k in keys): return g
    return 'torso' if name=='root' else 'other'
def mask(bgm,size=1024):
    m=read(bgm); V=m['V']; I=m['I'].reshape(-1,3); names=[b['name'] for b in m['bones']]
    # priority: for 'Leg' also matches 'UpLeg'; for 'Hand' avoid matching 'HandIndex' (collapsed anyway)
    bg=[group_of(n) for n in names]
    labels=list(GROUPS.keys())+['other']; img=Image.new('L',(size,size),0); dr=ImageDraw.Draw(img)
    # dominant group per vertex = argmax weight sum per group
    vg=np.zeros(len(V),int)
    for vi in range(len(V)):
        acc={}
        for k in range(4):
            w=V['w'][vi,k]/255.0; g=bg[V['j'][vi,k]]; acc[g]=acc.get(g,0)+w
        vg[vi]=labels.index(max(acc,key=acc.get))
    uv=V['uv']
    for tri in I:
        gl=np.bincount(vg[tri],minlength=len(labels)).argmax()
        pts=[(float(uv[t,0])*size,float(uv[t,1])*size) for t in tri]
        dr.polygon(pts,fill=int(gl)+1,outline=int(gl)+1)
    a=np.asarray(img); print(bgm,{lab:round(float((a==i+1).mean())*100,1) for i,lab in enumerate(labels)}); return a,labels
if __name__=='__main__':
    for tag in ['jojo','maya']:
        a,labels=mask(f'models/{tag}.bgm'); np.save(f'models/{tag}_uvmask.npy',a)
        pal=np.array([[0,0,0],[255,80,80],[80,160,255],[255,220,80],[80,220,120],[200,120,255],[128,128,128]],np.uint8)
        Image.fromarray(pal[np.clip(a,0,6)]).resize((512,512),Image.NEAREST).save(f'textures/{tag}_uvmask.png')
