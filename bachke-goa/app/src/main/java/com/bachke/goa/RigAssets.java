package com.bachke.goa;

/* loaded from: classes.dex */
public final class RigAssets {
    private RigAssets() {
    }

    public static int alphaPixel(int i) {
        int i2 = (i >> 16) & 255;
        int i3 = (i >> 8) & 255;
        int i4 = i & 255;
        double max = Math.max(Math.max(255 - i2, i3), 255 - i4);
        if (max < 22.0d) {
            return 0;
        }
        if (max >= 82.0d) {
            return i4 | (i2 << 16) | (-16777216) | (i3 << 8);
        }
        double d = (max - 22.0d) / 60.0d;
        double d2 = (1.0d - d) * 255.0d;
        return ((int) Math.max(0.0d, Math.min(255.0d, (i4 - d2) / d))) | (((int) Math.max(0.0d, Math.min(255.0d, (i2 - d2) / d))) << 16) | (((int) (d * 255.0d)) << 24) | (((int) Math.max(0.0d, Math.min(255.0d, i3 / d))) << 8);
    }
    public static int[] largestBounds(int[] iArr, int i, int i2) {
        char c;
        int i3;
        char c2;
        boolean z;
        int[] iArr2 = iArr;
        boolean[] zArr = new boolean[iArr2.length];
        int[] iArr3 = new int[iArr2.length];
        char c3 = 4;
        int i4 = 0;
        boolean z2 = true;
        char c4 = 3;
        int[] iArr4 = {0, 0, i, i2};
        int i5 = 0;
        int i6 = 0;
        while (i5 < iArr2.length) {
            if (zArr[i5]) {
                c = c3;
                boolean z3 = z2;
                i3 = i4;
                c2 = c4;
                z = z3;
            } else if ((iArr2[i5] >>> 24) < 40) {
                c = c3;
                boolean z4 = z2;
                i3 = i4;
                c2 = c4;
                z = z4;
            } else {
                iArr3[i4] = i5;
                zArr[i5] = z2;
                int i7 = i;
                int i8 = i2;
                int i9 = i4;
                int i10 = i9;
                int i11 = z2 ? 1 : 0;
                int r8 = z2 ? 1 : 0;
                while (i10 < i11) {
                    int i12 = i10 + 1;
                    int i13 = iArr3[i10];
                    int i14 = i13 % i;
                    int i15 = i13 / i;
                    i7 = Math.min(i7, i14);
                    i8 = Math.min(i8, i15);
                    i4 = Math.max(i4, i14);
                    int max = Math.max(i9, i15);
                    int i16 = -1;
                    for (int i17 = r8; i16 <= i17; i17 = 1) {
                        int i18 = i4;
                        int i19 = -1;
                        for (int i20 = i17; i19 <= i20; i20 = 1) {
                            int i21 = i14 + i19;
                            int i22 = i8;
                            int i23 = i15 + i16;
                            if (i21 >= 0 && i21 < i && i23 >= 0) {
                                if (i23 < i2) {
                                    int i24 = (i23 * i) + i21;
                                    if (!zArr[i24] && (iArr2[i24] >>> 24) >= 40) {
                                        zArr[i24] = true;
                                        iArr3[i11] = i24;
                                        i11++;
                                    }
                                }
                            }
                            i19++;
                            iArr2 = iArr;
                            i8 = i22;
                        }
                        i16++;
                        iArr2 = iArr;
                        i4 = i18;
                    }
                    i10 = i12;
                    i9 = max;
                    r8 = 1;
                    iArr2 = iArr;
                }
                if (i11 > i6) {
                    c = 4;
                    i3 = 0;
                    z = true;
                    c2 = 3;
                    iArr4 = new int[]{Math.max(0, i7 - 1), Math.max(0, i8 - 1), Math.min(i, i4 + 2), Math.min(i2, i9 + 2)};
                    i6 = i11;
                } else {
                    c = 4;
                    c2 = 3;
                    i3 = 0;
                    z = true;
                }
            }
            i5++;
            c3 = c;
            iArr2 = iArr;
            boolean z5 = z;
            c4 = c2;
            i4 = i3;
            z2 = z5;
        }
        if (i6 < 100) {
            throw new IllegalArgumentException("Empty rig component");
        }
        return iArr4;
    }
}
