package com.bachke.goa;

import com.bachke.goa.GameCore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* loaded from: classes.dex */
public final class RunnerCore {
    public static final double GRAVITY = 22.0d;
    public static final double JUMP = 8.8d;
    public static final double LANE = 2.25d;
    public static final double STEP = 0.008333333333333333d;
    /** Difficulty is flat through levels 1-3 and only begins to grow at this time (level 4). */
    public static final double RAMP_START = 3.0d * Routes.LEVEL_SECONDS;
    public static final double STREET_BASE_SPEED = 6.0d;
    public static final double STREET_MAX_SPEED = 9.0d;
    public static final double LANE_BASE_SPEED = 13.0d;
    public static final double LANE_MAX_SPEED = 28.0d;
    public static final double CART_BASE_SPEED = 0.7d;
    public static final double CART_MAX_SPEED = 1.5d;
    public static final double CART_INTERVAL_BASE = 4.2d;
    public static final double CART_INTERVAL_FLOOR = 2.6d;
    public static final double FIRST_CART_DISTANCE = 26.0d;
    private double accumulator;
    public int bonus;
    public int cleared;
    public int closeCalls;
    public int collected;
    public double distance;
    public int lane;
    private double nextRow;
    public double previousDistance;
    public double previousX;
    public double previousY;
    private int rows;
    public double slideUntil;
    public boolean streetMode;
    public double targetX;
    public double time;
    public double vy;
    public double x;
    public double y;
    public GameCore.State state = GameCore.State.MENU;
    public final List<Hazard> hazards = new ArrayList();
    public final List<Coin> coins = new ArrayList();
    public final List<GameCore.Event> events = new ArrayList();
    public double closeAt = -10.0d;
    public double landAt = -10.0d;
    public int combo = 1;
    public boolean spawning = true;
    private Random random = new Random();

    public enum Action {
        LEFT,
        RIGHT,
        JUMP,
        SLIDE
    }

    public enum Kind {
        CRATE,
        BARRIER,
        VAN,
        GAP
    }

    public static final class Hazard {
        public double closest = 100.0d;
        public boolean crossing;
        public final Kind kind;
        public final int lane;
        public boolean scored;
        public double streetX;
        public double velocity;
        public double z;

        public double worldX() {
            return this.crossing ? this.streetX : this.lane * 2.25d;
        }

        public Hazard(Kind kind, int i, double d) {
            this.kind = kind;
            this.lane = i;
            this.z = d;
        }

        public double depth() {
            if (this.kind == Kind.VAN) {
                return 3.8d;
            }
            return this.kind == Kind.GAP ? 2.4d : 1.0d;
        }
    }

    public static final class Coin {
        public final int lane;
        public final double y;
        public double z;

        Coin(int i, double d, double d2) {
            this.lane = i;
            this.y = d;
            this.z = d2;
        }
    }

    public void steer(double d) {
        if (this.streetMode && this.state == GameCore.State.PLAYING) {
            this.targetX = Math.max(-3.25d, Math.min(3.25d, this.targetX + d));
        }
    }

    public double target() {
        return this.streetMode ? this.targetX : this.lane * 2.25d;
    }

    public void start(long j) {
        this.state = GameCore.State.PLAYING;
        this.random = new Random(j);
        this.accumulator = 0.0d;
        this.vy = 0.0d;
        this.previousDistance = 0.0d;
        this.previousY = 0.0d;
        this.y = 0.0d;
        this.previousX = 0.0d;
        this.x = 0.0d;
        this.distance = 0.0d;
        this.time = 0.0d;
        this.targetX = 0.0d;
        this.closeCalls = 0;
        this.closeAt = -10.0d;
        this.lane = 0;
        this.rows = 0;
        this.bonus = 0;
        this.cleared = 0;
        this.collected = 0;
        this.combo = 1;
        this.landAt = -10.0d;
        this.slideUntil = -10.0d;
        this.nextRow = 1.6d;
        this.hazards.clear();
        this.coins.clear();
        this.events.clear();
    }

    public void pause() {
        if (this.state == GameCore.State.PLAYING) {
            this.state = GameCore.State.PAUSED;
            this.accumulator = 0.0d;
            this.events.clear();
        }
    }

    public void resume() {
        if (this.state == GameCore.State.PAUSED) {
            this.state = GameCore.State.PLAYING;
        }
    }

    public void menu() {
        this.state = GameCore.State.MENU;
        this.events.clear();
    }

    public int level() {
        return Routes.level(this.time);
    }

    public int route() {
        return Routes.index(this.time);
    }

    /** Seconds elapsed since the level-4 ramp began; zero through levels 1-3. */
    public double rampTime() {
        return Math.max(0.0d, this.time - RAMP_START);
    }

    public double speed() {
        if (this.streetMode) {
            return Math.min(STREET_MAX_SPEED, STREET_BASE_SPEED + (rampTime() * 0.02d));
        }
        return Math.min(LANE_MAX_SPEED, LANE_BASE_SPEED + (rampTime() * 0.085d));
    }

    public double cartSpeed() {
        return Math.min(CART_MAX_SPEED, CART_BASE_SPEED + (rampTime() / 150.0d));
    }

    public double cartInterval() {
        return Math.max(CART_INTERVAL_FLOOR, CART_INTERVAL_BASE - (rampTime() * 0.008d));
    }

    public double rowGap() {
        return Math.max(1.7d, 2.75d - (Math.max(0, level() - 3) * 0.13d));
    }

    public int score() {
        return ((int) (this.distance * 2.0d)) + this.bonus;
    }

    public boolean sliding() {
        return this.y < 0.05d && this.time < this.slideUntil;
    }

    public double renderX() {
        return this.previousX + (((this.x - this.previousX) * this.accumulator) / 0.008333333333333333d);
    }

    public double renderY() {
        return this.previousY + (((this.y - this.previousY) * this.accumulator) / 0.008333333333333333d);
    }

    public double renderDistance() {
        return this.previousDistance + (((this.distance - this.previousDistance) * this.accumulator) / 0.008333333333333333d);
    }

    public boolean act(Action action) {
        double d;
        double d2;
        if (this.state != GameCore.State.PLAYING) {
            return false;
        }
        if (this.streetMode && (action == Action.LEFT || action == Action.RIGHT)) {
            double d3 = this.targetX;
            steer(action == Action.LEFT ? -1.05d : 1.05d);
            return d3 != this.targetX;
        }
        if (action == Action.LEFT || action == Action.RIGHT) {
            int max = Math.max(-1, Math.min(1, this.lane + (action == Action.LEFT ? -1 : 1)));
            if (max == this.lane) {
                return false;
            }
            this.lane = max;
            return true;
        }
        if (action == Action.JUMP) {
            if (this.y > 0.0d || this.vy > 0.0d) {
                return false;
            }
            this.slideUntil = -10.0d;
            this.vy = 8.8d;
            this.events.add(GameCore.Event.JUMP);
            return true;
        }
        if (this.y > 0.0d) {
            this.vy = Math.min(this.vy, -12.0d);
            d = this.time;
            d2 = 0.95d;
        } else {
            d = this.time;
            d2 = 0.8d;
        }
        this.slideUntil = d + d2;
        return true;
    }

    public void advance(double d) {
        if (this.state == GameCore.State.PLAYING && Double.isFinite(d) && d > 0.0d) {
            if (d > 0.25d) {
                pause();
                return;
            }
            this.accumulator += d;
            while (this.accumulator + 1.0E-10d >= 0.008333333333333333d && this.state == GameCore.State.PLAYING) {
                step();
                this.accumulator = Math.max(0.0d, this.accumulator - 0.008333333333333333d);
            }
        }
    }

    private void step() {
        int level = level();
        double d = 0.008333333333333333d;
        this.time += 0.008333333333333333d;
        if (level() != level) {
            this.bonus += level() * 50;
            this.events.add(GameCore.Event.LEVEL);
        }
        this.previousX = this.x;
        this.previousY = this.y;
        this.previousDistance = this.distance;
        double target = target();
        this.x += Math.signum(target - this.x) * Math.min(Math.abs(target - this.x), 0.1171875d);
        double speed = speed() * 0.008333333333333333d;
        this.distance += speed;
        if (this.y > 0.0d || this.vy > 0.0d) {
            this.y += (this.vy * 0.008333333333333333d) - 7.638888888888888E-4d;
            this.vy -= 0.18333333333333332d;
            if (this.y <= 0.0d) {
                this.vy = 0.0d;
                this.y = 0.0d;
                this.landAt = this.time;
                this.events.add(GameCore.Event.LAND);
            }
        }
        if (this.spawning && this.time >= this.nextRow) {
            spawn();
        }
        int size = this.hazards.size() - 1;
        while (size >= 0) {
            Hazard hazard = this.hazards.get(size);
            hazard.z -= speed;
            if (hazard.crossing) {
                hazard.streetX += hazard.velocity * d;
                if (Math.abs(hazard.streetX) > 3.2d) {
                    hazard.streetX = Math.copySign(3.2d, hazard.streetX);
                    hazard.velocity = -hazard.velocity;
                }
            }
            double abs = Math.abs(this.x - hazard.worldX());
            boolean z = false;
            boolean z2 = abs < 0.86d;
            boolean z3 = Math.abs(hazard.z) < (hazard.depth() / 2.0d) + 0.28d;
            if (z3) {
                hazard.closest = Math.min(hazard.closest, abs);
            }
            if (hazard.kind != Kind.CRATE ? hazard.kind != Kind.BARRIER ? hazard.kind != Kind.GAP || this.y < 0.2d : !sliding() : this.y < 1.0d) {
                z = true;
            }
            if (z2 && z3 && z) {
                this.state = GameCore.State.OVER;
                this.events.add(GameCore.Event.HIT);
                return;
            }
            if (!hazard.scored && hazard.z < ((-hazard.depth()) / 2.0d) - 0.4d) {
                hazard.scored = true;
                this.cleared++;
                if (!this.streetMode) {
                    this.combo = Math.min(5, (this.cleared / 6) + 1);
                    this.bonus += this.combo * 15;
                } else if (hazard.closest < 0.86d || hazard.closest >= 1.35d) {
                    // clean pass but not close: chain resets
                    this.combo = 1;
                    this.bonus += 20;
                } else {
                    // close call: passed within 0.86-1.35 m
                    this.closeCalls++;
                    this.combo = Math.min(5, this.combo + 1);
                    this.bonus += this.combo * 100;
                    this.closeAt = this.time;
                }
                this.events.add(GameCore.Event.CLEAR);
            }
            if (hazard.z < -12.0d) {
                this.hazards.remove(size);
            }
            size--;
            d = 0.008333333333333333d;
        }
        for (int size2 = this.coins.size() - 1; size2 >= 0; size2--) {
            Coin coin = this.coins.get(size2);
            coin.z -= speed;
            if (Math.abs(coin.z) < 0.6d && Math.abs(this.x - (coin.lane * 2.25d)) < 0.65d && Math.abs((this.y + 0.95d) - coin.y) < 1.0d) {
                this.coins.remove(size2);
                this.collected++;
                this.bonus += this.combo * 25;
                this.events.add(GameCore.Event.COIN);
            } else if (coin.z < -9.0d) {
                this.coins.remove(size2);
            }
        }
    }

    private void spawn() {
        if (this.streetMode) {
            Hazard hazard = new Hazard(Kind.CRATE, 0, FIRST_CART_DISTANCE);
            hazard.crossing = true;
            hazard.streetX = this.rows % 2 == 0 ? -3.1d : 3.1d;
            hazard.velocity = (this.rows % 2 == 0 ? 1 : -1) * cartSpeed();
            this.hazards.add(hazard);
            this.rows++;
            this.nextRow = this.time + cartInterval();
            return;
        }
        int nextInt = this.random.nextInt(3) - 1;
        int i = level() < 4 ? 1 : 2;
        int i2 = ((nextInt + 2) % 3) - 1;
        int i3 = 0;
        while (i3 < i) {
            this.hazards.add(new Hazard(this.rows < 2 ? Kind.CRATE : Kind.values()[this.random.nextInt(Math.min(4, level() + 1))], i3 == 0 ? i2 : (-nextInt) - i2, 64.0d));
            i3++;
        }
        for (int i4 = 0; i4 < 5; i4++) {
            this.coins.add(new Coin(nextInt, 1.05d, 57.0d + (i4 * 2.7d)));
        }
        this.rows++;
        this.nextRow = this.time + rowGap() + (this.random.nextDouble() * 0.35d);
    }
}
