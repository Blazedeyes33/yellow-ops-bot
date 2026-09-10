package com.bachke.goa;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import java.io.IOException;
import java.io.InputStream;

/** GLES 3.0 skinned, textured character draw. One program, one VBO/IBO per model, one albedo. */
public final class SkinnedRenderer {
    static final String VERTEX =
        "#version 300 es\n" +
        "layout(location=0) in vec3 aPos; layout(location=1) in vec3 aNormal; layout(location=2) in vec2 aUv;" +
        "layout(location=3) in vec4 aJoints; layout(location=4) in vec4 aWeights;" +
        "uniform vec4 uBones[" + (BgmModel.MAX_BONES * 3) + "]; uniform mat4 uCamera; uniform mat4 uModel;" +
        "out vec2 vUv; out vec3 vNormal; out float vDepth;" +
        "void main(){ vec4 P=vec4(aPos,1.0); vec3 p=vec3(0.0); vec3 n=vec3(0.0);" +
        " for(int i=0;i<4;i++){ int j=int(aJoints[i])*3; float w=aWeights[i]; vec4 r0=uBones[j], r1=uBones[j+1], r2=uBones[j+2];" +
        "  p+=w*vec3(dot(r0,P),dot(r1,P),dot(r2,P)); n+=w*vec3(dot(r0.xyz,aNormal),dot(r1.xyz,aNormal),dot(r2.xyz,aNormal)); }" +
        " vec4 wp=uModel*vec4(p,1.0); vNormal=mat3(uModel)*n; vUv=aUv; vDepth=max(0.0,-wp.z); gl_Position=uCamera*wp; }";
    static final String FRAGMENT =
        "#version 300 es\nprecision mediump float;" +
        "in vec2 vUv; in vec3 vNormal; in float vDepth; uniform sampler2D uAlbedo; uniform float uNight; uniform vec3 uFog; out vec4 o;" +
        "void main(){ vec3 n=normalize(vNormal); vec3 L=normalize(vec3(-0.45,0.8,0.4));" +
        " float light=max(0.0,dot(n,L)); float hemi=n.y*.5+.5; float rim=pow(1.0-max(0.0,n.z),3.0)*0.12;" +
        " vec3 albedo=texture(uAlbedo,vUv).rgb;" +
        " vec3 day=albedo*(.50+.46*light+.14*hemi)+rim; vec3 night=albedo*vec3(.48,.56,.78)+albedo*light*.32+rim*0.5;" +
        " vec3 c=mix(day,night,uNight); float fog=smoothstep(55.0,145.0,vDepth); o=vec4(mix(c,uFog,fog),1.0); }";

    public static final class Instance {
        public final BgmModel model;
        public final Skeleton skeleton;
        public final CharacterAnimator animator;
        int vbo, ibo;
        /** One albedo per cosmetic skin: 0 = Everyday, 1 = Carnival. */
        final int[] tex = new int[2];
        Instance(BgmModel m, float runClipSpeed) { model = m; skeleton = new Skeleton(m); animator = new CharacterAnimator(m, runClipSpeed); }
    }

    int program, uBones, uCamera, uModel, uAlbedo, uNight, uFog;
    final float[] modelMatrix = new float[16];

    public void create() {
        int vs = compile(GLES30.GL_VERTEX_SHADER, VERTEX), fs = compile(GLES30.GL_FRAGMENT_SHADER, FRAGMENT);
        program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vs); GLES30.glAttachShader(program, fs); GLES30.glLinkProgram(program);
        int[] ok = new int[1]; GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) throw new IllegalStateException("skinned link: " + GLES30.glGetProgramInfoLog(program));
        GLES30.glDeleteShader(vs); GLES30.glDeleteShader(fs);
        uBones = GLES30.glGetUniformLocation(program, "uBones"); uCamera = GLES30.glGetUniformLocation(program, "uCamera");
        uModel = GLES30.glGetUniformLocation(program, "uModel"); uAlbedo = GLES30.glGetUniformLocation(program, "uAlbedo");
        uNight = GLES30.glGetUniformLocation(program, "uNight"); uFog = GLES30.glGetUniformLocation(program, "uFog");
    }

    public Instance upload(AssetManager assets, String modelAsset, String albedoAsset, String carnivalAlbedoAsset, float runClipSpeed) throws IOException {
        BgmModel m;
        try (InputStream in = assets.open(modelAsset)) { m = BgmModel.load(in); }
        Instance inst = new Instance(m, runClipSpeed);
        int[] ids = new int[2]; GLES30.glGenBuffers(2, ids, 0); inst.vbo = ids[0]; inst.ibo = ids[1];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, inst.vbo);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, m.vertexCount * BgmModel.VERTEX_STRIDE, m.vertices, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, inst.ibo);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, m.indexCount * 2, m.indices, GLES30.GL_STATIC_DRAW);
        GLES30.glGenTextures(2, inst.tex, 0);
        loadTexture(assets, albedoAsset, inst.tex[0]);
        loadTexture(assets, carnivalAlbedoAsset, inst.tex[1]);
        return inst;
    }

    static void loadTexture(AssetManager assets, String asset, int id) throws IOException {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id);
        Bitmap bmp;
        try (InputStream in = assets.open(asset)) { bmp = BitmapFactory.decodeStream(in); }
        if (bmp == null) throw new IOException("cannot decode " + asset);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
    }

    /** Draw the instance's current pose at world (x,y,z) facing +yaw with a small roll lean, scaled so its rest height is targetHeight. */
    public void draw(Instance inst, int skin, float[] camera, float x, float y, float z, float yaw, float lean, float targetHeight, float night, float fr, float fg, float fb) {
        inst.skeleton.evaluate(inst.animator.pose);
        float h = inst.model.bboxMax[1] - inst.model.bboxMin[1];
        float s = targetHeight / Math.max(0.01f, h);
        float cy = (float) Math.cos(yaw), sy = (float) Math.sin(yaw), cl = (float) Math.cos(lean), sl = (float) Math.sin(lean);
        // M = T * Ry(yaw) * Rz(lean) * S, column-major
        modelMatrix[0] = cy * cl * s; modelMatrix[1] = sl * s; modelMatrix[2] = -sy * cl * s; modelMatrix[3] = 0;
        modelMatrix[4] = -cy * sl * s; modelMatrix[5] = cl * s; modelMatrix[6] = sy * sl * s; modelMatrix[7] = 0;
        modelMatrix[8] = sy * s; modelMatrix[9] = 0; modelMatrix[10] = cy * s; modelMatrix[11] = 0;
        modelMatrix[12] = x; modelMatrix[13] = y - inst.model.bboxMin[1] * s; modelMatrix[14] = z; modelMatrix[15] = 1;
        GLES30.glUseProgram(program);
        GLES30.glUniformMatrix4fv(uCamera, 1, false, camera, 0);
        GLES30.glUniformMatrix4fv(uModel, 1, false, modelMatrix, 0);
        GLES30.glUniform4fv(uBones, inst.model.boneCount * 3, inst.skeleton.boneRows, 0);
        GLES30.glUniform1f(uNight, night); GLES30.glUniform3f(uFog, fr, fg, fb);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, inst.tex[Math.max(0, Math.min(1, skin))]); GLES30.glUniform1i(uAlbedo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, inst.vbo);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, inst.ibo);
        int st = BgmModel.VERTEX_STRIDE;
        GLES30.glEnableVertexAttribArray(0); GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, st, 0);
        GLES30.glEnableVertexAttribArray(1); GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, st, 12);
        GLES30.glEnableVertexAttribArray(2); GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, st, 24);
        GLES30.glEnableVertexAttribArray(3); GLES30.glVertexAttribPointer(3, 4, GLES30.GL_UNSIGNED_BYTE, false, st, 32);
        GLES30.glEnableVertexAttribArray(4); GLES30.glVertexAttribPointer(4, 4, GLES30.GL_UNSIGNED_BYTE, true, st, 36);
        GLES30.glEnable(GLES30.GL_CULL_FACE); GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, inst.model.indexCount, GLES30.GL_UNSIGNED_SHORT, 0);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        for (int i = 0; i < 5; i++) GLES30.glDisableVertexAttribArray(i);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    static int compile(int type, String src) {
        int s = GLES30.glCreateShader(type); GLES30.glShaderSource(s, src); GLES30.glCompileShader(s);
        int[] ok = new int[1]; GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) throw new IllegalStateException("skinned shader: " + GLES30.glGetShaderInfoLog(s));
        return s;
    }
}
