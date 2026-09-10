import struct,json,sys,numpy as np
exec(open('rig.py').read().split('g,b=load')[0])
def quat_mul(a,b):
    x1,y1,z1,w1=a; x2,y2,z2,w2=b
    return np.array([w1*x2+x1*w2+y1*z2-z1*y2, w1*y2-x1*z2+y1*w2+z1*x2, w1*z2+x1*y2-y1*x2+z1*w2, w1*w2-x1*x2-y1*y2-z1*z2])
def quat_rot(q,v):
    x,y,z,w=q; u=np.array([x,y,z]); return v+2*np.cross(u,np.cross(u,v)+w*v)
def slerp(a,b,t):
    d=np.dot(a,b)
    if d<0: b=-b; d=-d
    if d>0.9995: r=a+t*(b-a); return r/np.linalg.norm(r)
    th=np.arccos(d); return (np.sin((1-t)*th)*a+np.sin(t*th)*b)/np.sin(th)
def sample(times,vals,t,rot):
    if t<=times[0]: return vals[0]
    if t>=times[-1]: return vals[-1]
    i=np.searchsorted(times,t)-1; u=(t-times[i])/(times[i+1]-times[i]+1e-12)
    return slerp(vals[i],vals[i+1],u) if rot else vals[i]+(vals[i+1]-vals[i])*u

if __name__=="__main__":
  g,b=load(sys.argv[1]); nodes=g['nodes']
  name={i:nodes[i].get('name','n%d'%i) for i in range(len(nodes))}; byname={v:k for k,v in name.items()}
  parent={}
  for i,nd in enumerate(nodes):
      for c in nd.get('children',[]): parent[c]=i
  def world(pose,n):
      T=np.array(pose[n]['t']); R=np.array(pose[n]['r']); S=np.array(pose[n]['s'])
      p=parent.get(n)
      if p is None: return T,R
      pt,pr=world(pose,p)
      return pt+quat_rot(pr,T*S if False else T), quat_mul(pr,R)
  def evaluate(anim,t):
      pose={i:{'t':nodes[i].get('translation',[0,0,0]),'r':nodes[i].get('rotation',[0,0,0,1]),'s':nodes[i].get('scale',[1,1,1])} for i in range(len(nodes))}
      for ch in anim['channels']:
          s=anim['samplers'][ch['sampler']]; tgt=ch['target']
          ti=acc(g,b,s['input']); vo=acc(g,b,s['output'])
          path=tgt['path']; n=tgt['node']
          if path=='rotation': pose[n]['r']=sample(ti,vo,t,True)
          elif path=='translation': pose[n]['t']=sample(ti,vo,t,False)
          elif path=='scale': pose[n]['s']=sample(ti,vo,t,False)
      return pose
  def yaw(q):  # rotation about Y in degrees from forward vector
      f=quat_rot(q,np.array([0,0,1.0])); return np.degrees(np.arctan2(f[0],f[2]))
  def angle(a,b,c):
      v1=a-b; v2=c-b; return np.degrees(np.arccos(np.clip(np.dot(v1,v2)/(np.linalg.norm(v1)*np.linalg.norm(v2)+1e-12),-1,1)))
  want=[a.strip() for a in sys.argv[2].split(',')]
  anims={a['name']:a for a in g['animations']}
  # rest height
  rest=evaluate({'channels':[],'samplers':[]},0)
  hipY=world(rest,byname['pelvis'])[0][1]; footY=world(rest,byname['foot_l'])[0][1]; ballY=world(rest,byname['ball_l'])[0][1]
  print(f"REST: pelvis Y={hipY:.3f}  foot Y={footY:.3f}  ball Y={ballY:.3f}  (units)")
  for w in want:
      if w not in anims: print("MISSING",w); continue
      a=anims[w]
      tmax=max(acc(g,b,a['samplers'][ch['sampler']]['input'])[-1] for ch in a['channels'])
      kf=max(len(acc(g,b,a['samplers'][ch['sampler']]['input'])) for ch in a['channels'])
      N=60; ts=np.linspace(0,tmax,N,endpoint=False)
      rows=[]
      for t in ts:
          P=evaluate(a,t)
          bl=world(P,byname['ball_l'])[0]; br=world(P,byname['ball_r'])[0]
          fl=world(P,byname['foot_l'])[0]; fr=world(P,byname['foot_r'])[0]
          pel=world(P,byname['pelvis']); sp3=world(P,byname['spine_03'])
          ua=world(P,byname['upperarm_r'])[0]; la=world(P,byname['lowerarm_r'])[0]; ha=world(P,byname['hand_r'])[0]
          th=world(P,byname['thigh_r'])[0]; ca=world(P,byname['calf_r'])[0]
          root=world(P,byname['root'])[0]
          rows.append(dict(t=t,bl=bl,br=br,fl=fl,fr=fr,pelY=pel[0][1],pelYaw=yaw(pel[1]),shYaw=yaw(sp3[1]),
                           elbow=angle(ua,la,ha),knee=angle(th,ca,fr),root=root))
      # loop continuity: first vs last sample pose delta on ball bones
      P0=evaluate(a,0); P1=evaluate(a,tmax)
      loopd=max(np.linalg.norm(world(P0,byname[j])[0]-world(P1,byname[j])[0]) for j in ['ball_l','ball_r','hand_l','hand_r','head'])
      minBall=min(min(r['bl'][1],r['br'][1]) for r in rows)
      # contact phases: ball within 3cm of its min
      thr=minBall+0.03
      def contact(side):
          return [r[side][1]<=thr for r in rows]
      cl=contact('bl'); cr=contact('br')
      # planted-foot forward velocity during contact (in-place loop => should be ~constant negative)
      def vel(side,mask):
          vs=[]
          for i in range(N):
              if mask[i] and mask[(i+1)%N]:
                  dz=(rows[(i+1)%N][side][2]-rows[i][side][2]); dt=tmax/N; vs.append(dz/dt)
          return np.array(vs)
      vl=vel('bl',cl); vr=vel('br',cr)
      allv=np.concatenate([vl,vr]) if len(vl)+len(vr) else np.array([0.0])
      elb=[r['elbow'] for r in rows]; kn=[r['knee'] for r in rows]
      pely=[r['pelY'] for r in rows]
      cr_phase=np.corrcoef([r['pelYaw'] for r in rows],[r['shYaw'] for r in rows])[0,1]
      rootmove=np.linalg.norm(rows[-1]['root']-rows[0]['root'])
      print(f"\n## {w}")
      print(f"  duration {tmax:.3f}s  keyframes {kf}  ({kf/tmax:.1f} fps)  channels {len(a['channels'])}  root motion travel {rootmove:.3f}")
      print(f"  loop discontinuity (max joint pos delta first->last) {loopd*100:.1f} cm")
      print(f"  L contact {sum(cl)}/{N} samples ({100*sum(cl)/N:.0f}%)   R contact {sum(cr)}/{N} ({100*sum(cr)/N:.0f}%)   both airborne {sum(1 for i in range(N) if not cl[i] and not cr[i])}/{N}")
      print(f"  min ball height {minBall:.3f} (rest {ballY:.3f})  pelvis bob {max(pely)-min(pely):.3f}")
      if len(allv)>1: print(f"  planted-foot fwd velocity: mean {allv.mean():+.3f} u/s  std {allv.std():.3f}  (skate index std/|mean| = {allv.std()/(abs(allv.mean())+1e-9):.2f})")
      print(f"  R elbow angle range {min(elb):.0f}-{max(elb):.0f} deg   R knee range {min(kn):.0f}-{max(kn):.0f} deg")
      print(f"  pelvis yaw range {max(r['pelYaw'] for r in rows)-min(r['pelYaw'] for r in rows):.1f} deg  shoulder yaw range {max(r['shYaw'] for r in rows)-min(r['shYaw'] for r in rows):.1f} deg  pelvis/shoulder yaw corr {cr_phase:+.2f} (negative = counter-rotation)")
