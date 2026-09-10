package com.bachke.goa;

/* loaded from: classes.dex */
public final class World3D {
    private static final int CREAM = 16772045;
    private static final int ROOF = 12013878;
    private static final int WOOD = 6703931;

    public static Mesh3D build(int i) {
        int i2;
        double d;
        int[] iArr;
        Mesh3D mesh3D;
        Mesh3D mesh3D2 = new Mesh3D();
        mesh3D2.box(0.0d, -0.22d, -65.0d, 9.0d, 0.4d, 210.0d, 8951190);
        int i3 = -1;
        while (true) {
            i2 = 1;
            if (i3 > 1) {
                break;
            }
            double d2 = i3;
            int i4 = i3;
            mesh3D2.box(d2 * 4.75d, -0.02d, -65.0d, 1.1d, 0.35d, 210.0d, 14338477);
            mesh3D2.box(d2 * 4.12d, 0.03d, -65.0d, 0.13d, 0.18d, 210.0d, 16770212);
            mesh3D2.box(i4 * 13, -0.3d, -65.0d, 17.0d, 0.35d, 210.0d, i == 4 ? 15454100 : 9351042);
            i3 = i4 + 2;
        }
        int i5 = 4;
        int i6 = -6;
        while (i6 < 42) {
            double d3 = 16 - (i6 * 4);
            int i7 = -1;
            while (i7 <= i2) {
                mesh3D2.box(i7 * 4.75d, 0.165d, d3, 1.08d, 0.012d, 0.055d, 11706246);
                i7 += 2;
                i5 = i5;
                mesh3D2 = mesh3D2;
                d3 = d3;
                i6 = i6;
                i2 = 1;
            }
            i6++;
            mesh3D2 = mesh3D2;
            i2 = 1;
        }
        Mesh3D mesh3D3 = mesh3D2;
        int i8 = i5;
        for (int i9 = 0; i9 < 7; i9++) {
            double d4 = 18 - (i9 * 28);
            int i10 = -1;
            while (i10 <= 1) {
                double d5 = i10;
                double d6 = d5 * 7.5d;
                if (i == i8 && i10 == 1) {
                    Mesh3D mesh3D4 = mesh3D3;
                    boat(mesh3D4, d6 + 0.7d, d4 - 5.0d);
                    d = d5;
                    mesh3D = mesh3D4;
                } else {
                    Mesh3D mesh3D5 = mesh3D3;
                    if (i == 2) {
                        d = d5;
                        market(mesh3D5, d6, d4, i10, i9);
                        mesh3D = mesh3D5;
                    } else {
                        d = d5;
                        if (i == 3) {
                            if (i9 % 2 == 0) {
                                church(mesh3D5, i10 * 10, d4 - 9.0d, true);
                            } else {
                                house(mesh3D5, d6, d4, 14666672, i10, i9);
                            }
                            mesh3D = mesh3D5;
                        } else {
                            if (i == 1) {
                                iArr = new int[]{14792027, 15190417, 12310981};
                            } else {
                                int[] iArr2 = new int[i8];
                                iArr2[0] = 15763301;
                                iArr2[1] = 16043111;
                                iArr2[2] = 6471096;
                                iArr2[3] = 11845599;
                                iArr = iArr2;
                            }
                            house(mesh3D5, d6, d4, iArr[i9 % iArr.length], i10, i9);
                            if (i == 0 && i9 % 3 == 1) {
                                church(mesh3D5, i10 * 13, d4 - 13.0d, false);
                            }
                            if (i != 1 || i9 % 3 != 1) {
                                mesh3D = mesh3D5;
                            } else {
                                mesh3D = mesh3D5;
                                clock(mesh3D, i10 * 10, d4 - 13.0d);
                            }
                        }
                    }
                }
                palm(mesh3D, d * 5.45d, d4 - 9.0d, ((i9 % 3) * 0.45d) + 5.5d);
                lamp(mesh3D, 4.65d * d, d4 - 2.0d);
                planter(mesh3D, 5.2d * d, 3.0d + d4);
                i10 += 2;
                mesh3D3 = mesh3D;
            }
        }
        Mesh3D mesh3D6 = mesh3D3;
        if (i != i8) {
            return mesh3D6;
        }
        mesh3D6.box(24.0d, -0.28d, -75.0d, 22.0d, 0.1d, 240.0d, 2664893);
        for (int i11 = 0; i11 < 45; i11++) {
            mesh3D6.wind = 0.25f;
            mesh3D6.box(((i11 % 3) * 1.8d) + 16.0d, -0.19d, 22.0d - (i11 * 4.5d), 0.16d, 0.04d, 3.0d, 10873315);
        }
        mesh3D6.wind = 0.0f;
        return mesh3D6;
    }

    private static void house(Mesh3D mesh3D, double d, double d2, int i, int i2, int i3) {
        double d3 = ((i3 % 2) * 1.1d) + 4.5d;
        mesh3D.box(d, d3 / 2.0d, d2, 4.8d, d3, 7.8d, i);
        mesh3D.box(d, 0.36d, d2, 5.0d, 0.55d, 8.0d, 11240036);
        mesh3D.box(d, d3 - 0.17d, d2, 5.1d, 0.23d, 8.1d, CREAM);
        roof(mesh3D, d, d3, d2, 5.4d, 8.5d);
        double d4 = i2;
        double d5 = d - (2.43d * d4);
        for (int i4 = -1; i4 <= 1; i4++) {
            double d6 = d2 + (i4 * 2.4d);
            mesh3D.box(d5, 2.05d, d6, 0.1d, 2.15d, 1.4d, CREAM);
            mesh3D.box(d5 - (0.07d * d4), 2.1d, d6, 0.08d, 1.72d, 1.03d, 3235685);
            for (int i5 = 0; i5 < 5; i5++) {
                mesh3D.box(d5 - (0.12d * d4), (i5 * 0.28d) + 1.38d, d6, 0.05d, 0.035d, 1.03d, 7511444);
            }
            mesh3D.box(d5 - (0.15d * d4), 1.0d, d6, 0.35d, 0.15d, 1.6d, CREAM);
            if (d3 > 5.0d) {
                mesh3D.box(d5, 4.15d, d6, 0.12d, 1.1d, 1.5d, CREAM);
                mesh3D.box(d5 - (0.09d * d4), 4.15d, d6, 0.1d, 0.85d, 1.13d, 3499113);
            }
        }
        double d7 = d3 * 0.4d;
        mesh3D.box(d, d7, d2 + 3.94d, 1.6d, 2.5d, 0.1d, CREAM);
        mesh3D.box(d, d7, d2 + 4.01d, 1.17d, 2.2d, 0.08d, 3829874);
        for (int i6 = -2; i6 <= 2; i6++) {
            double d8 = d + (i6 * 0.8d);
            double d9 = d2 + 4.65d;
            mesh3D.rod(d8, 0.55d, d9, d8, 1.4d, d9, 0.045d, 0.045d, CREAM);
        }
        mesh3D.box(d, 0.5d, d2 + 4.4d, 4.7d, 0.3d, 1.1d, 13810067);
        mesh3D.box(d, 1.4d, d2 + 4.65d, 4.7d, 0.1d, 0.1d, CREAM);
    }

    private static void roof(Mesh3D mesh3D, double d, double d2, double d3, double d4, double d5) {
        double d6 = d4 / 2.0d;
        double d7 = d - d6;
        double d8 = d5 / 2.0d;
        double d9 = d3 - d8;
        double d10 = d2 + 1.3d;
        double d11 = d3 + d8;
        mesh3D.quad(d7, d2, d9, d, d10, d9, d, d10, d11, d7, d2, d11, ROOF);
        double d12 = d + d6;
        mesh3D.quad(d, d10, d9, d12, d2, d9, d12, d2, d11, d, d10, d11, 12739906);
        mesh3D.tri(d7, d2, d11, d12, d2, d11, d, d10, d11, 15186564);
        mesh3D.tri(d12, d2, d9, d7, d2, d9, d, d10, d9, 15186564);
        for (int i = 0; i < 9; i++) {
            double d13 = d9 + ((i * d5) / 8.0d);
            double d14 = d2 + 0.02d;
            double d15 = d2 + 1.32d;
            mesh3D.rod(d7, d14, d13, d, d15, d13, 0.035d, 0.035d, 14914402);
            mesh3D.rod(d, d15, d13, d12, d14, d13, 0.035d, 0.035d, 14914402);
        }
    }

    private static void church(Mesh3D mesh3D, double d, double d2, boolean z) {
        int i = z ? 10843477 : 16774104;
        mesh3D.box(d, 3.0d, d2, 6.0d, 6.0d, 9.0d, i);
        roof(mesh3D, d, 6.0d, d2, 6.5d, 9.5d);
        mesh3D.box(d, 3.3d, d2 + 4.65d, 6.5d, 6.6d, 0.5d, i);
        for (int i2 = -1; i2 <= 1; i2 += 2) {
            double d3 = d + (i2 * 2.55d);
            double d4 = d2 + 4.6d;
            mesh3D.box(d3, 4.5d, d4, 1.5d, 9.0d, 1.6d, i);
            mesh3D.box(d3, 8.8d, d4, 1.8d, 0.3d, 1.9d, CREAM);
            mesh3D.ellipsoid(d3, 9.15d, d4, 0.8d, 0.75d, 0.85d, i);
            mesh3D.box(d3, 7.65d, d2 + 5.45d, 0.65d, 1.1d, 0.06d, 3295313);
            mesh3D.box(d3, 10.0d, d4, 0.13d, 1.05d, 0.13d, 7428419);
            mesh3D.box(d3, 10.18d, d4, 0.65d, 0.13d, 0.13d, 7428419);
        }
        double d5 = d2 + 4.97d;
        mesh3D.tri(d - 2.0d, 6.6d, d5, d + 2.0d, 6.6d, d5, d, 8.1d, d5, i);
        mesh3D.box(d, 2.0d, d2 + 4.96d, 1.7d, 3.2d, 0.08d, 7031095);
        mesh3D.ellipsoid(d, 3.5d, d2 + 4.99d, 0.84d, 0.7d, 0.06d, 7031095);
        mesh3D.box(d, 5.35d, d2 + 4.98d, 0.7d, 1.1d, 0.08d, 3690071);
        for (int i3 = 0; i3 < 5; i3++) {
            double d6 = i3;
            mesh3D.box(d, (0.2d * d6) + 0.15d, (d2 + 5.9d) - (0.32d * d6), 7.0d - (d6 * 0.45d), 0.3d, 1.6d, 14206629);
        }
    }

    private static void clock(Mesh3D mesh3D, double d, double d2) {
        mesh3D.box(d, 3.0d, d2, 6.5d, 6.0d, 7.0d, 15124336);
        roof(mesh3D, d, 6.0d, d2, 7.0d, 7.7d);
        mesh3D.box(d, 6.0d, d2 + 3.5d, 2.4d, 5.0d, 1.8d, 16048288);
        mesh3D.ellipsoid(d, 7.1d, d2 + 4.43d, 0.64d, 0.64d, 0.05d, CREAM);
        double d3 = d2 + 4.5d;
        mesh3D.rod(d, 7.1d, d3, d, 7.53d, d3, 0.035d, 0.035d, 3492948);
        mesh3D.rod(d, 7.1d, d3, d + 0.31d, 6.93d, d3, 0.035d, 0.035d, 3492948);
        for (int i = -2; i <= 2; i++) {
            double d4 = d + (i * 1.25d);
            double d5 = d2 + 3.75d;
            mesh3D.rod(d4, 0.3d, d5, d4, 2.8d, d5, 0.12d, 0.12d, CREAM);
        }
        mesh3D.box(d, 3.0d, d2 + 3.7d, 6.8d, 0.3d, 0.9d, CREAM);
    }

    private static void market(Mesh3D mesh3D, double d, double d2, int i, int i2) {
        double d3 = i;
        house(mesh3D, d + (d3 * 1.5d), d2, 14990721, i, i2);
        for (int i3 = 0; i3 < 2; i3++) {
            double d4 = (d2 - 1.5d) + (i3 * 4.5d);
            double d5 = d - (1.3d * d3);
            mesh3D.box(d5, 0.7d, d4, 2.0d, 1.3d, 3.7d, WOOD);
            mesh3D.box(d5, 2.5d, d4, 2.6d, 0.15d, 4.1d, i2 % 2 == 0 ? 15098693 : 3583399);
            for (int i4 = -1; i4 <= 1; i4 += 2) {
                double d6 = d - (2.5d * d3);
                double d7 = d4 + (i4 * 1.8d);
                mesh3D.rod(d6, 0.0d, d7, d6, 2.5d, d7, 0.055d, 0.055d, WOOD);
            }
            for (int i5 = 0; i5 < 6; i5++) {
                mesh3D.ellipsoid(d5, 1.47d, (d4 - 1.35d) + (i5 * 0.53d), 0.24d, 0.22d, 0.24d, i5 % 2 == 0 ? 16038712 : 8629830);
            }
        }
    }

    private static void boat(Mesh3D mesh3D, double d, double d2) {
        mesh3D.ellipsoid(d, 0.4d, d2, 1.05d, 0.44d, 3.6d, 3777454);
        mesh3D.ellipsoid(d, 0.62d, d2, 0.88d, 0.12d, 3.3d, 16373904);
        mesh3D.box(d, 0.73d, d2, 0.14d, 0.1d, 6.7d, WOOD);
        for (int i = -1; i <= 1; i++) {
            mesh3D.box(d, 0.71d, d2 + (i * 1.6d), 1.7d, 0.1d, 0.24d, WOOD);
        }
        mesh3D.rod(d, 0.5d, d2, d, 3.7d, d2, 0.07d, 0.045d, WOOD);
        mesh3D.tri(d, 3.6d, d2, d, 1.3d, d2, d + 1.6d, 1.4d, d2, 16772048);
    }

    private static void planter(Mesh3D mesh3D, double d, double d2) {
        mesh3D.rod(d, 0.18d, d2, d, 0.68d, d2, 0.27d, 0.4d, 12939852);
        mesh3D.ellipsoid(d, 0.86d, d2, 0.5d, 0.4d, 0.5d, 4690283);
        for (int i = 0; i < 3; i++) {
            double d3 = i * 2;
            mesh3D.ellipsoid(d + (Math.cos(d3) * 0.3d), 1.06d, d2 + (Math.sin(d3) * 0.3d), 0.14d, 0.12d, 0.14d, 15565934);
        }
    }

    private static void lamp(Mesh3D mesh3D, double d, double d2) {
        mesh3D.rod(d, 0.1d, d2, d, 3.5d, d2, 0.075d, 0.055d, 3562592);
        mesh3D.box(d, 3.48d, d2, 0.45d, 0.6d, 0.45d, 16765049);
        mesh3D.box(d, 3.84d, d2, 0.61d, 0.13d, 0.61d, 3562592);
    }

    private static void palm(Mesh3D mesh3D, double d, double d2, double d3) {
        double d4 = d + 0.35d;
        mesh3D.rod(d, 0.15d, d2, d4, d3, d2, 0.19d, 0.12d, 10255953);
        int i = 0;
        while (i < 6) {
            double d5 = (i * 3.141592653589793d) / 3.0d;
            mesh3D.wind = 1.0f;
            int i2 = 0;
            while (i2 < 4) {
                double d6 = i2;
                double d7 = d6 * 0.7d;
                int i3 = i2 + 1;
                double d8 = i3 * 0.7d;
                double sin = (d3 + (Math.sin(d7) * 0.55d)) - ((d7 * d7) * 0.17d);
                double sin2 = (d3 + (Math.sin(d8) * 0.55d)) - ((d8 * d8) * 0.17d);
                double cos = d4 + (Math.cos(d5) * d7);
                double sin3 = d2 + (Math.sin(d5) * d7);
                double cos2 = d4 + (Math.cos(d5) * d8);
                double sin4 = d2 + (Math.sin(d5) * d8);
                double d9 = (1.0d - (d6 * 0.15d)) * 0.36d;
                double sin5 = Math.sin(d5) * d9;
                double d10 = (-Math.cos(d5)) * d9;
                double d11 = sin5 * 0.75d;
                double d12 = 0.75d * d10;
                mesh3D.quad(cos - sin5, sin, sin3 - d10, cos2 - d11, sin2, sin4 - d12, cos2 + d11, sin2, sin4 + d12, cos + sin5, sin, sin3 + d10, i % 2 == 0 ? 3247207 : 5810789);
                i = i;
                i2 = i3;
            }
            mesh3D.wind = 0.0f;
            i++;
        }
        mesh3D.ellipsoid(d4, d3 - 0.1d, d2, 0.28d, 0.3d, 0.25d, 8942148);
    }
}
