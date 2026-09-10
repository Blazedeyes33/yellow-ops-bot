package com.bachke.goa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Deterministic regression checks for the simulation core. No Android dependency. */
public final class CoreTests {
    static int passed = 0, failed = 0;
    static final List<String> failures = new ArrayList<>();

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("ok   " + name); }
        else { failed++; failures.add(name); System.out.println("FAIL " + name); }
    }
    static void near(String name, double a, double b, double eps) { check(name + String.format(Locale.US, " (%.4f vs %.4f)", a, b), Math.abs(a - b) <= eps); }

    static RunnerCore fresh(boolean street, long seed) {
        RunnerCore c = new RunnerCore(); c.streetMode = street; c.start(seed); return c;
    }
    static void run(RunnerCore c, double seconds) {
        int steps = (int) Math.round(seconds / RunnerCore.STEP);
        for (int i = 0; i < steps; i++) c.advance(RunnerCore.STEP);
    }

    public static void main(String[] args) {
        // ---- state machine ----
        RunnerCore c = new RunnerCore();
        check("initial state MENU", c.state == GameCore.State.MENU);
        check("advance in MENU is inert", !advanceChanges(c));
        c.streetMode = true; c.start(1);
        check("start -> PLAYING", c.state == GameCore.State.PLAYING);
        c.pause(); check("pause -> PAUSED", c.state == GameCore.State.PAUSED);
        double t = c.time; c.advance(0.1); near("paused sim frozen", c.time, t, 0);
        c.resume(); check("resume -> PLAYING", c.state == GameCore.State.PLAYING);
        c.advance(0.5); check("frame > 0.25s auto-pauses (interruption guard)", c.state == GameCore.State.PAUSED);
        c.resume(); c.menu(); check("menu -> MENU", c.state == GameCore.State.MENU);

        // ---- restart clears entities ----
        c = fresh(true, 7); run(c, 5.0);
        check("hazards spawned by 5s", !c.hazards.isEmpty());
        c.start(7); check("restart clears hazards", c.hazards.isEmpty() && c.coins.isEmpty() && c.events.isEmpty());
        near("restart resets time", c.time, 0, 0);

        // ---- jump physics ----
        c = fresh(true, 1);
        check("jump from ground accepted", c.act(RunnerCore.Action.JUMP));
        check("double jump rejected", !c.act(RunnerCore.Action.JUMP));
        double apex = 0; double air = 0;
        for (int i = 0; i < 400 && (c.y > 0 || c.vy > 0); i++) { c.advance(RunnerCore.STEP); apex = Math.max(apex, c.y); air += RunnerCore.STEP; }
        check("jump apex between 1.5m and 2.2m: " + apex, apex > 1.5 && apex < 2.2);
        check("airtime between 0.6s and 1.0s: " + air, air > 0.6 && air < 1.0);
        near("landed exactly at ground", c.y, 0, 1e-9);
        check("LAND event emitted", c.events.contains(GameCore.Event.LAND));

        // ---- slide ----
        c = fresh(true, 1);
        check("slide accepted", c.act(RunnerCore.Action.SLIDE));
        check("sliding immediately", c.sliding());
        run(c, 0.9); check("slide ends by 0.9s", !c.sliding());

        // ---- steering bounds ----
        c = fresh(true, 1);
        for (int i = 0; i < 100; i++) c.steer(0.5);
        near("steer clamps to right edge", c.targetX, 3.25, 0);
        for (int i = 0; i < 100; i++) c.steer(-0.5);
        near("steer clamps to left edge", c.targetX, -3.25, 0);
        c.spawning = false; c.targetX = 3.0; run(c, 2.0);
        near("runner reaches steer target", c.x, 3.0, 0.01);

        // ---- level-4 difficulty gate (approved product rule) ----
        c = fresh(true, 3); c.spawning = false;
        near("level 1 speed = base", c.speed(), RunnerCore.STREET_BASE_SPEED, 1e-9);
        run(c, 59.9);
        check("still level 3 at 59.9s", c.level() == 3);
        near("speed unchanged through level 3", c.speed(), RunnerCore.STREET_BASE_SPEED, 1e-9);
        near("cart speed unchanged through level 3", c.cartSpeed(), RunnerCore.CART_BASE_SPEED, 1e-9);
        near("cart interval unchanged through level 3", c.cartInterval(), RunnerCore.CART_INTERVAL_BASE, 1e-9);
        run(c, 0.2);
        check("level 4 at 60.1s", c.level() == 4);
        run(c, 30);
        check("speed grows after level 4", c.speed() > RunnerCore.STREET_BASE_SPEED);
        check("cart speed grows after level 4", c.cartSpeed() > RunnerCore.CART_BASE_SPEED);
        check("cart interval shrinks after level 4", c.cartInterval() < RunnerCore.CART_INTERVAL_BASE);
        run(c, 600);
        near("speed capped", c.speed(), RunnerCore.STREET_MAX_SPEED, 1e-9);
        near("cart speed capped", c.cartSpeed(), RunnerCore.CART_MAX_SPEED, 1e-9);
        near("cart interval floored", c.cartInterval(), RunnerCore.CART_INTERVAL_FLOOR, 1e-9);
        // no sudden jump: speed is continuous at the gate
        c = fresh(true, 3); c.spawning = false; run(c, 60.0); double s0 = c.speed(); run(c, RunnerCore.STEP); double s1 = c.speed();
        check("no speed discontinuity at level 4", s1 - s0 < 0.01);
        // lane mode too
        c = fresh(false, 3); c.spawning = false; run(c, 59.9);
        near("lane mode speed flat through level 3", c.speed(), RunnerCore.LANE_BASE_SPEED, 1e-9);
        check("lane mode row gap flat through level 3", c.rowGap() == 2.75);

        // ---- first cart timing ----
        c = fresh(true, 5); run(c, 1.7);
        check("first cart exists by 1.7s", c.hazards.size() == 1 && c.hazards.get(0).crossing);
        check("first cart spawned at FIRST_CART_DISTANCE", c.hazards.get(0).z > RunnerCore.FIRST_CART_DISTANCE - 1.0 && c.hazards.get(0).z <= RunnerCore.FIRST_CART_DISTANCE);

        // ---- collision and close-call ----
        c = fresh(true, 11); c.spawning = false;
        RunnerCore.Hazard h = new RunnerCore.Hazard(RunnerCore.Kind.CRATE, 0, 3.0); h.crossing = true; h.streetX = 0; h.velocity = 0; c.hazards.add(h);
        run(c, 2.0);
        check("head-on cart ends run", c.state == GameCore.State.OVER && c.events.contains(GameCore.Event.HIT));
        c = fresh(true, 11); c.spawning = false; c.targetX = 1.0; c.x = 1.0;
        h = new RunnerCore.Hazard(RunnerCore.Kind.CRATE, 0, 3.0); h.crossing = true; h.streetX = 0; h.velocity = 0; c.hazards.add(h);
        run(c, 2.0);
        check("passing at 1.0m clearance survives", c.state == GameCore.State.PLAYING);
        check("passing at 1.0m clearance counts as close call", c.closeCalls == 1 && c.combo == 2);
        c = fresh(true, 11); c.spawning = false; c.targetX = 2.5; c.x = 2.5;
        h = new RunnerCore.Hazard(RunnerCore.Kind.CRATE, 0, 3.0); h.crossing = true; h.streetX = 0; h.velocity = 0; c.hazards.add(h);
        run(c, 2.0);
        check("wide pass is not a close call", c.state == GameCore.State.PLAYING && c.closeCalls == 0 && c.combo == 1);
        check("cleared counter increments", c.cleared == 1);

        // ---- seeded courses with a simple driver: every run must be survivable ----
        int survived = 0;
        for (long seed = 0; seed < 24; seed++) {
            c = fresh(true, seed);
            for (int step = 0; step < (int) (240 / RunnerCore.STEP) && c.state == GameCore.State.PLAYING; step++) {
                RunnerCore.Hazard threat = null;
                for (RunnerCore.Hazard hz : c.hazards) if (hz.z > -1.0 && hz.z < 14.0 && (threat == null || hz.z < threat.z)) threat = hz;
                if (threat != null) {
                    // predict obstacle x when it reaches the runner; steer to the far side
                    double eta = Math.max(0, threat.z / c.speed());
                    double px = threat.streetX + threat.velocity * eta;
                    double want = px > 0 ? Math.max(-3.25, px - 2.6) : Math.min(3.25, px + 2.6);
                    c.steer(want - c.targetX);
                    // static obstacles: also use the vertical move if still overlapping laterally
                    if (threat.velocity == 0 && Math.abs(c.x - threat.streetX) < 1.0 && threat.z < 5.0 && threat.z > 0.5) {
                        if (threat.kind == RunnerCore.Kind.CRATE) c.act(RunnerCore.Action.JUMP);
                        else if (threat.kind == RunnerCore.Kind.BARRIER) c.act(RunnerCore.Action.SLIDE);
                    }
                }
                c.advance(RunnerCore.STEP);
            }
            if (c.state == GameCore.State.PLAYING) survived++;
            else System.out.println("     seed " + seed + " died at t=" + String.format(Locale.US, "%.2f", c.time) + " level " + c.level());
        }
        check("driver survives all 24 seeded 4-minute street courses (" + survived + "/24)", survived == 24);

        // ---- street encounter gating ----
        java.util.EnumMap<RunnerCore.Kind, Integer> seenByLevel3 = new java.util.EnumMap<>(RunnerCore.Kind.class);
        java.util.EnumMap<RunnerCore.Kind, Integer> seenLate = new java.util.EnumMap<>(RunnerCore.Kind.class);
        boolean staticBefore2 = false, barrierBefore3 = false, scooterBefore4 = false, pairedBefore6 = false;
        for (long seed = 0; seed < 12; seed++) {
            c = fresh(true, 100 + seed);
            java.util.Set<RunnerCore.Hazard> known = new java.util.HashSet<>();
            for (int step = 0; step < (int) (150 / RunnerCore.STEP); step++) {
                c.advance(RunnerCore.STEP);
                if (c.state != GameCore.State.PLAYING) { c.state = GameCore.State.PLAYING; } // observe spawns only
                for (RunnerCore.Hazard hz : c.hazards) if (known.add(hz)) {
                    int lvl = c.level();
                    (lvl <= 3 ? seenByLevel3 : seenLate).merge(hz.kind, 1, Integer::sum);
                    if (hz.velocity == 0 && lvl < 2) staticBefore2 = true;
                    if (hz.kind == RunnerCore.Kind.BARRIER && lvl < 3) barrierBefore3 = true;
                    if (hz.kind == RunnerCore.Kind.SCOOTER && lvl < 4) scooterBefore4 = true;
                }
                if (lvlPaired(c) && c.level() < 6) pairedBefore6 = true;
            }
        }
        check("level 1 has crossing carts only (no static before level 2)", !staticBefore2);
        check("no barrier before level 3", !barrierBefore3);
        check("no scooter before level 4", !scooterBefore4);
        check("no paired carts before level 6", !pairedBefore6);
        check("crates appear by level 3 " + seenByLevel3, seenByLevel3.getOrDefault(RunnerCore.Kind.CRATE, 0) > 0);
        check("scooters appear after level 4 " + seenLate, seenLate.getOrDefault(RunnerCore.Kind.SCOOTER, 0) > 0 && seenLate.getOrDefault(RunnerCore.Kind.BARRIER, 0) > 0);
        // static obstacle never blocks the whole street: an escape lane of >= 1.72 m must exist
        c = fresh(true, 9); boolean escape = true;
        for (int step = 0; step < (int) (200 / RunnerCore.STEP); step++) { c.advance(RunnerCore.STEP); if (c.state != GameCore.State.PLAYING) c.state = GameCore.State.PLAYING;
            for (RunnerCore.Hazard hz : c.hazards) if (hz.velocity == 0 && Math.abs(hz.streetX) < 1.0) escape = false; }
        check("static obstacles leave an escape side", escape);

        // ---- determinism ----
        RunnerCore a = fresh(true, 99), b = fresh(true, 99);
        for (int i = 0; i < 6000; i++) { a.advance(RunnerCore.STEP); b.advance(RunnerCore.STEP); }
        check("same seed => identical hazards", a.hazards.size() == b.hazards.size() && a.score() == b.score());
        // variable frame times converge to the same fixed-step result
        a = fresh(true, 5); b = fresh(true, 5); a.spawning = b.spawning = false;
        for (int i = 0; i < 600; i++) a.advance(1.0 / 60.0);
        for (int i = 0; i < 1650; i++) b.advance(1.0 / 165.0);
        near("60Hz and 165Hz inputs agree on distance", a.distance, b.distance, 0.2);

        // ---- Routes ----
        check("level(0)=1", Routes.level(0) == 1);
        check("level(59.99)=3", Routes.level(59.99) == 3);
        check("level(60)=4", Routes.level(60) == 4);
        check("five routes cycle", Routes.index(100) == 0 && Routes.NAMES.length == 5);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        for (String f : failures) System.out.println("  - " + f);
        System.exit(failed == 0 ? 0 : 1);
    }

    static boolean lvlPaired(RunnerCore c) {
        int moving = 0; for (RunnerCore.Hazard hz : c.hazards) if (hz.velocity != 0 && hz.z > 20) moving++; return moving >= 2;
    }
    static boolean advanceChanges(RunnerCore c) { double t = c.time; c.advance(0.1); return c.time != t; }
}
