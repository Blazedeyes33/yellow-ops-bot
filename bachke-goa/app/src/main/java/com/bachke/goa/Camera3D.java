package com.bachke.goa;

/* loaded from: classes.dex */
public final class Camera3D {
    public static float[] matrix(double d, boolean z) {
        double d2 = z ? 1.5d : 0.0d;
        double d3 = z ? 3.1d : 4.3d;
        double d4 = z ? 8.0d : 10.5d;
        double d5 = d2 - 0.0d;
        double d6 = d3 - (z ? 1.4d : 1.3d);
        double d7 = d4 - (z ? 0.0d : -18.0d);
        double sqrt = Math.sqrt((d5 * d5) + (d6 * d6) + (d7 * d7));
        double d8 = d5 / sqrt;
        double d9 = d6 / sqrt;
        double d10 = d7 / sqrt;
        double d11 = -d8;
        double sqrt2 = Math.sqrt((d10 * d10) + (d11 * d11));
        double d12 = d3;
        double d13 = d10 / sqrt2;
        double d14 = d11 / sqrt2;
        double d15 = d9 * d14;
        double d16 = d4;
        double d17 = (d10 * d13) - (d8 * d14);
        double d18 = d2;
        double d19 = (-d9) * d13;
        float[] fArr = {(float) d13, (float) d15, (float) d8, 0.0f, 0.0f, (float) d17, (float) d9, 0.0f, (float) d14, (float) d19, (float) d10, 0.0f, (float) (((-d13) * d18) - (d14 * d16)), (float) ((((-d15) * d18) - (d17 * d12)) - (d19 * d16)), (float) (((d11 * d18) - (d9 * d12)) - (d10 * d16)), 1.0f};
        float tan = (float) (1.0d / Math.tan(Math.toRadians(z ? 43.0d : 54.0d) / 2.0d));
        float[] fArr2 = {tan / ((float) d), 0.0f, 0.0f, 0.0f, 0.0f, tan, 0.0f, 0.0f, 0.0f, 0.0f, -1.0014296f, -1.0f, 0.0f, 0.0f, -0.30021444f, 0.0f};
        float[] fArr3 = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int i2 = 0; i2 < 4; i2++) {
                for (int i3 = 0; i3 < 4; i3++) {
                    int i4 = i * 4;
                    int i5 = i4 + i2;
                    fArr3[i5] = fArr3[i5] + (fArr2[(i3 * 4) + i2] * fArr[i4 + i3]);
                }
            }
        }
        return fArr3;
    }
}
