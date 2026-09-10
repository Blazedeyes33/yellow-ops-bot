package com.bachke.goa;

/* loaded from: classes.dex */
public final class Model3D {
    public static final double STRIDE = 3.25d;

    public static double footZ(double d) {
        double floor = d - Math.floor(d);
        return floor < 0.5d ? (floor * 3.25d) - 0.8125d : 0.8125d - ((floor - 0.5d) * 3.25d);
    }

    public static double footY(double d) {
        double floor = d - Math.floor(d);
        if (floor < 0.5d) {
            return 0.13d;
        }
        return 0.13d + (Math.sin((floor - 0.5d) * 3.141592653589793d * 2.0d) * 0.56d);
    }

    public static void character(Mesh3D mesh3D, int i, double d, double d2, double d3, boolean z, double d4, double d5) {
        double d6;
        double d7;
        double d8;
        double d9;
        double d10;
        double d11;
        int i2 = 1;
        int i3 = 2;
        boolean z2 = i >= 2;
        boolean z3 = i % 2 == 1;
        int i4 = z2 ? 13207904 : 12154698;
        int i5 = z2 ? z3 ? 9327293 : 15624286 : z3 ? 16098620 : 2145203;
        int i6 = z2 ? 1600377 : z3 ? 6898587 : 2705000;
        int i7 = z3 ? z2 ? 15300438 : 16761917 : 16181967;
        double d12 = d / 3.25d;
        double d13 = 0.0d;
        double cos = z ? 0.43d : ((Math.cos((d12 * 3.141592653589793d) * 4.0d) * 0.035d) + 1.05d) - (d4 < 0.11d ? Math.sin((Math.max(0.0d, d4) * 3.141592653589793d) / 0.11d) * 0.12d : 0.0d);
        mesh3D.lean = d5;
        int i8 = -1;
        while (true) {
            if (i8 > i2) {
                break;
            }
            double d14 = d12 + (i8 <= 0 ? d13 : 0.5d);
            double footY = footY(d14);
            double footZ = footZ(d14);
            double d15 = i8 * 0.2d;
            if (d3 > 0.03d) {
                footY = (i8 <= 0 ? d13 : 0.14d) + 0.3d;
                footZ = i8 > 0 ? -0.38d : 0.36d;
            }
            if (z) {
                d11 = i8 > 0 ? -0.67d : -0.94d;
                d10 = 0.11d;
            } else {
                d10 = footY;
                d11 = footZ;
            }
            double[] dArr = new double[i3];
            CharacterRig.joint(0.0d, cos, d11, d10, 0.68d, 0.68d, true, dArr);
            double d16 = dArr[i2];
            double d17 = dArr[0];
            mesh3D.rod(d15, cos, 0.0d, d15, d16, d17, 0.18d, 0.145d, i6);
            mesh3D.ellipsoid(d15, d16, d17, 0.146d, 0.15d, 0.145d, i4);
            mesh3D.rod(d15, d16, d17, d15, d10, d11, 0.12d, 0.082d, i4);
            double d18 = d11 - 0.11d;
            mesh3D.ellipsoid(d15, d10 - 0.06d, d18, 0.155d, 0.12d, 0.29d, i7);
            mesh3D.box(d15, d10 - 0.145d, d18, 0.29d, 0.065d, 0.49d, 16775395);
            mesh3D.box(d15, d10 + 0.037d, d11 - 0.21d, 0.14d, 0.026d, 0.11d, 3624807);
            i8 += 2;
            i3 = i3;
            i2 = 1;
            d13 = 0.0d;
        }
        mesh3D.ellipsoid(0.0d, cos + 0.035d, 0.0d, 0.33d, 0.24d, 0.245d, i6);
        double d19 = cos + 0.6d;
        double d20 = z ? 0.28d : 0.0d;
        mesh3D.ellipsoid(0.0d, cos + 0.37d, d20, 0.375d, 0.43d, 0.245d, i5);
        mesh3D.box(0.0d, cos + 0.03d, 0.208d, 0.58d, 0.065d, 0.052d, 2313568);
        double d21 = cos + 0.14d;
        double d22 = d19 - 0.13d;
        mesh3D.rod(-0.27d, d21, 0.223d, -0.29d, d22, 0.223d, 0.026d, 0.026d, 16772553);
        mesh3D.rod(0.27d, d21, 0.223d, 0.29d, d22, 0.223d, 0.026d, 0.026d, 16772553);
        if (z2) {
            double d23 = cos + 0.43d;
            mesh3D.box(0.0d, d23, 0.246d, 0.2d, 0.19d, 0.015d, 16772553);
            mesh3D.box(0.0d, d23, 0.258d, 0.09d, 0.11d, 0.015d, i5);
        }
        if (z3) {
            for (int i9 = 0; i9 < 4; i9++) {
                mesh3D.ellipsoid(i9 % 2 == 0 ? -0.15d : 0.12d, cos + 0.2d + (i9 * 0.11d), 0.235d, 0.035d, 0.045d, 0.023d, 16773055);
            }
        }
        double d24 = d19 + 0.16d;
        double d25 = d20;
        mesh3D.rod(0.0d, d19 - 0.03d, d25, 0.0d, d24, d20, 0.13d, 0.125d, i4);
        mesh3D.ellipsoid(0.0d, d19 + 0.01d, d25, 0.175d, 0.075d, 0.145d, 16773321);
        double d26 = d19 + 0.38d;
        double d27 = d20 - 0.035d;
        mesh3D.ellipsoid(0.0d, d26, d27, 0.345d, 0.4d, 0.315d, i4);
        mesh3D.ellipsoid(0.0d, d26 + 0.235d, d27 + 0.018d, 0.36d, 0.245d, 0.326d, 2367543);
        for (int i10 = 0; i10 < 6; i10++) {
            double d28 = i10 * 1.05d;
            mesh3D.ellipsoid(Math.cos(d28) * 0.25d, d26 + 0.29d + (Math.sin(i10 * 2) * 0.04d), d27 + (Math.sin(d28) * 0.19d), 0.14d, 0.145d, 0.15d, 2367543);
        }
        int i11 = -1;
        while (i11 <= 1) {
            double d29 = i11;
            mesh3D.ellipsoid(d29 * 0.335d, d26 - 0.025d, d27, 0.073d, 0.105d, 0.07d, i4);
            double d30 = d29 * 0.135d;
            mesh3D.ellipsoid(d30, d26 + 0.025d, d27 - 0.275d, 0.091d, 0.091d, 0.044d, 16774887);
            mesh3D.ellipsoid(d30, d26 + 0.023d, d27 - 0.312d, 0.037d, 0.05d, 0.02d, 1777713);
            double d31 = d27 - 0.28d;
            int i12 = i11;
            mesh3D.rod(d30 - 0.07d, d26 + 0.13d, d31, d30 + 0.07d, d26 + 0.145d, d31, 0.022d, 0.022d, 2367543);
            double sin = Math.sin((d12 + (i12 > 0 ? 0.5d : 0.0d)) * 3.141592653589793d * 2.0d);
            double d32 = 0.12d - (0.25d * sin);
            double d33 = d19 - 0.3d;
            double d34 = (-0.25d) - (sin * 0.35d);
            double d35 = d19 - 0.18d;
            if (d3 > 0.03d) {
                d33 = d19 - 0.16d;
                d32 = 0.18d;
                d34 = -0.19d;
                d35 = d24;
            }
            if (z) {
                d8 = d19 - 0.2d;
                d6 = d19 - 0.48d;
                d9 = 0.55d;
                d7 = 0.43d;
            } else {
                d6 = d35;
                d7 = d32;
                d8 = d33;
                d9 = d34;
            }
            double d36 = d29 * 0.49d;
            double d37 = d29 * 0.43d;
            mesh3D.rod(d29 * 0.35d, d19 - 0.045d, d20, d36, d8, d7, 0.155d, 0.115d, i5);
            mesh3D.ellipsoid(d36, d8, d7, 0.115d, 0.12d, 0.115d, i4);
            mesh3D.rod(d36, d8, d7, d37, d6, d9, 0.095d, 0.075d, i4);
            mesh3D.ellipsoid(d37, d6, d9, 0.1d, 0.125d, 0.1d, i4);
            mesh3D.rod(d37, d6 - 0.06d, d9, d37, d6 - 0.035d, d9, 0.1d, 0.1d, 16768633);
            i11 = i12 + 2;
        }
        mesh3D.ellipsoid(0.0d, d26 - 0.045d, d27 - 0.313d, 0.062d, 0.087d, 0.08d, i4);
        double d38 = d26 - 0.19d;
        double d39 = d27 - 0.27d;
        mesh3D.rod(-0.075d, d38, d39, 0.075d, d38, d39, 0.014d, 0.014d, 7946034);
        if (z2) {
            double sin2 = Math.sin(9.0d * d2) * 0.16d;
            mesh3D.ellipsoid(0.0d, d26 + 0.29d, d27 + 0.29d, 0.13d, 0.12d, 0.12d, i5);
            mesh3D.rod(0.0d, d26 + 0.27d, d27 + 0.28d, sin2, d26 - 0.13d, d27 + 0.65d, 0.14d, 0.1d, 2367543);
            mesh3D.ellipsoid(sin2, d26 - 0.15d, d27 + 0.66d, 0.12d, 0.2d, 0.15d, 2367543);
        } else if (z3) {
            mesh3D.rod(0.0d, d19 + 0.04d, 0.17d, Math.sin(11.0d * d2) * 0.13d, d19 - 0.28d, 0.46d, 0.07d, 0.11d, 3524289);
        }
        mesh3D.lean = 0.0d;
    }
}
