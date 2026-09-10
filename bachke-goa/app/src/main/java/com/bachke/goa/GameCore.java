package com.bachke.goa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* loaded from: classes.dex */
public final class GameCore {
    public static final double BOOST = 780.0d;
    public static final double DOUBLE_MAX = 0.32d;
    public static final double DOUBLE_MIN = 0.075d;
    public static final double GRAVITY = 1800.0d;
    public static final double JUMP = 720.0d;
    public static final double PLAYER_HEIGHT = 76.0d;
    public static final double PLAYER_WIDTH = 32.0d;
    public static final double PLAYER_X = 112.0d;
    public static final double STEP = 0.008333333333333333d;
    public static final double WORLD_WIDTH = 480.0d;
    private double accumulator;
    public int bonus;
    public double boostTime;
    public boolean boosted;
    public int cleared;
    public int collected;
    public double distance;
    public double landTime;
    public double previousDistance;
    public double previousY;
    private int spawnCount;
    public double time;
    public double velocity;
    public double y;
    public State state = State.MENU;
    public final List<Obstacle> obstacles = new ArrayList();
    public final List<Coin> coins = new ArrayList();
    public final List<Event> events = new ArrayList();
    public int combo = 1;
    private double firstTap = -999.0d;
    private double nextSpawn = 2.0d;
    private double bufferedUntil = -1.0d;
    private Random random = new Random(1);
    private int lastKind = -1;
    boolean spawning = true;

    public enum Event {
        JUMP,
        BOOST,
        LAND,
        COIN,
        CLEAR,
        HIT,
        LEVEL
    }

    public enum Motion {
        RUN,
        RISE,
        BOOST,
        FALL,
        LAND,
        HIT
    }

    public enum State {
        MENU,
        PLAYING,
        PAUSED,
        OVER
    }

    public static final class Obstacle {
        public final double extraSpeed;
        public final double height;
        public final int kind;
        public boolean scored;
        public final double width;
        public double x;

        public Obstacle(int i, double d) {
            double d2;
            double d3;
            this.kind = i;
            this.x = d;
            if (i < 0 || i > 5) {
                throw new IllegalArgumentException("Unknown obstacle");
            }
            if (i == 1) {
                d2 = 78.0d;
            } else if (i == 2) {
                d2 = 58.0d;
            } else if (i == 3) {
                d2 = 84.0d;
            } else {
                d2 = i == 4 ? 64.0d : i == 5 ? 112.0d : 52.0d;
            }
            this.width = d2;
            if (i == 1 || i == 5) {
                d3 = 0.0d;
            } else if (i == 2) {
                d3 = 184.0d;
            } else if (i == 3) {
                d3 = 68.0d;
            } else {
                d3 = i == 4 ? 212.0d : 48.0d;
            }
            this.height = d3;
            this.extraSpeed = i != 3 ? 0.0d : 48.0d;
        }

        public boolean requiresBoost() {
            return this.kind == 2 || this.kind == 4;
        }

        public boolean gap() {
            return this.kind == 1 || this.kind == 5;
        }
    }

    public static final class Coin {
        public final double extraSpeed;
        public double x;
        public final double y;

        public Coin(double d, double d2) {
            this(d, d2, 0.0d);
        }

        public Coin(double d, double d2, double d3) {
            this.x = d;
            this.y = d2;
            this.extraSpeed = d3;
        }
    }

    public void start(long j) {
        this.random = new Random(j);
        this.state = State.PLAYING;
        this.accumulator = 0.0d;
        this.velocity = 0.0d;
        this.previousY = 0.0d;
        this.y = 0.0d;
        this.previousDistance = 0.0d;
        this.distance = 0.0d;
        this.time = 0.0d;
        this.spawnCount = 0;
        this.bonus = 0;
        this.cleared = 0;
        this.collected = 0;
        this.combo = 1;
        this.lastKind = -1;
        this.firstTap = -999.0d;
        this.boostTime = -999.0d;
        this.landTime = -999.0d;
        this.bufferedUntil = -1.0d;
        this.nextSpawn = 2.0d;
        this.boosted = false;
        this.obstacles.clear();
        this.coins.clear();
        this.events.clear();
    }

    public void menu() {
        this.state = State.MENU;
        this.accumulator = 0.0d;
        this.events.clear();
    }

    public void pause() {
        if (this.state == State.PLAYING) {
            this.state = State.PAUSED;
            this.accumulator = 0.0d;
            this.bufferedUntil = -1.0d;
            this.events.clear();
        }
    }

    public void resume() {
        if (this.state == State.PAUSED) {
            this.state = State.PLAYING;
            this.accumulator = 0.0d;
            this.firstTap = -999.0d;
        }
    }

    public boolean tap() {
        if (this.state != State.PLAYING) {
            return false;
        }
        if (this.y <= 0.0d && this.velocity <= 0.0d) {
            launch();
            return true;
        }
        double d = this.time - this.firstTap;
        if (this.boosted || d < 0.074999999d || d > 0.32000000100000003d) {
            if (this.velocity < 0.0d && this.y < 32.0d) {
                this.bufferedUntil = this.time + 0.1d;
            }
            return false;
        }
        this.velocity = 780.0d;
        this.boosted = true;
        this.boostTime = this.time;
        this.events.add(Event.BOOST);
        return true;
    }

    private void launch() {
        this.velocity = 720.0d;
        this.firstTap = this.time;
        this.boosted = false;
        this.bufferedUntil = -1.0d;
        this.events.add(Event.JUMP);
    }

    public int score() {
        return ((int) Math.floor((this.time * 8.0d) + 1.0E-8d)) + this.bonus;
    }

    public int stage() {
        return level() - 1;
    }

    public int level() {
        return Routes.level(this.time);
    }

    public int route() {
        return Routes.index(this.time);
    }

    public String stageName() {
        return "L" + level() + " · " + Routes.NAMES[route()];
    }

    public double speed() {
        return Math.min(520.0d, (this.time * 1.8d) + 230.0d);
    }

    public double minimumGap() {
        return Math.max(1.68d, 2.18d - (Math.min(stage(), 10) * 0.055d));
    }

    public double obstacleSpeed(Obstacle obstacle) {
        return speed() + obstacle.extraSpeed;
    }

    public double jumpLead(Obstacle obstacle) {
        if (obstacle.kind == 4) {
            return 0.47d;
        }
        if (obstacle.kind == 2) {
            return 0.4d;
        }
        return obstacle.kind == 5 ? 0.22d : 0.3d;
    }

    public Motion motion() {
        return this.state == State.OVER ? Motion.HIT : this.y > 0.0d ? this.velocity <= 0.0d ? Motion.FALL : this.boosted ? Motion.BOOST : Motion.RISE : this.time - this.landTime < 0.09d ? Motion.LAND : Motion.RUN;
    }

    public double interpolation() {
        return this.accumulator / 0.008333333333333333d;
    }

    public double renderY() {
        return this.previousY + ((this.y - this.previousY) * interpolation());
    }

    public double renderDistance() {
        return this.previousDistance + ((this.distance - this.previousDistance) * interpolation());
    }

    public void advance(double d) {
        if (this.state == State.PLAYING && Double.isFinite(d) && d > 0.0d) {
            if (d > 0.25d) {
                pause();
                return;
            }
            this.accumulator += d;
            while (this.accumulator + 1.0E-10d >= 0.008333333333333333d && this.state == State.PLAYING) {
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
            this.events.add(Event.LEVEL);
        }
        double speed = speed() * 0.008333333333333333d;
        this.previousDistance = this.distance;
        this.distance += speed;
        this.previousY = this.y;
        if (this.y > 0.0d || this.velocity > 0.0d) {
            this.y += (this.velocity * 0.008333333333333333d) - 0.0625d;
            this.velocity -= 15.0d;
            if (this.y <= 0.0d) {
                this.y = 0.0d;
                this.velocity = 0.0d;
                this.boosted = false;
                this.landTime = this.time;
                this.events.add(Event.LAND);
                if (this.bufferedUntil >= this.time) {
                    launch();
                }
            }
        }
        if (this.spawning && this.time >= this.nextSpawn) {
            spawn();
        }
        int size = this.obstacles.size() - 1;
        while (size >= 0) {
            Obstacle obstacle = this.obstacles.get(size);
            obstacle.x -= (obstacle.extraSpeed * d) + speed;
            if (128.0d > obstacle.x + 5.0d && 96.0d < (obstacle.x + obstacle.width) - 5.0d) {
                if (obstacle.gap()) {
                    if (this.y < 12.0d) {
                        this.state = State.OVER;
                        this.events.add(Event.HIT);
                        return;
                    }
                } else if (this.y + 3.0d < obstacle.height) {
                    this.state = State.OVER;
                    this.events.add(Event.HIT);
                    return;
                }
            }
            if (!obstacle.scored && obstacle.x + obstacle.width < 96.0d) {
                obstacle.scored = true;
                this.cleared++;
                this.combo = Math.min(5, (this.cleared / 3) + 1);
                this.bonus += (obstacle.requiresBoost() ? 35 : 15) * this.combo;
                this.events.add(Event.CLEAR);
            }
            if (obstacle.x + obstacle.width < -30.0d) {
                this.obstacles.remove(size);
            }
            size--;
            d = 0.008333333333333333d;
        }
        for (int size2 = this.coins.size() - 1; size2 >= 0; size2--) {
            Coin coin = this.coins.get(size2);
            coin.x -= (coin.extraSpeed * 0.008333333333333333d) + speed;
            if (Math.abs(coin.x - 112.0d) < 29.0d && coin.y > this.y - 9.0d && coin.y < this.y + 76.0d + 9.0d) {
                this.coins.remove(size2);
                this.collected++;
                this.bonus += this.combo * 25;
                this.events.add(Event.COIN);
            } else if (coin.x < -30.0d) {
                this.coins.remove(size2);
            }
        }
    }

    private void spawn() {
        int nextInt;
        double nextDouble = this.random.nextDouble();
        if (this.spawnCount < 2) {
            nextInt = 0;
        } else if (stage() == 0) {
            nextInt = this.random.nextInt(2);
        } else if (stage() >= 2 && nextDouble < 0.2d) {
            nextInt = 3;
        } else if (stage() < 4 || nextDouble >= 0.34d) {
            nextInt = nextDouble < Math.min(0.78d, (((double) stage()) * 0.025d) + 0.42d) ? (stage() < 3 || !this.random.nextBoolean()) ? 2 : 4 : this.random.nextInt(2);
        } else {
            nextInt = 5;
        }
        if (nextInt == this.lastKind && (nextInt == 2 || nextInt == 4)) {
            nextInt = stage() >= 2 ? 3 : 1;
        }
        Obstacle obstacle = new Obstacle(nextInt, Math.max(72.0d, speed() * 0.3d) + 480.0d);
        this.obstacles.add(obstacle);
        double d = obstacle.requiresBoost() ? 256.0d : 112.0d;
        int i = 0;
        for (int i2 = 3; i < i2; i2 = 3) {
            this.coins.add(new Coin((i * 40) + (obstacle.x - 42.0d), d + (i == 1 ? 18 : 0), obstacle.extraSpeed));
            i++;
        }
        this.spawnCount++;
        this.lastKind = nextInt;
        this.nextSpawn = this.time + minimumGap() + (this.random.nextDouble() * Math.max(0.2d, 0.55d - (stage() * 0.025d)));
    }
}
