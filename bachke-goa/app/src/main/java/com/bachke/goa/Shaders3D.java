package com.bachke.goa;

/** World shader (GLSL ES 1.00, runs on the ES 3.0 context). aWind > 0 sways foliage; aWind < 0 marks paving. */
public final class Shaders3D {
    public static final String VERTEX =
        "attribute vec3 aPosition;attribute vec3 aNormal;attribute vec3 aColor;attribute float aWind;" +
        "uniform mat4 uCamera;uniform float uOffset;uniform float uTime;" +
        "varying vec3 vNormal;varying vec3 vColor;varying float vDepth;varying vec3 vWorld;varying float vPaving;" +
        "void main(){vec3 p=aPosition;p.z+=uOffset;float w=max(aWind,0.0);p.x+=sin(uTime*1.6+p.z*.19)*w*.17;p.y+=cos(uTime*1.3+p.x)*w*.08;" +
        "vNormal=aNormal;vColor=aColor;vDepth=max(0.0,-p.z);vWorld=p;vPaving=aWind<0.0?1.0:0.0;gl_Position=uCamera*vec4(p,1.0);}";
    public static final String FRAGMENT =
        "precision highp float;varying vec3 vNormal;varying vec3 vColor;varying float vDepth;varying vec3 vWorld;varying float vPaving;" +
        "uniform float uNight;uniform vec3 uFog;uniform float uOffset;" +
        "float hash(vec2 q){return fract(sin(dot(q,vec2(127.1,311.7)))*43758.5453);}" +
        "void main(){vec3 n=normalize(vNormal);float light=max(0.0,dot(n,normalize(vec3(-0.45,0.8,0.4))));float hemi=n.y*.5+.5;" +
        "vec3 base=vColor;" +
        "if(vPaving>0.5){vec2 g=vec2(vWorld.x*1.6,(vWorld.z-uOffset)*1.6);vec2 cell=floor(g);vec2 f=fract(g);" +
        " float line=smoothstep(0.0,0.06,f.x)*smoothstep(0.0,0.06,f.y)*smoothstep(1.0,0.94,f.x)*smoothstep(1.0,0.94,f.y);" +
        " float tone=0.9+0.2*hash(cell);base=base*mix(0.72,1.0,line)*tone;}" +
        "float ao=mix(0.78,1.0,smoothstep(0.0,0.9,vWorld.y))*(1.0-0.22*vPaving*0.0);" +
        "vec3 day=base*(.50+.44*light+.14*hemi)*ao;vec3 night=(base*vec3(.46,.54,.76)+base*light*.3)*ao;" +
        "vec3 c=mix(day,night,uNight);float fog=smoothstep(55.0,145.0,vDepth);gl_FragColor=vec4(mix(c,uFog,fog),1.0);}";
}
