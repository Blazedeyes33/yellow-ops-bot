"""Cosmetic skin textures by garment recolour. Pixels are assigned to the nearest reference colour
(hand-picked from k-means clusters); matched garment pixels get a hue/sat/val remap that preserves
shading and print detail. Skin, hair and accessories are excluded by not being in the target set."""
from PIL import Image; import numpy as np, colorsys, sys
def rgb2hsv(a):
    a=a/255.0; mx=a.max(2); mn=a.min(2); d=mx-mn+1e-9
    h=np.zeros_like(mx); r,g,b=a[...,0],a[...,1],a[...,2]
    m=(mx==r); h[m]=((g-b)/d)[m]%6; m=(mx==g); h[m]=((b-r)/d)[m]+2; m=(mx==b); h[m]=((r-g)/d)[m]+4
    h=h/6.0; s=d/(mx+1e-9); return h,s,mx
def hsv2rgb(h,s,v):
    i=np.floor(h*6).astype(int)%6; f=h*6-np.floor(h*6); p=v*(1-s); q=v*(1-f*s); t=v*(1-(1-f)*s)
    out=np.zeros(h.shape+(3,)); 
    for k,(rr,gg,bb) in enumerate([(v,t,p),(q,v,p),(p,v,t),(p,q,v),(t,p,v),(v,p,q)]):
        m=i==k; out[m]=np.stack([rr[m],gg[m],bb[m]],1)
    return (out*255).clip(0,255).astype(np.uint8)
def recolor(src,dst,rules,protect=(),pthr=40):
    im=np.asarray(Image.open(src).convert('RGB')).astype(np.float32); out=im.copy()
    h,s,v=rgb2hsv(im)
    keep=np.zeros(im.shape[:2],bool)
    for c in protect: keep|=((im-np.array(c,np.float32))**2).sum(2)<pthr*pthr
    for refs,thr,(dh,smul,vmul,newhue) in rules:
        d=np.min(np.stack([((im-np.array(c,np.float32))**2).sum(2) for c in refs]),0); m=(d<thr*thr)&~keep
        hh=h.copy(); 
        if newhue is not None: hh[m]=newhue
        else: hh[m]=(h[m]+dh)%1.0
        ss=np.clip(s*smul,0,1); vv=np.clip(v*vmul,0,1)
        rgb=hsv2rgb(hh,ss,vv); out[m]=rgb[m]
        print(f"  rule {refs[0]} -> {int(m.sum())} px ({100*m.mean():.1f}%)")
    Image.fromarray(out.astype(np.uint8)).save(dst); print("wrote",dst)
# Jojo Carnival (CP10 approved palette): saffron print shirt, plum trousers. Skin/hair/shoes protected.
SKIN_J=[(195,123,80),(166,98,64),(140,80,50),(220,150,105)]
recolor('models/jojo_albedo.png','models/jojo_carnival_albedo.png',[
  ([(209,196,164),(238,223,186),(181,173,144),(153,146,124)],42,(0,1.9,0.98,0.09)),        # cream shirt -> saffron, keep print
  ([(84,69,54),(60,52,38),(100,88,66),(70,66,48),(45,42,30)],30,(0,1.7,1.10,0.83)),         # olive trousers (all shades) -> plum
],protect=SKIN_J+[(45,35,24),(30,22,15)],pthr=36)
# Maya Carnival: plum hoodie, gold pattern on teal joggers. Skin, lips, hair protected.
SKIN_M=[(163,119,95),(131,96,69),(190,140,110),(150,100,80)]
recolor('models/maya_albedo.png','models/maya_carnival_albedo.png',[
  ([(216,108,100),(200,90,85),(230,130,120)],40,(0,1.15,0.85,0.86)),                          # coral hoodie -> plum
  ([(213,211,205),(181,185,174)],36,(0,2.2,0.95,0.12)),                                        # white pattern/laces -> gold
],protect=SKIN_M+[(152,42,66),(49,27,29)],pthr=34)
