package com.bachke.goa;

/** A local-space skeleton pose: per-bone rotation quaternion (xyzw) and translation. */
public final class Pose {
    public final float[] q;
    public final float[] t;
    public final int bones;

    public Pose(int bones) { this.bones = bones; q = new float[bones * 4]; t = new float[bones * 3]; }

    public void set(Pose o) { System.arraycopy(o.q, 0, q, 0, q.length); System.arraycopy(o.t, 0, t, 0, t.length); }

    /** Sample a clip at time seconds (clamped or wrapped by clip.loop) into this pose. */
    public void sample(BgmModel.Clip c, float time) {
        float dur = c.duration();
        float ft;
        if (c.loop) { ft = dur <= 0 ? 0 : time - dur * (float) Math.floor(time / dur); }
        else ft = Math.max(0, Math.min(dur, time));
        float f = ft * c.fps;
        int f0 = (int) Math.floor(f);
        if (f0 >= c.frames - 1) { f0 = c.frames - 1; }
        int f1 = c.loop ? (f0 + 1) % c.frames : Math.min(c.frames - 1, f0 + 1);
        float u = f - f0;
        int b0 = f0 * bones, b1 = f1 * bones;
        for (int i = 0; i < bones; i++) {
            int a = (b0 + i) * 4, b = (b1 + i) * 4;
            float ax = c.q[a] * INV, ay = c.q[a + 1] * INV, az = c.q[a + 2] * INV, aw = c.q[a + 3] * INV;
            float bx = c.q[b] * INV, by = c.q[b + 1] * INV, bz = c.q[b + 2] * INV, bw = c.q[b + 3] * INV;
            if (ax * bx + ay * by + az * bz + aw * bw < 0) { bx = -bx; by = -by; bz = -bz; bw = -bw; }
            float x = ax + (bx - ax) * u, y = ay + (by - ay) * u, z = az + (bz - az) * u, w = aw + (bw - aw) * u;
            float inv = 1f / (float) Math.sqrt(x * x + y * y + z * z + w * w);
            int o = i * 4; q[o] = x * inv; q[o + 1] = y * inv; q[o + 2] = z * inv; q[o + 3] = w * inv;
            int ta = (b0 + i) * 3, tb = (b1 + i) * 3, to = i * 3;
            t[to] = c.t[ta] + (c.t[tb] - c.t[ta]) * u;
            t[to + 1] = c.t[ta + 1] + (c.t[tb + 1] - c.t[ta + 1]) * u;
            t[to + 2] = c.t[ta + 2] + (c.t[tb + 2] - c.t[ta + 2]) * u;
        }
    }

    /** this = lerp(a, b, w) with shortest-arc quaternion nlerp. */
    public void blend(Pose a, Pose b, float w) {
        for (int i = 0; i < bones; i++) {
            int o = i * 4;
            float ax = a.q[o], ay = a.q[o + 1], az = a.q[o + 2], aw = a.q[o + 3];
            float bx = b.q[o], by = b.q[o + 1], bz = b.q[o + 2], bw = b.q[o + 3];
            if (ax * bx + ay * by + az * bz + aw * bw < 0) { bx = -bx; by = -by; bz = -bz; bw = -bw; }
            float x = ax + (bx - ax) * w, y = ay + (by - ay) * w, z = az + (bz - az) * w, ww = aw + (bw - aw) * w;
            float inv = 1f / (float) Math.sqrt(x * x + y * y + z * z + ww * ww);
            q[o] = x * inv; q[o + 1] = y * inv; q[o + 2] = z * inv; q[o + 3] = ww * inv;
            int to = i * 3;
            for (int k = 0; k < 3; k++) t[to + k] = a.t[to + k] + (b.t[to + k] - a.t[to + k]) * w;
        }
    }

    private static final float INV = 1f / 32767f;
}
