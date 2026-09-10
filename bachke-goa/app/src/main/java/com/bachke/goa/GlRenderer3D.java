package com.bachke.goa;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;
import android.opengl.GLSurfaceView;
import com.bachke.goa.GameCore;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* loaded from: classes.dex */
public final class GlRenderer3D implements GLSurfaceView.Renderer {
    int camera;
    int clock;
    int color;
    final RunnerCore core;
    FloatBuffer dynamic;
    int fog;
    int night;
    int normal;
    int offset;
    int pos;
    int program;
    Stage3D stage;
    final Scene ui;
    int wind;
    int width = 480;
    int height = 960;
    int[] buffers = new int[6];

    final AssetManager assets;
    SkinnedRenderer skinned;
    BackdropRenderer backdrop;
    SkinnedRenderer.Instance[] characters = new SkinnedRenderer.Instance[2];
    long lastNanos;

    public GlRenderer3D(RunnerCore runnerCore, Scene scene, AssetManager assetManager) {
        this.core = runnerCore;
        this.ui = scene;
        this.assets = assetManager;
    }

    private int shader(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glCompileShader(glCreateShader);
        int[] iArr = new int[1];
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return glCreateShader;
        }
        throw new IllegalStateException(GLES20.glGetShaderInfoLog(glCreateShader));
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        int shader = shader(35633, Shaders3D.VERTEX);
        int shader2 = shader(35632, Shaders3D.FRAGMENT);
        this.program = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.program, shader);
        GLES20.glAttachShader(this.program, shader2);
        GLES20.glLinkProgram(this.program);
        int[] iArr = new int[1];
        GLES20.glGetProgramiv(this.program, 35714, iArr, 0);
        if (iArr[0] == 0) {
            throw new IllegalStateException(GLES20.glGetProgramInfoLog(this.program));
        }
        GLES20.glDeleteShader(shader);
        GLES20.glDeleteShader(shader2);
        this.pos = GLES20.glGetAttribLocation(this.program, "aPosition");
        this.normal = GLES20.glGetAttribLocation(this.program, "aNormal");
        this.color = GLES20.glGetAttribLocation(this.program, "aColor");
        this.wind = GLES20.glGetAttribLocation(this.program, "aWind");
        this.camera = GLES20.glGetUniformLocation(this.program, "uCamera");
        this.offset = GLES20.glGetUniformLocation(this.program, "uOffset");
        this.clock = GLES20.glGetUniformLocation(this.program, "uTime");
        this.night = GLES20.glGetUniformLocation(this.program, "uNight");
        this.fog = GLES20.glGetUniformLocation(this.program, "uFog");
        if (this.stage == null) {
            this.stage = new Stage3D();
        }
        GLES20.glGenBuffers(6, this.buffers, 0);
        for (int i = 0; i < 5; i++) {
            Mesh3D mesh3D = this.stage.worlds[i];
            FloatBuffer asFloatBuffer = ByteBuffer.allocateDirect(mesh3D.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            asFloatBuffer.put(mesh3D.data, 0, mesh3D.size).flip();
            GLES20.glBindBuffer(34962, this.buffers[i]);
            GLES20.glBufferData(34962, mesh3D.size * 4, asFloatBuffer, 35044);
        }
        GLES20.glEnable(2929);
        GLES20.glDisable(2884);
        try {
            this.backdrop = new BackdropRenderer(this.assets);
            this.backdrop.create();
        } catch (Exception e) {
            Log.e("Bachke", "backdrop unavailable", e);
            this.backdrop = null;
        }
        try {
            this.skinned = new SkinnedRenderer();
            this.skinned.create();
            this.characters[0] = this.skinned.upload(this.assets, "jojo.bgm", "jojo_albedo.png", "jojo_carnival_albedo.png", CharacterAnimator.JOJO_RUN_CLIP_SPEED);
            this.characters[1] = this.skinned.upload(this.assets, "maya.bgm", "maya_albedo.png", "maya_carnival_albedo.png", CharacterAnimator.MAYA_RUN_CLIP_SPEED);
            this.stage.skinnedCharacter = true;
        } catch (Exception e) {
            // Fall back to the procedural CP9 character rather than crash; logged for QA.
            Log.e("Bachke", "skinned character unavailable, using procedural fallback", e);
            this.skinned = null;
            this.stage.skinnedCharacter = false;
        }
        this.lastNanos = 0L;
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        this.width = i;
        this.height = i2;
        GLES20.glViewport(0, 0, i, i2);
    }

    private void draw(int i, int i2, float f) {
        GLES20.glUniform1f(this.offset, f);
        GLES20.glBindBuffer(34962, i);
        int[] iArr = {this.pos, this.normal, this.color, this.wind};
        int[] iArr2 = {3, 3, 3, 1};
        int[] iArr3 = {0, 12, 24, 36};
        for (int i3 = 0; i3 < 4; i3++) {
            GLES20.glEnableVertexAttribArray(iArr[i3]);
            GLES20.glVertexAttribPointer(iArr[i3], iArr2[i3], 5126, false, 40, iArr3[i3]);
        }
        GLES20.glDrawArrays(4, 0, i2);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onDrawFrame(GL10 gl10) {
        synchronized (this.core) {
            boolean z = this.core.state == GameCore.State.MENU;
            int route = z ? 0 : this.core.route();
            float f = this.ui.night ? 1.0f : 0.0f;
            float f2 = f == 1.0f ? 0.075f : 0.53f;
            float f3 = f == 1.0f ? 0.13f : 0.78f;
            float f4 = f == 1.0f ? 0.24f : 0.83f;
            GLES20.glClearColor(f2, f3, f4, 1.0f);
            GLES20.glClear(16640);
            float[] cameraMatrix = Camera3D.matrix((double) this.width / this.height, z, z ? 0.0d : this.core.renderX());
            if (this.backdrop != null) {
                this.backdrop.draw(route, cameraMatrix, this.ui.night, z ? this.ui.ambience * 0.6d : this.core.renderDistance(), f2, f3, f4);
            }
            GLES20.glUseProgram(this.program);
            GLES20.glUniformMatrix4fv(this.camera, 1, false, cameraMatrix, 0);
            GLES20.glUniform1f(this.clock, (float) this.ui.ambience);
            GLES20.glUniform1f(this.night, f);
            GLES20.glUniform3f(this.fog, f2, f3, f4);
            draw(this.buffers[route], this.stage.worlds[route].vertices(), (float) ((z ? this.ui.ambience * 0.6d : this.core.renderDistance()) % 28.0d));
            this.stage.frame(this.core, (this.ui.character * 2) + this.ui.skin, this.ui.ambience, z);
            Mesh3D mesh3D = this.stage.actors;
            if (this.dynamic == null || this.dynamic.capacity() < mesh3D.size) {
                this.dynamic = ByteBuffer.allocateDirect(mesh3D.data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            }
            this.dynamic.clear();
            this.dynamic.put(mesh3D.data, 0, mesh3D.size).flip();
            GLES20.glBindBuffer(34962, this.buffers[5]);
            GLES20.glBufferData(34962, mesh3D.size * 4, this.dynamic, 35048);
            draw(this.buffers[5], mesh3D.vertices(), 0.0f);
            if (this.skinned != null) {
                long now = System.nanoTime();
                double dt = this.lastNanos == 0L ? 0.016d : Math.min(0.05d, (now - this.lastNanos) / 1.0E9d);
                this.lastNanos = now;
                int which = Math.max(0, Math.min(1, this.ui.character));
                SkinnedRenderer.Instance inst = this.characters[which];
                inst.animator.update(this.core, z, dt);
                float px = z ? 0.0f : (float) this.core.renderX();
                float py = z ? 0.0f : (float) this.core.renderY();
                float yaw = z ? (float) ((Math.sin(this.ui.ambience * 0.6d) * 0.22d) + Math.PI) : 0.0f;
                float lean = z ? 0.0f : (float) Math.max(-0.18d, Math.min(0.18d, (this.core.x - this.core.target()) * 0.15d));
                this.skinned.draw(inst, this.ui.skin, cameraMatrix, px, py, 0.0f, yaw, -lean, 1.85f, f, f2, f3, f4);
            }
        }
    }
}
