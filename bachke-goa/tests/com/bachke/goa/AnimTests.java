package com.bachke.goa;

import java.io.FileInputStream;
import java.util.Locale;

/** Desktop checks for the skinned character runtime: model format, skeleton math, animator state machine. */
public final class AnimTests {
    static int passed = 0, failed = 0;
    /** General 4x4 inverse, column-major. */
    static void invert(float[] m, float[] out) {
        double[] a = new double[16]; for (int i = 0; i < 16; i++) a[i] = m[i];
        double[] inv = new double[16];
        inv[0]=a[5]*a[10]*a[15]-a[5]*a[11]*a[14]-a[9]*a[6]*a[15]+a[9]*a[7]*a[14]+a[13]*a[6]*a[11]-a[13]*a[7]*a[10];
        inv[4]=-a[4]*a[10]*a[15]+a[4]*a[11]*a[14]+a[8]*a[6]*a[15]-a[8]*a[7]*a[14]-a[12]*a[6]*a[11]+a[12]*a[7]*a[10];
        inv[8]=a[4]*a[9]*a[15]-a[4]*a[11]*a[13]-a[8]*a[5]*a[15]+a[8]*a[7]*a[13]+a[12]*a[5]*a[11]-a[12]*a[7]*a[9];
        inv[12]=-a[4]*a[9]*a[14]+a[4]*a[10]*a[13]+a[8]*a[5]*a[14]-a[8]*a[6]*a[13]-a[12]*a[5]*a[10]+a[12]*a[6]*a[9];
        inv[1]=-a[1]*a[10]*a[15]+a[1]*a[11]*a[14]+a[9]*a[2]*a[15]-a[9]*a[3]*a[14]-a[13]*a[2]*a[11]+a[13]*a[3]*a[10];
        inv[5]=a[0]*a[10]*a[15]-a[0]*a[11]*a[14]-a[8]*a[2]*a[15]+a[8]*a[3]*a[14]+a[12]*a[2]*a[11]-a[12]*a[3]*a[10];
        inv[9]=-a[0]*a[9]*a[15]+a[0]*a[11]*a[13]+a[8]*a[1]*a[15]-a[8]*a[3]*a[13]-a[12]*a[1]*a[11]+a[12]*a[3]*a[9];
        inv[13]=a[0]*a[9]*a[14]-a[0]*a[10]*a[13]-a[8]*a[1]*a[14]+a[8]*a[2]*a[13]+a[12]*a[1]*a[10]-a[12]*a[2]*a[9];
        inv[2]=a[1]*a[6]*a[15]-a[1]*a[7]*a[14]-a[5]*a[2]*a[15]+a[5]*a[3]*a[14]+a[13]*a[2]*a[7]-a[13]*a[3]*a[6];
        inv[6]=-a[0]*a[6]*a[15]+a[0]*a[7]*a[14]+a[4]*a[2]*a[15]-a[4]*a[3]*a[14]-a[12]*a[2]*a[7]+a[12]*a[3]*a[6];
        inv[10]=a[0]*a[5]*a[15]-a[0]*a[7]*a[13]-a[4]*a[1]*a[15]+a[4]*a[3]*a[13]+a[12]*a[1]*a[7]-a[12]*a[3]*a[5];
        inv[14]=-a[0]*a[5]*a[14]+a[0]*a[6]*a[13]+a[4]*a[1]*a[14]-a[4]*a[2]*a[13]-a[12]*a[1]*a[6]+a[12]*a[2]*a[5];
        inv[3]=-a[1]*a[6]*a[11]+a[1]*a[7]*a[10]+a[5]*a[2]*a[11]-a[5]*a[3]*a[10]-a[9]*a[2]*a[7]+a[9]*a[3]*a[6];
        inv[7]=a[0]*a[6]*a[11]-a[0]*a[7]*a[10]-a[4]*a[2]*a[11]+a[4]*a[3]*a[10]+a[8]*a[2]*a[7]-a[8]*a[3]*a[6];
        inv[11]=-a[0]*a[5]*a[11]+a[0]*a[7]*a[9]+a[4]*a[1]*a[11]-a[4]*a[3]*a[9]-a[8]*a[1]*a[7]+a[8]*a[3]*a[5];
        inv[15]=a[0]*a[5]*a[10]-a[0]*a[6]*a[9]-a[4]*a[1]*a[10]+a[4]*a[2]*a[9]+a[8]*a[1]*a[6]-a[8]*a[2]*a[5];
        double det = a[0]*inv[0]+a[1]*inv[4]+a[2]*inv[8]+a[3]*inv[12];
        for (int i = 0; i < 16; i++) out[i] = (float) (inv[i] / det);
    }
    /** Rotation part of a column-major matrix (assumed orthonormal) to quaternion xyzw. */
    static void toQuat(float[] m, float[] q, int o) {
        double m00 = m[0], m10 = m[1], m20 = m[2], m01 = m[4], m11 = m[5], m21 = m[6], m02 = m[8], m12 = m[9], m22 = m[10];
        double tr = m00 + m11 + m22, x, y, z, w;
        if (tr > 0) { double s = Math.sqrt(tr + 1) * 2; w = 0.25 * s; x = (m21 - m12) / s; y = (m02 - m20) / s; z = (m10 - m01) / s; }
        else if (m00 > m11 && m00 > m22) { double s = Math.sqrt(1 + m00 - m11 - m22) * 2; w = (m21 - m12) / s; x = 0.25 * s; y = (m01 + m10) / s; z = (m02 + m20) / s; }
        else if (m11 > m22) { double s = Math.sqrt(1 + m11 - m00 - m22) * 2; w = (m02 - m20) / s; x = (m01 + m10) / s; y = 0.25 * s; z = (m12 + m21) / s; }
        else { double s = Math.sqrt(1 + m22 - m00 - m11) * 2; w = (m10 - m01) / s; x = (m02 + m20) / s; y = (m12 + m21) / s; z = 0.25 * s; }
        q[o] = (float) x; q[o + 1] = (float) y; q[o + 2] = (float) z; q[o + 3] = (float) w;
    }
    static void check(String n, boolean ok) { if (ok) { passed++; System.out.println("ok   " + n); } else { failed++; System.out.println("FAIL " + n); } }

    public static void main(String[] args) throws Exception {
        for (String path : args) {
            BgmModel m;
            try (FileInputStream in = new FileInputStream(path)) { m = BgmModel.load(in); }
            String tag = path.replaceAll(".*/", "");
            check(tag + ": bones <= MAX_BONES (" + m.boneCount + ")", m.boneCount <= BgmModel.MAX_BONES);
            check(tag + ": u16 indices (" + m.vertexCount + " verts)", m.vertexCount < 65536);
            check(tag + ": triangle budget <= 14000 (" + m.indexCount / 3 + ")", m.indexCount / 3 <= 14000);
            check(tag + ": has core clips", m.clipByName.containsKey("Run_Anime") && m.clipByName.containsKey("Jump_Start") && m.clipByName.containsKey("Jump_air")
                && m.clipByName.containsKey("Jump_Land") && m.clipByName.containsKey("Slide") && m.clipByName.containsKey("Idle_A") && m.clipByName.containsKey("Hit_Knockback"));
            check(tag + ": no finger bones", !m.bones[0].name.isEmpty() && java.util.Arrays.stream(m.bones).noneMatch(b -> b.name.startsWith("index_") || b.name.startsWith("thumb_") || b.name.endsWith("_leaf") || b.name.contains("HandIndex") || b.name.contains("HandThumb") || b.name.endsWith("_End")));
            // weights sum to 255 per vertex; joints in range
            boolean wOk = true, jOk = true;
            for (int v = 0; v < m.vertexCount; v++) {
                int o = v * BgmModel.VERTEX_STRIDE; int s = 0;
                for (int k = 0; k < 4; k++) { s += m.vertices.get(o + 36 + k) & 0xff; if ((m.vertices.get(o + 32 + k) & 0xff) >= m.boneCount) jOk = false; }
                if (s != 255) wOk = false;
            }
            check(tag + ": weights sum to 1", wOk); check(tag + ": joint indices in range", jOk);
            // Skeleton math: bind-pose locals derived from inverse-bind matrices must reproduce identity skinning.
            float[][] bindWorld = new float[m.boneCount][16];
            for (int i = 0; i < m.boneCount; i++) invert(m.bones[i].inverseBind, bindWorld[i]);
            Pose bind = new Pose(m.boneCount); float[] inv = new float[16], loc = new float[16], rootInv = new float[16];
            invert(m.rootTransform, rootInv);
            for (int i = 0; i < m.boneCount; i++) {
                int p = m.bones[i].parent;
                if (p < 0) Skeleton.multiply(loc, 0, rootInv, 0, bindWorld[i], 0);
                else { invert(bindWorld[p], inv); Skeleton.multiply(loc, 0, inv, 0, bindWorld[i], 0); }
                toQuat(loc, bind.q, i * 4); bind.t[i * 3] = loc[12]; bind.t[i * 3 + 1] = loc[13]; bind.t[i * 3 + 2] = loc[14];
                m.bones[i].restS[0] = m.bones[i].restS[1] = m.bones[i].restS[2] = 1f;
            }
            Skeleton sk = new Skeleton(m); sk.evaluate(bind);
            double maxErr = 0;
            for (int v = 0; v < m.vertexCount; v += 53) {
                int o = v * BgmModel.VERTEX_STRIDE;
                float px = m.vertices.getFloat(o), py = m.vertices.getFloat(o + 4), pz = m.vertices.getFloat(o + 8);
                double sx = 0, sy = 0, sz = 0;
                for (int k = 0; k < 4; k++) {
                    int j = m.vertices.get(o + 32 + k) & 0xff; double w = (m.vertices.get(o + 36 + k) & 0xff) / 255.0; if (w == 0) continue;
                    int r = j * 12;
                    sx += w * (sk.boneRows[r] * px + sk.boneRows[r + 1] * py + sk.boneRows[r + 2] * pz + sk.boneRows[r + 3]);
                    sy += w * (sk.boneRows[r + 4] * px + sk.boneRows[r + 5] * py + sk.boneRows[r + 6] * pz + sk.boneRows[r + 7]);
                    sz += w * (sk.boneRows[r + 8] * px + sk.boneRows[r + 9] * py + sk.boneRows[r + 10] * pz + sk.boneRows[r + 11]);
                }
                maxErr = Math.max(maxErr, Math.abs(sx - px) + Math.abs(sy - py) + Math.abs(sz - pz));
            }
            check(tag + String.format(Locale.US, ": bind-pose skinning reproduces mesh (max err %.4f)", maxErr), maxErr < 0.01);
            // Posed plausibility: skinned Idle_A frame keeps a standing human envelope.
            Pose idle = new Pose(m.boneCount); idle.sample(m.clip("Idle_A"), 0.5f); sk.evaluate(idle);
            double minYs = 9, maxYs = -9, maxR = 0;
            for (int v = 0; v < m.vertexCount; v += 7) {
                int o = v * BgmModel.VERTEX_STRIDE;
                float px = m.vertices.getFloat(o), py = m.vertices.getFloat(o + 4), pz = m.vertices.getFloat(o + 8);
                double sx = 0, sy = 0, sz = 0;
                for (int k = 0; k < 4; k++) {
                    int j = m.vertices.get(o + 32 + k) & 0xff; double w = (m.vertices.get(o + 36 + k) & 0xff) / 255.0; if (w == 0) continue;
                    int r = j * 12;
                    sx += w * (sk.boneRows[r] * px + sk.boneRows[r + 1] * py + sk.boneRows[r + 2] * pz + sk.boneRows[r + 3]);
                    sy += w * (sk.boneRows[r + 4] * px + sk.boneRows[r + 5] * py + sk.boneRows[r + 6] * pz + sk.boneRows[r + 7]);
                    sz += w * (sk.boneRows[r + 8] * px + sk.boneRows[r + 9] * py + sk.boneRows[r + 10] * pz + sk.boneRows[r + 11]);
                }
                minYs = Math.min(minYs, sy); maxYs = Math.max(maxYs, sy); maxR = Math.max(maxR, Math.hypot(sx, sz));
            }
            double bindH = m.bboxMax[1] - m.bboxMin[1];
            check(tag + String.format(Locale.US, ": posed Idle_A height %.2f within 15%% of bind %.2f, radius %.2f", maxYs - minYs, bindH, maxR), Math.abs((maxYs - minYs) - bindH) < 0.15 * bindH && maxR < 1.2 && minYs > -0.15);
            // sampling: continuity across loop seam for looping clips
            BgmModel.Clip run = m.clip("Run_Anime"); Pose a = new Pose(m.boneCount), b = new Pose(m.boneCount);
            a.sample(run, run.duration() - 0.001f); b.sample(run, 0.001f);
            double d = 0; for (int i = 0; i < a.t.length; i++) d = Math.max(d, Math.abs(a.t[i] - b.t[i]));
            check(tag + String.format(Locale.US, ": Run_Anime loop translation seam %.3f", d), d < 0.2);
            // pelvis stays above ground through a run cycle
            int pelvis = -1; for (int i = 0; i < m.boneCount; i++) if (m.bones[i].name.equals("pelvis") || m.bones[i].name.equals("mixamorigHips")) pelvis = i;
            check(tag + ": pelvis bone present", pelvis >= 0);
            float[] pp = new float[3]; double minY = 9, maxY = -9;
            for (int f = 0; f < 30; f++) { a.sample(run, run.duration() * f / 30f); sk.evaluate(a); sk.bonePosition(pelvis, pp); minY = Math.min(minY, pp[1]); maxY = Math.max(maxY, pp[1]); }
            check(tag + String.format(Locale.US, ": pelvis height plausible in run (%.2f..%.2f)", minY, maxY), minY > 0.5 && maxY < 1.4 && maxY - minY < 0.3);

            // animator state machine driven by the real core
            RunnerCore core = new RunnerCore(); core.streetMode = true;
            float clipSpeed = tag.startsWith("jojo") ? CharacterAnimator.JOJO_RUN_CLIP_SPEED : CharacterAnimator.MAYA_RUN_CLIP_SPEED;
            CharacterAnimator an = new CharacterAnimator(m, clipSpeed);
            an.update(core, true, 0.016); check(tag + ": menu -> IDLE", an.state == CharacterAnimator.State.IDLE);
            core.start(3); core.spawning = false;
            for (int i = 0; i < 10; i++) { core.advance(RunnerCore.STEP); an.update(core, false, RunnerCore.STEP); }
            check(tag + ": playing -> RUN", an.state == CharacterAnimator.State.RUN);
            check(tag + ": run rate matches world speed", Math.abs(an.rate - Math.max(0.8f, Math.min(2.0f, (float) core.speed() / clipSpeed))) < 1e-4);
            core.act(RunnerCore.Action.JUMP);
            boolean sawStart = false, sawAir = false, sawLand = false;
            for (int i = 0; i < 200; i++) { core.advance(RunnerCore.STEP); an.update(core, false, RunnerCore.STEP);
                sawStart |= an.state == CharacterAnimator.State.JUMP_START; sawAir |= an.state == CharacterAnimator.State.JUMP_AIR; sawLand |= an.state == CharacterAnimator.State.JUMP_LAND; }
            check(tag + ": jump plays start -> air -> land", sawStart && sawAir && sawLand);
            check(tag + ": back to RUN after landing", an.state == CharacterAnimator.State.RUN);
            core.act(RunnerCore.Action.SLIDE);
            boolean sawSlide = false, sawExit = false;
            for (int i = 0; i < 160; i++) { core.advance(RunnerCore.STEP); an.update(core, false, RunnerCore.STEP);
                sawSlide |= an.state == CharacterAnimator.State.SLIDE; sawExit |= an.state == CharacterAnimator.State.SLIDE_EXIT; }
            check(tag + ": slide plays slide -> exit -> run", sawSlide && sawExit && an.state == CharacterAnimator.State.RUN);
            core.act(RunnerCore.Action.LEFT);
            boolean sawDodge = false;
            for (int i = 0; i < 60; i++) { core.advance(RunnerCore.STEP); an.update(core, false, RunnerCore.STEP); sawDodge |= an.state == CharacterAnimator.State.DODGE_LEFT; }
            check(tag + ": swipe left plays dodge and returns", sawDodge && an.state == CharacterAnimator.State.RUN);
            core.state = GameCore.State.OVER; an.update(core, false, 0.016);
            check(tag + ": OVER -> HIT", an.state == CharacterAnimator.State.HIT);
            // pose stays finite and normalised through everything
            boolean fin = true; for (int i = 0; i < an.pose.q.length; i += 4) { double n = Math.sqrt(an.pose.q[i]*an.pose.q[i]+an.pose.q[i+1]*an.pose.q[i+1]+an.pose.q[i+2]*an.pose.q[i+2]+an.pose.q[i+3]*an.pose.q[i+3]); if (!(Math.abs(n - 1) < 1e-3)) fin = false; }
            check(tag + ": blended quaternions normalised", fin);
        }
        System.out.println(); System.out.println(passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
