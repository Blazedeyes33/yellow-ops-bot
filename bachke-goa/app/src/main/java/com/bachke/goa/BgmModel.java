package com.bachke.goa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Bachke Goa Model: a flat little-endian skinned mesh + skeleton + animation clips.
 * Produced offline by tools/glb2bgm.py from an optimised GLB. No JSON at runtime.
 * Pure Java so the animation path is testable on the desktop.
 */
public final class BgmModel {
    public static final int VERTEX_STRIDE = 40; // pos3f normal3f uv2f joints4u8 weights4u8
    public static final int MAX_BONES = 40;

    public static final class Bone {
        public int parent;
        public final float[] inverseBind = new float[16];
        public final float[] restT = new float[3];
        public final float[] restR = new float[4];
        public final float[] restS = new float[3];
        public String name;
    }

    public static final class Clip {
        public String name;
        public int frames;
        public float fps;
        public boolean loop;
        /** frames * bones * 4, quaternion xyzw scaled by 32767 */
        public short[] q;
        /** frames * bones * 3 */
        public float[] t;
        public float duration() { return (frames - 1) / fps; }
    }

    public int vertexCount, indexCount, boneCount, clipCount;
    public final float[] bboxMin = new float[3], bboxMax = new float[3];
    public final float[] rootTransform = new float[16];
    public ByteBuffer vertices;   // direct, native order, VERTEX_STRIDE bytes per vertex
    public ByteBuffer indices;    // direct, u16
    public Bone[] bones;
    public Clip[] clips;
    public final Map<String, Clip> clipByName = new HashMap<>();

    public Clip clip(String name) {
        Clip c = clipByName.get(name);
        if (c == null) throw new IllegalArgumentException("missing clip " + name);
        return c;
    }

    public static BgmModel load(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1 << 20);
        byte[] buf = new byte[1 << 16];
        for (int n; (n = in.read(buf)) > 0; ) bos.write(buf, 0, n);
        return parse(bos.toByteArray());
    }

    public static BgmModel parse(byte[] bytes) throws IOException {
        ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (b.get() != 'B' || b.get() != 'G' || b.get() != 'M' || b.get() != '1') throw new IOException("not a BGM1 file");
        BgmModel m = new BgmModel();
        m.vertexCount = b.getInt(); m.indexCount = b.getInt(); m.boneCount = b.getInt(); m.clipCount = b.getInt();
        if (m.boneCount > MAX_BONES) throw new IOException("bone count " + m.boneCount + " exceeds MAX_BONES " + MAX_BONES);
        if (m.vertexCount > 65535) throw new IOException("vertex count exceeds u16 indices");
        for (int i = 0; i < 3; i++) m.bboxMin[i] = b.getFloat();
        for (int i = 0; i < 3; i++) m.bboxMax[i] = b.getFloat();
        for (int i = 0; i < 16; i++) m.rootTransform[i] = b.getFloat();
        int vbytes = m.vertexCount * VERTEX_STRIDE;
        m.vertices = ByteBuffer.allocateDirect(vbytes).order(ByteOrder.nativeOrder());
        m.vertices.put(bytes, b.position(), vbytes).flip(); b.position(b.position() + vbytes);
        int ibytes = m.indexCount * 2;
        m.indices = ByteBuffer.allocateDirect(ibytes).order(ByteOrder.nativeOrder());
        // indices are little-endian u16 in the file; copy as shorts so native order is honoured
        for (int i = 0; i < m.indexCount; i++) m.indices.putShort(b.getShort());
        m.indices.flip();
        m.bones = new Bone[m.boneCount];
        for (int i = 0; i < m.boneCount; i++) {
            Bone bn = new Bone();
            bn.parent = b.getShort();
            for (int k = 0; k < 16; k++) bn.inverseBind[k] = b.getFloat();
            for (int k = 0; k < 3; k++) bn.restT[k] = b.getFloat();
            for (int k = 0; k < 4; k++) bn.restR[k] = b.getFloat();
            for (int k = 0; k < 3; k++) bn.restS[k] = b.getFloat();
            bn.name = readName(b);
            if (bn.parent >= i) throw new IOException("bone " + i + " parent " + bn.parent + " not before child");
            m.bones[i] = bn;
        }
        m.clips = new Clip[m.clipCount];
        for (int i = 0; i < m.clipCount; i++) {
            Clip c = new Clip();
            c.name = readName(b);
            c.frames = b.getShort() & 0xffff;
            c.fps = b.getFloat();
            c.loop = b.get() != 0;
            int n = c.frames * m.boneCount;
            c.q = new short[n * 4]; c.t = new float[n * 3];
            for (int k = 0; k < n; k++) {
                for (int e = 0; e < 4; e++) c.q[k * 4 + e] = b.getShort();
                for (int e = 0; e < 3; e++) c.t[k * 3 + e] = b.getFloat();
            }
            m.clips[i] = c;
            m.clipByName.put(c.name, c);
        }
        return m;
    }

    private static String readName(ByteBuffer b) {
        int n = b.get() & 0xff;
        byte[] s = new byte[n];
        b.get(s);
        return new String(s, StandardCharsets.UTF_8);
    }
}
