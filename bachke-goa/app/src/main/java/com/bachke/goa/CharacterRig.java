package com.bachke.goa;

import com.bachke.goa.GameCore;

/* loaded from: classes.dex */
public final class CharacterRig {
    public static final double STRIDE = 176.0d;
    private final double[] current = new double[12];
    private final double[] from = new double[12];
    private final double[] target = new double[12];
    private int lastState = -1;
    private double changedAt = -1.0d;
    private double lastTime = -1.0d;
    private final double[] knee = new double[2];

    public static double footX(double d) {
        double floor = d - Math.floor(d);
        return floor < 0.5d ? 44.0d - (floor * 176.0d) : (-44.0d) + ((floor - 0.5d) * 176.0d);
    }

    public static double footLift(double d) {
        double floor = d - Math.floor(d);
        if (floor < 0.5d) {
            return 0.0d;
        }
        return Math.sin((floor - 0.5d) * 3.141592653589793d * 2.0d) * 32.0d;
    }

    public static void joint(double d, double d2, double d3, double d4, double d5, double d6, boolean z, double[] dArr) {
        double d7 = d3 - d;
        double d8 = d4 - d2;
        double max = Math.max(0.001d, Math.sqrt((d7 * d7) + (d8 * d8)));
        double min = Math.min((d5 + d6) - 0.001d, Math.max(Math.abs(d5 - d6) + 0.001d, max));
        double d9 = d5 * d5;
        double d10 = ((d9 - (d6 * d6)) + (min * min)) / (min * 2.0d);
        double sqrt = Math.sqrt(Math.max(0.0d, d9 - (d10 * d10)));
        double d11 = z ? 1.0d : -1.0d;
        double d12 = d7 / max;
        double d13 = d8 / max;
        dArr[0] = (d12 * d10) + d + (d13 * sqrt * d11);
        dArr[1] = (d2 + (d13 * d10)) - ((d12 * sqrt) * d11);
    }

    private void pose(double d, double d2, GameCore.Motion motion) {
        double d3 = d / 176.0d;
        double d4 = 3.141592653589793d * d3 * 2.0d;
        this.target[0] = footX(d3);
        this.target[1] = (-5.0d) - footLift(d3);
        double d5 = d3 + 0.5d;
        this.target[2] = footX(d5);
        this.target[3] = (-5.0d) - footLift(d5);
        this.target[4] = Math.sin(d4) * 25.0d;
        this.target[5] = (-72.0d) - (Math.cos(d4) * 10.0d);
        this.target[6] = (-Math.sin(d4)) * 25.0d;
        this.target[7] = (Math.cos(d4) * 10.0d) - 72.0d;
        this.target[8] = Math.sin(d4 * 2.0d) * 2.0d;
        this.target[9] = 7.0d;
        this.target[10] = Math.sin(d4) * 2.0d;
        this.target[11] = 0.0d;
        if (motion != GameCore.Motion.RUN) {
            this.target[8] = 0.0d;
            this.target[9] = 6.0d;
            this.target[10] = 0.0d;
            if (motion == GameCore.Motion.RISE) {
                this.target[0] = -20.0d;
                this.target[1] = -17.0d;
                this.target[2] = 22.0d;
                this.target[3] = -30.0d;
                this.target[4] = 23.0d;
                this.target[5] = -107.0d;
                this.target[6] = -19.0d;
                this.target[7] = -73.0d;
                this.target[9] = 10.0d;
            }
            if (motion == GameCore.Motion.BOOST) {
                this.target[0] = -13.0d;
                this.target[1] = -33.0d;
                this.target[2] = 23.0d;
                this.target[3] = -40.0d;
                this.target[4] = 28.0d;
                this.target[5] = -109.0d;
                this.target[6] = -18.0d;
                this.target[7] = -102.0d;
                this.target[9] = 13.0d;
            }
            if (motion == GameCore.Motion.FALL) {
                this.target[0] = -21.0d;
                this.target[1] = -8.0d;
                this.target[2] = 25.0d;
                this.target[3] = -3.0d;
                this.target[4] = 37.0d;
                this.target[5] = -73.0d;
                this.target[6] = -28.0d;
                this.target[7] = -74.0d;
                this.target[9] = -3.0d;
            }
            if (motion == GameCore.Motion.LAND || motion == GameCore.Motion.HIT) {
                this.target[0] = -21.0d;
                this.target[1] = -5.0d;
                this.target[2] = 27.0d;
                this.target[3] = -5.0d;
                this.target[8] = 9.0d;
                this.target[9] = 19.0d;
                this.target[4] = 36.0d;
                this.target[5] = -53.0d;
                this.target[6] = -23.0d;
                this.target[7] = -60.0d;
            }
        }
        int ordinal = motion.ordinal();
        if (this.lastState < 0 || d2 < this.lastTime) {
            System.arraycopy(this.target, 0, this.current, 0, 12);
            this.lastState = ordinal;
            this.changedAt = d2 - 0.1d;
            System.arraycopy(this.current, 0, this.from, 0, 12);
        }
        if (ordinal != this.lastState) {
            System.arraycopy(this.current, 0, this.from, 0, 12);
            this.changedAt = d2;
            this.lastState = ordinal;
        }
        double max = Math.max(0.0d, Math.min(1.0d, (d2 - this.changedAt) / 0.075d));
        double d6 = max * max * (3.0d - (max * 2.0d));
        for (int i = 0; i < 12; i++) {
            this.current[i] = this.from[i] + ((this.target[i] - this.from[i]) * d6);
        }
        this.lastTime = d2;
    }

    public void draw(Painter painter, int i, double d, double d2, double d3, double d4, double d5, GameCore.Motion motion) {
        pose(d4, d5, motion);
        double d6 = this.current[8] - 50.0d;
        double d7 = this.current[9];
        double sin = Math.sin(Math.toRadians(d7)) * 38.0d;
        double cos = d6 - (Math.cos(Math.toRadians(d7)) * 38.0d);
        leg(painter, i, d, d2, d3, d6, this.current[2], this.current[3], 0.76d);
        arm(painter, i, d, d2, d3, d6, this.current[6], this.current[7], 0.76d);
        leg(painter, i, d, d2, d3, d6, this.current[0], this.current[1], 1.0d);
        part(painter, i, 1, d, d2, d3, 0.0d, d6 + 5.0d, 32.0d, 43.0d, d7, 0.5d, 1.0d, 1.0d);
        if (i >= 2) {
            part(painter, i, 7, d, d2, d3, sin - 9.0d, cos - 27.0d, 36.0d, 29.0d, (Math.sin(8.0d * d5) * 9.0d) - 10.0d, 0.9d, 0.3d, 1.0d);
        } else if (i == 1) {
            part(painter, i, 7, d, d2, d3, sin - 2.0d, cos + 7.0d, 34.0d, 12.0d, Math.sin(d5 * 9.0d) * 7.0d, 0.9d, 0.4d, 1.0d);
        }
        part(painter, i, 0, d, d2, d3, sin, cos + 2.0d, 37.0d, 39.0d, this.current[10] + (d7 * 0.25d), 0.44d, 1.0d, 1.0d);
        arm(painter, i, d, d2, d3, d6, this.current[4], this.current[5], 1.0d);
    }

    private void leg(Painter painter, int i, double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        joint(0.0d, d4, d5, d6, 33.0d, 34.0d, true, this.knee);
        double d8 = this.knee[0];
        double d9 = this.knee[1];
        bone(painter, i, 4, d, d2, d3, 0.0d, d4, d8, d9, 18.0d, d7);
        bone(painter, i, 5, d, d2, d3, d8, d9, d5, d6, 11.0d, d7);
        part(painter, i, 6, d, d2, d3, d5, d6, 23.0d, 10.0d, 0.0d, 0.34d, 0.4d, d7);
    }

    private void arm(Painter painter, int i, double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        double d8 = d4 - 28.0d;
        joint(3.0d, d8, d5, d6, 22.0d, 23.0d, false, this.knee);
        double d9 = this.knee[0];
        double d10 = this.knee[1];
        bone(painter, i, 2, d, d2, d3, 3.0d, d8, d9, d10, 15.0d, d7);
        bone(painter, i, 3, d, d2, d3, d9, d10, d5, d6, 11.0d, d7);
    }

    private void bone(Painter painter, int i, int i2, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9) {
        double d10 = d6 - d4;
        double d11 = d7 - d5;
        double sqrt = Math.sqrt((d10 * d10) + (d11 * d11)) + 5.0d;
        part(painter, i, i2, d, d2, d3, d4, d5, d8, sqrt, Math.toDegrees(Math.atan2(d11, d10)) - 90.0d, 0.5d, 2.0d / sqrt, d9);
    }

    private void part(Painter painter, int i, int i2, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11) {
        painter.part((i * 8) + i2, d + (d4 * d3), d2 + (d5 * d3), d6 * d3, d7 * d3, d8, d9, d10, d11);
    }
}
