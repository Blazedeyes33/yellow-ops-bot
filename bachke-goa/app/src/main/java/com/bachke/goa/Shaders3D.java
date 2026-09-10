package com.bachke.goa;

/** World shader (GLSL ES 1.00, runs on the ES 3.0 context). aWind > 0 sways foliage; aWind < 0 marks paving. */
public final class Shaders3D {
    public static final String VERTEX =
        "attribute vec3 aPosition;attribute vec3 aNormal;attribute vec3 aColor;attribute float aWind;" +
        "uniform mat4 uCamera;uniform float uOffset;uniform float uTime;" +
        "varying vec3 vNormal;varying vec3 vColor;varying float vDepth;varying vec3 vWorld;varying float vPaving;" +
        "void main(){vec3 p=aPosition;p.z+=uOffset;float w=max(aWind,0.0);p.x+=sin(uTime*1.6+p.z*.19)*w*.17;p.y+=cos(uTime*1.3+p.x)*w*.08;" +
        "vNormal=aNormal;vColor=aColor;vDepth=max(0.0,-p.z);vWorld=p;vPaving=aWind<0.0?-aWind:0.0;gl_Position=uCamera*vec4(p,1.0);}";
    public static final String FRAGMENT =
        "precision highp float;varying vec3 vNormal;varying vec3 vColor;varying float vDepth;varying vec3 vWorld;varying float vPaving;" +
        "uniform float uNight;uniform vec3 uFog;uniform float uOffset;" +
        "float hash(vec2 q){return fract(sin(dot(q,vec2(127.1,311.7)))*43758.5453);}" +
        "bool vPaging(float m){return m>2.5&&m<3.5;}" +
        "void main(){vec3 n=normalize(vNormal);float light=max(0.0,dot(n,normalize(vec3(-0.45,0.8,0.4))));float hemi=n.y*.5+.5;" +
        "vec3 base=vColor;" +
        "vec3 wl=vec3(vWorld.x,vWorld.y,vWorld.z-uOffset);" +
        "if(vPaving>0.5&&vPaving<1.5){vec2 g=wl.xz*1.6;vec2 cell=floor(g);vec2 f=fract(g);" +
        " float line=smoothstep(0.0,0.06,f.x)*smoothstep(0.0,0.06,f.y)*smoothstep(1.0,0.94,f.x)*smoothstep(1.0,0.94,f.y);" +
        " float tone=0.9+0.2*hash(cell);base=base*mix(0.72,1.0,line)*tone;}" +
        "else if(vPaving>1.5&&vPaving<2.5){float row=wl.y*7.0;float col=(abs(n.x)>0.5?wl.z:wl.x)*3.2+floor(row)*0.5;" +
        " float r=fract(row);float c=fract(col);float edge=smoothstep(0.0,0.18,r)*smoothstep(1.0,0.82,r)*smoothstep(0.0,0.10,c);" +
        " float tone=0.88+0.24*hash(vec2(floor(row),floor(col)));base=base*mix(0.62,1.0,edge)*tone;}" +
        "else if(vPaging(vPaving)){float grain=hash(floor(wl.xz*9.0)+floor(wl.y*9.0));base=base*(0.94+0.08*grain)*mix(0.86,1.0,smoothstep(0.0,2.2,wl.y));}" +
        "float ao=mix(0.78,1.0,smoothstep(0.0,0.9,vWorld.y));" +
        "vec3 day=base*(.50+.44*light+.14*hemi)*ao;vec3 night=(base*vec3(.46,.54,.76)+base*light*.3)*ao;" +
        "vec3 c=mix(day,night,uNight);float fog=smoothstep(55.0,145.0,vDepth);gl_FragColor=vec4(mix(c,uFog,fog),1.0);}";
}
