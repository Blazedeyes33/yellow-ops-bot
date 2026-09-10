package com.bachke.goa;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Painted Goa skyline behind the 3D street. Each route's panorama asset (day on top, night below) is drawn
 * on a far billboard across the road end and, optionally, on low side walls beyond the buildings.
 * The texture scrolls with run distance so the skyline drifts. Only the current route's texture is resident.
 */
public final class BackdropRenderer {
    static final String VERTEX =
        "#version 300 es\nlayout(location=0) in vec3 aPos; layout(location=1) in vec2 aUv; uniform mat4 uCamera; out vec2 vUv; out float vDepth;" +
        "void main(){ vUv=aUv; vDepth=max(0.0,-aPos.z); gl_Position=uCamera*vec4(aPos,1.0); }";
    static final String FRAGMENT =
        "#version 300 es\nprecision mediump float; in vec2 vUv; in float vDepth; uniform sampler2D uTex; uniform float uNightRow; uniform float uScroll; uniform vec3 uFog; uniform vec3 uFogParams; uniform vec3 uSkyTop; out vec4 o;" +
        "void main(){ vec2 uv=vec2(fract(vUv.x+uScroll), uNightRow+clamp(vUv.y,0.0,1.0)*0.5); vec3 c=texture(uTex,uv).rgb;" +
        " float fog=smoothstep(uFogParams.x,uFogParams.y,vDepth)*uFogParams.z; c=mix(c,uFog,fog);" +
        " float top=smoothstep(0.80,0.99,vUv.y); o=vec4(mix(c,uSkyTop,top),1.0); }";
    /** Full-screen vertical gradient drawn first: horizon colour (fog) to zenith colour. */
    static final String SKY_VERTEX =
        "#version 300 es\nlayout(location=0) in vec2 aPos; out float vY; void main(){ vY=aPos.y*0.5+0.5; gl_Position=vec4(aPos,0.999,1.0); }";
    static final String SKY_FRAGMENT =
        "#version 300 es\nprecision mediump float; in float vY; uniform vec3 uFog; uniform vec3 uSkyTop; out vec4 o;" +
        "void main(){ float t=smoothstep(0.30,0.95,vY); o=vec4(mix(uFog,uSkyTop,t),1.0); }";

    public static final float BILLBOARD_WIDTH = 150f, BILLBOARD_Z = -135f, BILLBOARD_FOG = 0.55f;
    public static final float WALL_X = 24f, WALL_LENGTH = 220f, WALL_HEIGHT = 11f, WALL_Z = -60f, WALL_FOG = 0.85f;
    public boolean walls = false;

    int program, uCamera, uTex, uNightRow, uScroll, uFog, uFogParams, uSkyTop;
    int skyProgram, skyFog, skyTop, skyVbo;
    int vbo, vertexCount;
    int texture = 0, textureRoute = -1;
    final AssetManager assets;

    public BackdropRenderer(AssetManager assets) { this.assets = assets; }

    public void create() {
        int vs = SkinnedRenderer.compile(GLES30.GL_VERTEX_SHADER, VERTEX), fs = SkinnedRenderer.compile(GLES30.GL_FRAGMENT_SHADER, FRAGMENT);
        program = GLES30.glCreateProgram(); GLES30.glAttachShader(program, vs); GLES30.glAttachShader(program, fs); GLES30.glLinkProgram(program);
        int[] ok = new int[1]; GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) throw new IllegalStateException("backdrop link: " + GLES30.glGetProgramInfoLog(program));
        GLES30.glDeleteShader(vs); GLES30.glDeleteShader(fs);
        uCamera = GLES30.glGetUniformLocation(program, "uCamera"); uTex = GLES30.glGetUniformLocation(program, "uTex");
        uNightRow = GLES30.glGetUniformLocation(program, "uNightRow"); uScroll = GLES30.glGetUniformLocation(program, "uScroll");
        uFog = GLES30.glGetUniformLocation(program, "uFog"); uFogParams = GLES30.glGetUniformLocation(program, "uFogParams");
        uSkyTop = GLES30.glGetUniformLocation(program, "uSkyTop");
        int svs = SkinnedRenderer.compile(GLES30.GL_VERTEX_SHADER, SKY_VERTEX), sfs = SkinnedRenderer.compile(GLES30.GL_FRAGMENT_SHADER, SKY_FRAGMENT);
        skyProgram = GLES30.glCreateProgram(); GLES30.glAttachShader(skyProgram, svs); GLES30.glAttachShader(skyProgram, sfs); GLES30.glLinkProgram(skyProgram);
        GLES30.glGetProgramiv(skyProgram, GLES30.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) throw new IllegalStateException("sky link: " + GLES30.glGetProgramInfoLog(skyProgram));
        GLES30.glDeleteShader(svs); GLES30.glDeleteShader(sfs);
        skyFog = GLES30.glGetUniformLocation(skyProgram, "uFog"); skyTop = GLES30.glGetUniformLocation(skyProgram, "uSkyTop");
        float[] sq = {-1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1};
        FloatBuffer sfb = ByteBuffer.allocateDirect(sq.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(); sfb.put(sq).flip();
        int[] sid = new int[1]; GLES30.glGenBuffers(1, sid, 0); skyVbo = sid[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, skyVbo); GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, sq.length * 4, sfb, GLES30.GL_STATIC_DRAW);
        // geometry: billboard (6 verts) + two walls (12 verts); each vertex x y z u v
        float bw = BILLBOARD_WIDTH, bh = bw / 6f, bz = BILLBOARD_Z, by = -1f;
        float[] q = new float[18 * 5]; int n = 0;
        n = quad(q, n, -bw / 2, by, bz, bw / 2, by, bz, bw / 2, by + bh, bz, -bw / 2, by + bh, bz, 0f, 0.5f);
        float z0 = WALL_Z + WALL_LENGTH / 2, z1 = WALL_Z - WALL_LENGTH / 2;
        n = quad(q, n, -WALL_X, -0.5f, z0, -WALL_X, -0.5f, z1, -WALL_X, WALL_HEIGHT - 0.5f, z1, -WALL_X, WALL_HEIGHT - 0.5f, z0, 0f, 3f);
        n = quad(q, n, WALL_X, -0.5f, z1, WALL_X, -0.5f, z0, WALL_X, WALL_HEIGHT - 0.5f, z0, WALL_X, WALL_HEIGHT - 0.5f, z1, 0f, 3f);
        vertexCount = n / 5;
        FloatBuffer fb = ByteBuffer.allocateDirect(n * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(); fb.put(q, 0, n).flip();
        int[] ids = new int[1]; GLES30.glGenBuffers(1, ids, 0); vbo = ids[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo); GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, n * 4, fb, GLES30.GL_STATIC_DRAW);
        texture = 0; textureRoute = -1;
    }

    /** Two triangles with u from u0..u1 across the first edge and v 0..1 (wall v scaled to 0.55 so the sky stays low). */
    static int quad(float[] q, int n, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float u0, float u1) {
        boolean wall = ax == bx; float vTop = wall ? 0.55f : 1f;
        float[][] v = {{ax, ay, az, u0, 0}, {bx, by, bz, u1, 0}, {cx, cy, cz, u1, vTop}, {dx, dy, dz, u0, vTop}};
        int[] order = {0, 1, 2, 0, 2, 3};
        for (int i : order) { System.arraycopy(v[i], 0, q, n, 5); n += 5; }
        return n;
    }

    void ensureTexture(int route) throws IOException {
        if (route == textureRoute && texture != 0) return;
        if (texture != 0) { GLES30.glDeleteTextures(1, new int[]{texture}, 0); texture = 0; }
        int[] t = new int[1]; GLES30.glGenTextures(1, t, 0); texture = t[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
        Bitmap bmp;
        try (InputStream in = assets.open(Routes.ASSETS[route])) { bmp = BitmapFactory.decodeStream(in); }
        if (bmp == null) throw new IOException("cannot decode " + Routes.ASSETS[route]);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        textureRoute = route;
    }

    /** Draw before the world so depth writes from geometry occlude it. */
    public void draw(int route, float[] camera, boolean night, double distance, float fr, float fg, float fb) {
        float tr = night ? 0.02f : 0.16f, tg = night ? 0.04f : 0.42f, tb = night ? 0.12f : 0.80f; // zenith colour
        GLES30.glUseProgram(skyProgram);
        GLES30.glUniform3f(skyFog, fr, fg, fb); GLES30.glUniform3f(skyTop, tr, tg, tb);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, skyVbo);
        GLES30.glEnableVertexAttribArray(0); GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, 0);
        GLES30.glDepthMask(false); GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6); GLES30.glDepthMask(true);
        GLES30.glDisableVertexAttribArray(0);
        try { ensureTexture(route); } catch (IOException e) { return; }
        GLES30.glUseProgram(program);
        GLES30.glUniform3f(uSkyTop, tr, tg, tb);
        GLES30.glUniformMatrix4fv(uCamera, 1, false, camera, 0);
        GLES30.glUniform1f(uNightRow, night ? 0.0f : 0.5f);
        GLES30.glUniform1f(uScroll, (float) ((distance / 220.0) % 1.0));
        GLES30.glUniform3f(uFog, fr, fg, fb);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture); GLES30.glUniform1i(uTex, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo);
        GLES30.glEnableVertexAttribArray(0); GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 20, 0);
        GLES30.glEnableVertexAttribArray(1); GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 20, 12);
        GLES30.glDepthMask(false);
        GLES30.glUniform3f(uFogParams, 80f, 150f, BILLBOARD_FOG);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
        if (walls) { GLES30.glUniform3f(uFogParams, 60f, 140f, WALL_FOG); GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 6, 12); }
        GLES30.glDepthMask(true);
        GLES30.glDisableVertexAttribArray(0); GLES30.glDisableVertexAttribArray(1);
    }
}
