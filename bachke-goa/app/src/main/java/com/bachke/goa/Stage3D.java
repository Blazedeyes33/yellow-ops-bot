package com.bachke.goa;

import com.bachke.goa.RunnerCore;

/* loaded from: classes.dex */
public final class Stage3D {
    public final Mesh3D[] worlds = new Mesh3D[5];
    public final Mesh3D actors = new Mesh3D();

    public Stage3D() {
        for (int i = 0; i < 5; i++) {
            this.worlds[i] = World3D.build(i);
        }
    }
    /** When true the procedural CP9 character is not emitted; GlRenderer3D draws the skinned model instead. */
    public boolean skinnedCharacter;

    public void frame(RunnerCore runnerCore, int i, double d, boolean z) {
        double d2;
        boolean z2;
        double d3;
        double d4;
        Mesh3D mesh3D = this.actors;
        mesh3D.clear();
        double d5 = 0.0d;
        double renderX = z ? 0.0d : runnerCore.renderX();
        double renderY = z ? 0.0d : runnerCore.renderY();
        double d6 = renderX;
        mesh3D.shadow(renderX, 0.0d, 0.6d, 0.75d);
        double d7 = 10.0d;
        boolean z3 = true;
        if (z) {
            d2 = d6;
            z2 = true;
            d3 = 0.0d;
        } else {
            for (RunnerCore.Hazard hazard : runnerCore.hazards) {
                double worldX = hazard.worldX();
                double d8 = -hazard.z;
                if (d8 < -110.0d) {
                    z3 = z3;
                    d6 = d6;
                } else if (d8 <= 10.0d) {
                    double d9 = d6;
                    int r12 = z3 ? 1 : 0;
                    mesh3D.shadow(worldX, d8, 0.8d, hazard.depth() / 2.0d);
                    int i2 = -1;
                    if (hazard.kind == RunnerCore.Kind.SCOOTER) {
                        d4 = d5;
                        double dir = Math.signum(hazard.velocity == 0.0d ? 1.0d : hazard.velocity);
                        mesh3D.yaw = dir > 0 ? -1.5707963267948966d : 1.5707963267948966d;
                        mesh3D.ox = worldX; mesh3D.oz = d8; mesh3D.oy = 0.0d;
                        mesh3D.box(0.0d, 0.55d, 0.0d, 0.5d, 0.35d, 1.5d, 15168086);      // body
                        mesh3D.box(0.0d, 0.30d, 0.0d, 0.42d, 0.28d, 1.1d, 3166820);       // engine/chassis
                        mesh3D.rod(0.0d, 0.32d, 0.72d, 0.0d, 0.32d, 0.72d, 0.30d, 0.30d, 2504256);  // wheels (discs)
                        mesh3D.ellipsoid(0.0d, 0.32d, 0.72d, 0.12d, 0.32d, 0.32d, 2504256);
                        mesh3D.ellipsoid(0.0d, 0.32d, -0.62d, 0.12d, 0.32d, 0.32d, 2504256);
                        mesh3D.rod(0.0d, 0.7d, 0.55d, 0.0d, 1.15d, 0.75d, 0.03d, 0.03d, 3562592);   // steering column
                        mesh3D.box(0.0d, 1.15d, 0.75d, 0.62d, 0.04d, 0.08d, 3562592);     // handlebar
                        mesh3D.box(0.0d, 0.98d, 0.85d, 0.36d, 0.32d, 0.10d, 16769958);    // headlamp/front shield
                        mesh3D.ellipsoid(0.0d, 0.95d, -0.05d, 0.26d, 0.30d, 0.26d, 15300438); // rider torso
                        mesh3D.ellipsoid(0.0d, 1.42d, -0.05d, 0.17d, 0.19d, 0.17d, 13209681); // helmet
                        mesh3D.yaw = 0.0d; mesh3D.ox = 0.0d; mesh3D.oz = 0.0d; mesh3D.oy = 0.0d;
                    } else if (hazard.crossing && hazard.velocity != 0.0d) {
                        d4 = d5;
                        mesh3D.box(worldX, 0.8d, d8, 1.4d, 0.7d, 1.0d, 2402720);
                        mesh3D.box(worldX, 1.18d, d8, 1.5d, 0.12d, 1.1d, 15976315);
                        for (int i3 = -1; i3 <= r12; i3 += 2) {
                            double d10 = i3;
                            mesh3D.ellipsoid(worldX + (d10 * 0.6d), 0.3d, d8, 0.15d, 0.3d, 0.3d, 2702920);
                            double d11 = worldX + (d10 * 0.5d);
                            mesh3D.rod(d11, 0.9d, d8 + 0.4d, d11, 0.9d, d8 + 1.0d, 0.05d, 0.05d, 7753785);
                        }
                        while (i2 <= r12) {
                            mesh3D.ellipsoid(worldX + (i2 * 0.4d), 1.4d, d8, 0.22d, 0.22d, 0.25d, 15710270);
                            i2++;
                        }
                        mesh3D.box(worldX, 2.2d, d8, 0.9d, 0.08d, 0.1d, 16761671);
                    } else {
                        d4 = d5;
                        if (hazard.kind == RunnerCore.Kind.CRATE) {
                            mesh3D.box(worldX, 0.55d, d8, 1.4d, 1.1d, 1.0d, 13209681);
                            mesh3D.box(worldX, 0.55d, d8 + 0.515d, 1.16d, 0.87d, 0.025d, 9135169);
                            double d12 = worldX - 0.55d;
                            double d13 = d8 + 0.55d;
                            double d14 = worldX + 0.55d;
                            mesh3D.rod(d12, 0.18d, d13, d14, 0.95d, d13, 0.06d, 0.06d, 15777915);
                            mesh3D.rod(d14, 0.18d, d13, d12, 0.95d, d13, 0.06d, 0.06d, 15777915);
                        } else if (hazard.kind == RunnerCore.Kind.BARRIER) {
                            while (i2 <= r12) {
                                double d15 = worldX + (i2 * 0.86d);
                                mesh3D.rod(d15, 0.1d, d8, d15, 2.6d, d8, 0.08d, 0.08d, 3427159);
                                i2 += 2;
                            }
                            mesh3D.box(worldX, 2.04d, d8, 1.95d, 1.0d, 0.65d, 16761671);
                            for (int i4 = 0; i4 < 3; i4++) {
                                double d16 = i4 * 0.53d;
                                double d17 = d8 + 0.34d;
                                mesh3D.rod((worldX - 0.7d) + d16, 1.62d, d17, (worldX - 0.27d) + d16, 2.46d, d17, 0.07d, 0.07d, 14639692);
                            }
                        } else if (hazard.kind == RunnerCore.Kind.GAP) {
                            mesh3D.box(worldX, 0.025d, d8, 1.8d, 0.04d, 2.4d, 2571592);
                            mesh3D.box(worldX, 0.052d, d8 + 1.2d, 1.9d, 0.06d, 0.15d, 15775560);
                        } else {
                            mesh3D.box(worldX, 1.05d, d8, 1.73d, 1.8d, 3.8d, 15168086);
                            mesh3D.box(worldX, 2.13d, d8 + 0.27d, 1.62d, 0.58d, 2.3d, 16769958);
                            mesh3D.box(worldX, 1.65d, d8 + 1.93d, 1.4d, 0.57d, 0.025d, 3166820);
                            mesh3D.box(worldX, 0.54d, d8 + 1.95d, 1.77d, 0.19d, 0.14d, 16765320);
                            while (i2 <= r12) {
                                double d18 = i2;
                                mesh3D.box(worldX + (0.62d * d18), 0.82d, d8 + 1.94d, 0.22d, 0.16d, 0.04d, 16773072);
                                double d19 = worldX + (d18 * 0.86d);
                                double d20 = d8 - 1.12d;
                                double d21 = worldX + (d18 * 0.93d);
                                mesh3D.rod(d19, 0.4d, d20, d21, 0.4d, d20, 0.34d, 0.34d, 2504256);
                                double d22 = d8 + 1.2d;
                                mesh3D.rod(d19, 0.4d, d22, d21, 0.4d, d22, 0.34d, 0.34d, 2504256);
                                i2 += 2;
                            }
                        }
                    }
                    d5 = d4;
                    z3 = r12 != 0;
                    d6 = d9;
                }
            }
            d2 = d6;
            z2 = z3;
            d3 = d5;
            for (RunnerCore.Coin coin : runnerCore.coins) {
                if (coin.z >= -9.0d && coin.z <= 105.0d) {
                    mesh3D.ox = coin.worldX();
                    mesh3D.oy = coin.y;
                    mesh3D.oz = -coin.z;
                    mesh3D.yaw = 2.3d * d;
                    mesh3D.ellipsoid(0.0d, 0.0d, 0.0d, 0.23d, 0.28d, 0.065d, 16763718);
                    mesh3D.rod(0.0d, -0.13d, 0.07d, 0.0d, 0.13d, 0.07d, 0.023d, 0.023d, 16773537);
                }
            }
        }
        mesh3D.ox = d2;
        mesh3D.oy = renderY;
        mesh3D.oz = d3;
        mesh3D.yaw = z ? (Math.sin(d * 0.6d) * 0.22d) + 3.141592653589793d : d3;
        double max = z ? d3 : Math.max(-0.18d, Math.min(0.18d, (runnerCore.x - runnerCore.target()) * 0.15d));
        double renderDistance = z ? 2.1d * d : runnerCore.renderDistance();
        if (z || !runnerCore.sliding()) {
            z2 = false;
        }
        if (!z) {
            d7 = runnerCore.time - runnerCore.landAt;
        }
        double d23 = d3;
        if (!this.skinnedCharacter) {
            Model3D.character(mesh3D, i, renderDistance, d, renderY, z2, d7, max);
        }
        mesh3D.yaw = d23;
        mesh3D.oz = d23;
        mesh3D.oy = d23;
        mesh3D.ox = d23;
    }
}
