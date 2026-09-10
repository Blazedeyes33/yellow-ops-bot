package com.bachke.goa;

/**
 * Turns a local Pose into GPU skinning matrices. Column-major 4x4 math kept local so this
 * class has no Android dependency. Output is packed as 3 rows of vec4 per bone (3x4), which is
 * what the vertex shader consumes: 40 bones * 3 = 120 vec4 uniforms.
 */
public final class Skeleton {
    public final BgmModel model;
    public final float[] world;      // bones * 16
    public final float[] boneRows;   // bones * 12
    private final float[] local = new float[16];
    private final float[] tmp = new float[16];

    public Skeleton(BgmModel model) {
        this.model = model;
        world = new float[model.boneCount * 16];
        boneRows = new float[model.boneCount * 12];
    }

    public void evaluate(Pose pose) {
        int n = model.boneCount;
        for (int i = 0; i < n; i++) {
            BgmModel.Bone b = model.bones[i];
            compose(local, pose.q, i * 4, pose.t, i * 3, b.restS);
            int p = b.parent;
            if (p < 0) multiply(world, i * 16, model.rootTransform, 0, local, 0);
            else multiply(world, i * 16, world, p * 16, local, 0);
        }
        for (int i = 0; i < n; i++) {
            multiply(tmp, 0, world, i * 16, model.bones[i].inverseBind, 0);
            // column-major m[col*4+row] -> rows
            int o = i * 12;
            for (int r = 0; r < 3; r++) {
                boneRows[o + r * 4] = tmp[r];
                boneRows[o + r * 4 + 1] = tmp[4 + r];
                boneRows[o + r * 4 + 2] = tmp[8 + r];
                boneRows[o + r * 4 + 3] = tmp[12 + r];
            }
        }
    }

    /** World position of a bone origin after evaluate(); used by tests and debug overlays. */
    public void bonePosition(int bone, float[] out) {
        out[0] = world[bone * 16 + 12]; out[1] = world[bone * 16 + 13]; out[2] = world[bone * 16 + 14];
    }

    static void compose(float[] m, float[] q, int qo, float[] t, int to, float[] s) {
        float x = q[qo], y = q[qo + 1], z = q[qo + 2], w = q[qo + 3];
        float xx = x * x, yy = y * y, zz = z * z, xy = x * y, xz = x * z, yz = y * z, wx = w * x, wy = w * y, wz = w * z;
        m[0] = (1 - 2 * (yy + zz)) * s[0]; m[1] = (2 * (xy + wz)) * s[0]; m[2] = (2 * (xz - wy)) * s[0]; m[3] = 0;
        m[4] = (2 * (xy - wz)) * s[1]; m[5] = (1 - 2 * (xx + zz)) * s[1]; m[6] = (2 * (yz + wx)) * s[1]; m[7] = 0;
        m[8] = (2 * (xz + wy)) * s[2]; m[9] = (2 * (yz - wx)) * s[2]; m[10] = (1 - 2 * (xx + yy)) * s[2]; m[11] = 0;
        m[12] = t[to]; m[13] = t[to + 1]; m[14] = t[to + 2]; m[15] = 1;
    }

    /** out = a * b, column-major, out may not alias a or b. */
    static void multiply(float[] out, int oo, float[] a, int ao, float[] b, int bo) {
        for (int c = 0; c < 4; c++) {
            float b0 = b[bo + c * 4], b1 = b[bo + c * 4 + 1], b2 = b[bo + c * 4 + 2], b3 = b[bo + c * 4 + 3];
            for (int r = 0; r < 4; r++) {
                out[oo + c * 4 + r] = a[ao + r] * b0 + a[ao + 4 + r] * b1 + a[ao + 8 + r] * b2 + a[ao + 12 + r] * b3;
            }
        }
    }
}
