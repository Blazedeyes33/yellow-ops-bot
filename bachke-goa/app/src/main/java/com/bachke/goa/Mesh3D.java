package com.bachke.goa;

import java.util.Arrays;

/* loaded from: classes.dex */
public final class Mesh3D {
    public float[] data = new float[32768];
    public double lean;
    public double ox;
    public double oy;
    public double oz;
    public int size;
    public float wind;
    public double yaw;

    public void clear() {
        this.size = 0;
        this.lean = 0.0d;
        this.yaw = 0.0d;
        this.oz = 0.0d;
        this.oy = 0.0d;
        this.ox = 0.0d;
        this.wind = 0.0f;
    }

    public int vertices() {
        return this.size / 10;
    }

    public void vertex(double d, double d2, double d3, double d4, double d5, double d6, int i) {
        if (this.size + 10 > this.data.length) {
            this.data = Arrays.copyOf(this.data, this.data.length * 2);
        }
        double cos = Math.cos(this.yaw);
        double sin = Math.sin(this.yaw);
        double cos2 = Math.cos(this.lean);
        double sin2 = Math.sin(this.lean);
        double d7 = (d * cos2) - (d2 * sin2);
        double d8 = (d * sin2) + (d2 * cos2);
        double d9 = (d4 * cos2) - (d5 * sin2);
        double d10 = (sin2 * d4) + (cos2 * d5);
        float[] fArr = this.data;
        int i2 = this.size;
        this.size = i2 + 1;
        fArr[i2] = (float) (this.ox + (d7 * cos) + (d3 * sin));
        float[] fArr2 = this.data;
        int i3 = this.size;
        this.size = i3 + 1;
        fArr2[i3] = (float) (this.oy + d8);
        float[] fArr3 = this.data;
        int i4 = this.size;
        this.size = i4 + 1;
        fArr3[i4] = (float) ((this.oz + (d3 * cos)) - (d7 * sin));
        float[] fArr4 = this.data;
        int i5 = this.size;
        this.size = i5 + 1;
        fArr4[i5] = (float) ((d9 * cos) + (d6 * sin));
        float[] fArr5 = this.data;
        int i6 = this.size;
        this.size = i6 + 1;
        fArr5[i6] = (float) d10;
        float[] fArr6 = this.data;
        int i7 = this.size;
        this.size = i7 + 1;
        fArr6[i7] = (float) ((cos * d6) - (d9 * sin));
        float[] fArr7 = this.data;
        int i8 = this.size;
        this.size = i8 + 1;
        fArr7[i8] = ((i >> 16) & 255) / 255.0f;
        float[] fArr8 = this.data;
        int i9 = this.size;
        this.size = i9 + 1;
        fArr8[i9] = ((i >> 8) & 255) / 255.0f;
        float[] fArr9 = this.data;
        int i10 = this.size;
        this.size = i10 + 1;
        fArr9[i10] = (i & 255) / 255.0f;
        float[] fArr10 = this.data;
        int i11 = this.size;
        this.size = i11 + 1;
        fArr10[i11] = this.wind;
    }

    public void tri(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, int i) {
        double d10 = d4 - d;
        double d11 = d5 - d2;
        double d12 = d6 - d3;
        double d13 = d7 - d;
        double d14 = d8 - d2;
        double d15 = d9 - d3;
        double d16 = (d11 * d15) - (d12 * d14);
        double d17 = (d12 * d13) - (d15 * d10);
        double d18 = (d10 * d14) - (d11 * d13);
        double sqrt = Math.sqrt((d16 * d16) + (d17 * d17) + (d18 * d18));
        if (sqrt < 1.0E-9d) {
            return;
        }
        double d19 = d16 / sqrt;
        double d20 = d17 / sqrt;
        double d21 = d18 / sqrt;
        vertex(d, d2, d3, d19, d20, d21, i);
        vertex(d4, d5, d6, d19, d20, d21, i);
        vertex(d7, d8, d9, d19, d20, d21, i);
    }

    public void quad(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12, int i) {
        tri(d, d2, d3, d4, d5, d6, d7, d8, d9, i);
        tri(d, d2, d3, d7, d8, d9, d10, d11, d12, i);
    }

    public void box(double d, double d2, double d3, double d4, double d5, double d6, int i) {
        double d7 = d4 / 2.0d;
        double d8 = d - d7;
        double d9 = d + d7;
        double d10 = d5 / 2.0d;
        double d11 = d2 - d10;
        double d12 = d2 + d10;
        double d13 = d6 / 2.0d;
        double d14 = d3 - d13;
        double d15 = d3 + d13;
        quad(d8, d11, d15, d9, d11, d15, d9, d12, d15, d8, d12, d15, i);
        quad(d9, d11, d14, d8, d11, d14, d8, d12, d14, d9, d12, d14, i);
        quad(d8, d11, d14, d8, d11, d15, d8, d12, d15, d8, d12, d14, i);
        quad(d9, d11, d15, d9, d11, d14, d9, d12, d14, d9, d12, d15, i);
        quad(d8, d12, d15, d9, d12, d15, d9, d12, d14, d8, d12, d14, i);
        quad(d8, d11, d14, d9, d11, d14, d9, d11, d15, d8, d11, d15, i);
    }

    public void ellipsoid(double d, double d2, double d3, double d4, double d5, double d6, int i) {
        for (int i2 = 0; i2 < 8; i2++) {
            int i3 = 0;
            while (i3 < 12) {
                double d7 = 8;
                double d8 = ((i2 * 3.141592653589793d) / d7) - 1.5707963267948966d;
                double d9 = (((i2 + 1) * 3.141592653589793d) / d7) - 1.5707963267948966d;
                double d10 = 12;
                double d11 = ((i3 * 3.141592653589793d) * 2.0d) / d10;
                i3++;
                double d12 = ((i3 * 3.141592653589793d) * 2.0d) / d10;
                point(d, d2, d3, d4, d5, d6, d8, d11, i);
                point(d, d2, d3, d4, d5, d6, d9, d11, i);
                point(d, d2, d3, d4, d5, d6, d9, d12, i);
                point(d, d2, d3, d4, d5, d6, d8, d11, i);
                point(d, d2, d3, d4, d5, d6, d9, d12, i);
                point(d, d2, d3, d4, d5, d6, d8, d12, i);
            }
        }
    }

    private void point(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, int i) {
        double cos = Math.cos(d7) * Math.cos(d8);
        double sin = Math.sin(d7);
        double cos2 = Math.cos(d7) * Math.sin(d8);
        double d9 = cos / d4;
        double d10 = sin / d5;
        double d11 = cos2 / d6;
        double sqrt = Math.sqrt((d9 * d9) + (d10 * d10) + (d11 * d11));
        vertex(d + (cos * d4), d2 + (sin * d5), d3 + (cos2 * d6), d9 / sqrt, d10 / sqrt, d11 / sqrt, i);
    }

    public void rod(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, int i) {
        double d9;
        double d10;
        double d11 = d4 - d;
        double d12 = d5 - d2;
        double d13 = d6 - d3;
        double sqrt = Math.sqrt((d11 * d11) + (d12 * d12) + (d13 * d13));
        if (sqrt < 1.0E-6d) {
            return;
        }
        double d14 = d11 / sqrt;
        double d15 = d12 / sqrt;
        double d16 = d13 / sqrt;
        double d17 = -d14;
        double sqrt2 = Math.sqrt((d15 * d15) + (d17 * d17));
        if (sqrt2 < 0.001d) {
            d10 = 1.0d;
            d9 = 0.0d;
        } else {
            d9 = d17 / sqrt2;
            d10 = d15 / sqrt2;
        }
        double d18 = (d15 * 0.0d) - (d16 * d9);
        double d19 = (d16 * d10) - (d14 * 0.0d);
        double d20 = (d14 * d9) - (d15 * d10);
        int i2 = 0;
        while (i2 < 10) {
            double d21 = (i2 * 3.141592653589793d) / 5.0d;
            int i3 = i2 + 1;
            double d22 = (i3 * 3.141592653589793d) / 5.0d;
            double cos = (Math.cos(d21) * d10) + (Math.sin(d21) * d18);
            double cos2 = (Math.cos(d21) * d9) + (Math.sin(d21) * d19);
            double sin = (Math.sin(d21) * d20) + (Math.cos(d21) * 0.0d);
            double cos3 = (Math.cos(d22) * d10) + (Math.sin(d22) * d18);
            double cos4 = (Math.cos(d22) * d9) + (Math.sin(d22) * d19);
            double cos5 = (Math.cos(d22) * 0.0d) + (Math.sin(d22) * d20);
            double d23 = d + (cos * d7);
            double d24 = d2 + (cos2 * d7);
            double d25 = d3 + (sin * d7);
            vertex(d23, d24, d25, cos, cos2, sin, i);
            double d26 = d4 + (cos * d8);
            double d27 = d5 + (cos2 * d8);
            double d28 = d6 + (sin * d8);
            vertex(d26, d27, d28, cos, cos2, sin, i);
            double d29 = d4 + (cos3 * d8);
            double d30 = d5 + (cos4 * d8);
            double d31 = d6 + (cos5 * d8);
            vertex(d29, d30, d31, cos3, cos4, cos5, i);
            vertex(d23, d24, d25, cos, cos2, sin, i);
            vertex(d29, d30, d31, cos3, cos4, cos5, i);
            double d32 = d + (cos3 * d7);
            double d33 = d2 + (cos4 * d7);
            double d34 = d3 + (cos5 * d7);
            vertex(d32, d33, d34, cos3, cos4, cos5, i);
            tri(d, d2, d3, d32, d33, d34, d23, d24, d25, i);
            tri(d4, d5, d6, d26, d27, d28, d29, d30, d31, i);
            i2 = i3;
        }
    }

    public void shadow(double d, double d2, double d3, double d4) {
        int i = 3;
        int i2 = 3;
        while (i2 >= 1) {
            double d5 = (i2 * 0.15d) + 0.65d;
            int i3 = i2 == i ? 7832453 : i2 == 2 ? 6582644 : 5332835;
            int i4 = 0;
            while (i4 < 24) {
                double d6 = (i4 * 3.141592653589793d) / 12.0d;
                int i5 = i4 + 1;
                double d7 = (i5 * 3.141592653589793d) / 12.0d;
                double d8 = ((4 - i2) * 0.002d) + 0.016d;
                double d9 = d3 * d5;
                double d10 = d4 * d5;
                tri(d, d8, d2, d + (Math.cos(d6) * d9), d8, d2 + (Math.sin(d6) * d10), d + (d9 * Math.cos(d7)), d8, d2 + (d10 * Math.sin(d7)), i3);
                i4 = i5;
                i2 = i2;
                i = i;
            }
            i2--;
        }
    }
}
