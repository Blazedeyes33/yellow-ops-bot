package com.bachke.goa;

/* loaded from: classes.dex */
public final class Shaders3D {
    public static final String FRAGMENT = "precision mediump float;varying vec3 vNormal;varying vec3 vColor;varying float vDepth;uniform float uNight;uniform vec3 uFog;void main(){vec3 n=normalize(vNormal);float light=max(0.0,dot(n,normalize(vec3(-0.45,0.8,0.4))));float hemi=n.y*.5+.5;vec3 day=vColor*(.48+.42*light+.13*hemi);vec3 night=vColor*vec3(.48,.56,.75)+vColor*light*.3;vec3 c=mix(day,night,uNight);float fog=smoothstep(55.0,145.0,vDepth);gl_FragColor=vec4(mix(c,uFog,fog),1.0);}";
    public static final String VERTEX = "attribute vec3 aPosition;attribute vec3 aNormal;attribute vec3 aColor;attribute float aWind;uniform mat4 uCamera;uniform float uOffset;uniform float uTime;varying vec3 vNormal;varying vec3 vColor;varying float vDepth;void main(){vec3 p=aPosition;p.z+=uOffset;p.x+=sin(uTime*1.6+p.z*.19)*aWind*.17;p.y+=cos(uTime*1.3+p.x)*aWind*.08;vNormal=aNormal;vColor=aColor;vDepth=max(0.0,-p.z);gl_Position=uCamera*vec4(p,1.0);}";
}
