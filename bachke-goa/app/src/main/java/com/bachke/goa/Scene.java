package com.bachke.goa;

import com.bachke.goa.GameCore;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public final class Scene {
    public static final int CREAM = -2594;
    public static final int GOLD = -13226;
    public static final int INK = -15454917;
    public static final int TEAL = -15157331;
    public double ambience;
    public int best;
    public int character;
    public final GameCore game;
    public boolean night;
    public int skin;
    public double toastUntil;
    public boolean sound = true;
    public boolean haptics = true;
    public double height = 960.0d;
    public String toast = "";
    public final HomeScreen home = new HomeScreen(this);
    private final CharacterRig[] rigs = {new CharacterRig(), new CharacterRig(), new CharacterRig(), new CharacterRig()};

    public Scene(GameCore gameCore) {
        this.game = gameCore;
    }

    public double ground() {
        return this.height * 0.77d;
    }
    public void draw(Painter painter) {
        double d;
        boolean z;
        int r14;
        int i;
        if (this.game.state == GameCore.State.MENU) {
            this.home.draw(painter);
            return;
        }
        double ground = ground();
        int i2 = this.night ? -15455921 : -8070940;
        painter.rect(0.0d, 0.0d, 480.0d, this.height, i2, 0.0d);
        double max = Math.max(330.0d, 0.65d * ground);
        int route = this.game.state == GameCore.State.MENU ? 0 : this.game.route();
        double progress = Routes.progress(this.game.time) * 20.0d;
        double speed = this.game.state == GameCore.State.MENU ? this.ambience * 11.0d : this.game.speed() * progress * 0.22d;
        double min = (this.game.state == GameCore.State.MENU || this.game.level() <= 1) ? 1.0d : Math.min(1.0d, progress / 1.0d);
        if (min < 1.0d) {
            d = 1.0d;
            background(painter, (route + 4) % 5, max, this.game.speed() * 20.0d * 0.22d, 1.0d);
        } else {
            d = 1.0d;
        }
        background(painter, route, max, speed, min);
        double floor = Math.floor((ground - max) + 6.0d);
        painter.rect(0.0d, floor, 480.0d, 8.0d, i2, 0.0d);
        for (int i3 = 0; i3 < 92; i3++) {
            double d2 = i3;
            painter.rect(0.0d, floor + 8.0d + d2, 480.0d, 1.0d, (((int) (Math.pow(d - (d2 / 92.0d), 1.8d) * 255.0d)) << 24) | (i2 & 16777215), 0.0d);
        }
        int i4 = 0;
        while (true) {
            if (i4 >= 7) {
                break;
            }
            double d3 = (((i4 * 113) + (this.ambience * (this.night ? 3 : 7))) % 560.0d) - 40.0d;
            double d4 = i4;
            double sin = ((i4 % 3) * 43) + 110 + (Math.sin((this.ambience * 0.7d) + d4) * 7.0d);
            if (this.night) {
                painter.ellipse(d3, sin, 3.0d, 3.0d, -1711279176);
                i = i4;
            } else {
                double sin2 = sin + (Math.sin((this.ambience * 4.0d) + d4) * 4.0d);
                i = i4;
                painter.line(d3 - 6.0d, sin2, d3, sin, 1.5d, -2141024617);
                painter.line(d3, sin, d3 + 6.0d, sin2, 1.5d, -2141024617);
            }
            i4 = i + 1;
        }
        if (!this.night) {
            for (int i5 = 0; i5 < 3; i5++) {
                double d5 = (((i5 * 210) + (this.ambience * 4.0d)) % 680.0d) - 100.0d;
                double d6 = (i5 * 43) + 170;
                painter.ellipse(d5, d6, 106.0d, 21.0d, 905969663);
                painter.ellipse(d5 + 26.0d, d6 - 11.0d, 52.0d, 26.0d, 905969663);
            }
        }
        painter.rect(0.0d, ground, 480.0d, this.height - ground, this.night ? -13616560 : -4351882, 0.0d);
        painter.rect(0.0d, ground, 480.0d, 6.0d, this.night ? -1590412 : -8543, 0.0d);
        painter.rect(0.0d, ground + 7.0d, 480.0d, 9.0d, this.night ? -15195337 : -9348276, 0.0d);
        double renderDistance = this.game.state == GameCore.State.MENU ? this.ambience * 40.0d : this.game.renderDistance();
        int i6 = 0;
        while (i6 < 4) {
            double d7 = 38.0d + ground + (i6 * 42);
            int i7 = i6;
            painter.line(0.0d, d7, 480.0d, d7, 1.0d, this.night ? 811563930 : 1079725383);
            for (int i8 = -1; i8 < 8; i8++) {
                double d8 = ((i8 * 94) - (renderDistance % 94.0d)) + ((i7 % 2) * 47);
                painter.line(d8, d7 - 37.0d, d8 - 18.0d, d7, 1.0d, this.night ? 811563930 : 1079725383);
            }
            i6 = i7 + 1;
        }
        if (this.game.state != GameCore.State.MENU) {
            for (GameCore.Coin coin : this.game.coins) {
                coin(painter, coin.x, ground - coin.y);
            }
            Iterator<GameCore.Obstacle> it = this.game.obstacles.iterator();
            while (it.hasNext()) {
                obstacle(painter, it.next(), ground);
            }
            player(painter, 112.0d, ground - this.game.renderY(), 106.0d, pose(), (this.character * 2) + this.skin);
        }
        if (this.game.state == GameCore.State.MENU) {
            this.home.draw(painter);
            return;
        }
        hud(painter);
        if (this.game.state != GameCore.State.PLAYING) {
            z = true;
        } else {
            String str = this.game.time < 5.0d ? "TAP TO JUMP" : this.game.time < 12.0d ? "TAP TWICE QUICKLY TO BOOST" : (this.game.time <= 17.0d || this.game.time >= 23.0d) ? "" : "TALL BARRIERS NEED A DOUBLE TAP";
            if (!str.isEmpty()) {
                pill(painter, str, 240.0d, ground + 95.0d, 13);
            }
            if (this.game.time < this.toastUntil) {
                pill(painter, this.toast, 240.0d, ground - 290.0d, 16);
            }
            if (progress < 2.5d) {
                r14 = 1;
                r14 = 1;
                if (this.game.level() > 1) {
                    pill(painter, "LEVEL " + this.game.level() + "  ·  " + Routes.NAMES[route].toUpperCase(Locale.US), 240.0d, 204.0d, 16);
                }
            } else {
                r14 = 1;
            }
            GameCore.Obstacle obstacle = null;
            for (GameCore.Obstacle obstacle2 : this.game.obstacles) {
                if (obstacle2.x > 112.0d && (obstacle == null || obstacle2.x < obstacle.x)) {
                    obstacle = obstacle2;
                }
            }
            if (obstacle != null && obstacle.x > 425.0d && this.game.level() > r14) {
                pill(painter, obstacle.requiresBoost() ? "BOOST →" : obstacle.kind == 3 ? "SCOOTER →" : "JUMP →", 396.0d, 251.0d, 11);
            }
            button(painter, "JUMP  /  DOUBLE TAP", 34.0d, this.height - 94.0d, 412.0d, 59.0d, false);
            z = r14 != 0;
        }
        if (this.game.state == GameCore.State.PAUSED) {
            overlay(painter, false);
        }
        if (this.game.state == GameCore.State.OVER) {
            overlay(painter, z);
        }
    }

    private void background(Painter painter, int i, double d, double d2, double d3) {
        double d4 = d * (i == 3 ? 3.0d : 6.0d);
        double d5 = ((new double[]{0.56d, 0.35d, 0.48d, 0.12d, 0.27d}[i] * d4) + d2) % (2.0d * d4);
        int i2 = -1;
        for (int i3 = 3; i2 < i3; i3 = i3) {
            painter.image(Routes.ASSETS[i], 1, 2, this.night ? 1 : 0, (i2 * d4) - d5, 8.0d + (ground() - d), d4, d, Math.floorMod(i2, 2) != 0, d3);
            i2++;
        }
    }

    private int pose() {
        switch (this.game.motion()) {
            case RISE:
                return 4;
            case BOOST:
                return 5;
            case FALL:
                return 6;
            case LAND:
            case HIT:
                return 7;
            default:
                return ((int) (this.game.distance / 34.0d)) % 4;
        }
    }

    private void player(Painter painter, double d, double d2, double d3, int i, int i2) {
        double max = Math.max(24.0d, 65.0d - (Math.max(0.0d, ground() - d2) * 0.12d));
        painter.ellipse(d - (max / 2.0d), ground() - 3.0d, max, 10.0d, this.night ? 1879511071 : 1346252845);
        this.rigs[i2].draw(painter, i2, d, d2, d3 / 106.0d, this.game.renderDistance(), this.game.time, this.game.motion());
        if (this.game.state == GameCore.State.PLAYING && this.game.time - this.game.boostTime < 0.25d) {
            for (int i3 = 0; i3 < 4; i3++) {
                double d4 = i3 * 6;
                double d5 = d2 + (i3 * 4) + ((this.game.time - this.game.boostTime) * 90.0d);
                painter.line((d - 28.0d) - d4, d5, (d - 24.0d) - d4, d5 + 14.0d, 3.0d, GOLD);
            }
        }
        if (this.game.state == GameCore.State.PLAYING && this.game.y == 0.0d && i < 4) {
            double d6 = (this.game.distance % 35.0d) / 35.0d;
            painter.ellipse((d - 36.0d) - (20.0d * d6), (d2 - 3.0d) - (8.0d * d6), 9.0d - (d6 * 5.0d), 4.0d, 1627381690);
        }
    }

    private void coin(Painter painter, double d, double d2) {
        double abs = (Math.abs(Math.cos((this.ambience * 4.0d) + (0.01d * d))) * 6.0d) + 13.0d;
        double d3 = d - (abs / 2.0d);
        painter.ellipse(d3 - 2.0d, d2 - 12.0d, abs + 4.0d, 24.0d, -6331605);
        painter.ellipse(d3, d2 - 11.0d, abs, 20.0d, GOLD);
        painter.line(d, d2 - 6.0d, d, d2 + 5.0d, 2.0d, -3913);
    }

    private void obstacle(Painter painter, GameCore.Obstacle obstacle, double d) {
        double d2 = obstacle.x;
        if (obstacle.gap()) {
            painter.ellipse(d2 - 3.0d, d - 5.0d, obstacle.width + 6.0d, 20.0d, -9083561);
            painter.ellipse(d2 + 3.0d, d - 3.0d, obstacle.width - 6.0d, 13.0d, -15259590);
            double d3 = d + 3.0d;
            painter.line(d2 + 16.0d, d3, d2 + 34.0d, d3, 2.0d, -8741972);
            return;
        }
        if (obstacle.kind != 3) {
            if (obstacle.kind == 4) {
                for (int i = 0; i < 4; i++) {
                    double d4 = d - ((i + 1) * 53);
                    painter.rect(d2, d4, obstacle.width, 51.0d, -10403281, 3.0d);
                    painter.rect(d2 + 3.0d, d4 + 3.0d, obstacle.width - 6.0d, 43.0d, -4750261, 3.0d);
                    double d5 = d2 + 7.0d;
                    double d6 = d4 + 7.0d;
                    double d7 = d4 + 43.0d;
                    painter.line(d5, d6, (obstacle.width + d2) - 7.0d, d7, 4.0d, -1066389);
                    painter.line((obstacle.width + d2) - 7.0d, d6, d5, d7, 4.0d, -7839680);
                }
                pill(painter, "HIGH! 2×", d2 + (obstacle.width / 2.0d), (d - obstacle.height) - 17.0d, 11);
                return;
            }
            if (obstacle.kind != 0) {
                painter.rect(d2, d - obstacle.height, 8.0d, obstacle.height, INK, 2.0d);
                painter.rect((obstacle.width + d2) - 8.0d, d - obstacle.height, 8.0d, obstacle.height, INK, 2.0d);
                for (int i3 = 0; i3 < 5; i3++) {
                    double d8 = (d - obstacle.height) + 5.0d + (i3 * 34);
                    painter.rect(d2 - 3.0d, d8, obstacle.width + 6.0d, 24.0d, GOLD, 3.0d);
                    double d9 = d8 + 21.0d;
                    double d10 = d8 + 3.0d;
                    painter.line(d2 + 3.0d, d9, d2 + 22.0d, d10, 7.0d, -1215664);
                    painter.line(d2 + 30.0d, d9, d2 + 49.0d, d10, 7.0d, -1215664);
                }
                double d11 = d - 7.0d;
                painter.rect(d2 - 9.0d, d11, 22.0d, 8.0d, INK, 2.0d);
                painter.rect((obstacle.width + d2) - 13.0d, d11, 22.0d, 8.0d, INK, 2.0d);
                pill(painter, "2× TAP", d2 + (obstacle.width / 2.0d), (d - obstacle.height) - 20.0d, 12);
                return;
            }
            painter.rect(d2, d - 48.0d, 52.0d, 48.0d, -10403281, 4.0d);
            double d12 = d - 46.0d;
            painter.rect(d2 + 3.0d, d12, 46.0d, 42.0d, -3831218, 3.0d);
            double d13 = d2 + 5.0d;
            double d14 = d - 43.0d;
            double d15 = 46.0d + d2;
            double d16 = d - 7.0d;
            painter.line(d13, d14, d15, d16, 5.0d, -1331353);
            painter.line(d15, d14, d13, d16, 5.0d, -8235212);
            painter.line(d2, d12, d2 + 52.0d, d12, 4.0d, -473727);
            return;
        }
        double d17 = d - 23.0d;
        painter.ellipse(d2 + 1.0d, d17, 25.0d, 25.0d, INK);
        painter.ellipse(d2 + 57.0d, d17, 25.0d, 25.0d, INK);
        double d18 = d - 17.0d;
        painter.ellipse(d2 + 7.0d, d18, 13.0d, 13.0d, -5650488);
        painter.ellipse(d2 + 63.0d, d18, 13.0d, 13.0d, -5650488);
        painter.rect(d2 + 12.0d, d - 44.0d, 57.0d, 25.0d, -1215664, 10.0d);
        painter.rect(d2 + 44.0d, d - 66.0d, 12.0d, 40.0d, -1215664, 6.0d);
        painter.rect(d2 + 15.0d, d - 50.0d, 31.0d, 8.0d, INK, 4.0d);
        double d19 = d2 + 49.0d;
        double d20 = d - 64.0d;
        painter.line(d19, d20, d2 + 69.0d, d20, 5.0d, INK);
        painter.ellipse(d19, d - 63.0d, 10.0d, 10.0d, GOLD);
        double d21 = d - 33.0d;
        painter.line(d2 + 3.0d, d21, d2 - 14.0d, d21, 2.0d, this.night ? -2130706433 : -2144057004);
    }

    private void hud(Painter painter) {
        painter.rect(20.0d, 35.0d, 244.0d, 91.0d, -283890373, 20.0d);
        painter.text("SCORE", 38.0d, 58.0d, 10.0d, -5388597, true, false);
        painter.text(String.format(Locale.US, "%05d", Integer.valueOf(this.game.score())), 38.0d, 94.0d, 32.0d, CREAM, true, false);
        painter.text("×" + this.game.combo, 224.0d, 89.0d, 24.0d, GOLD, true, true);
        painter.text(this.game.stageName() + "   ·   " + this.game.collected + " coins", 39.0d, 114.0d, 11.0d, -5388597, false, false);
        button(painter, "Ⅱ", 396.0d, 35.0d, 62.0d, 58.0d, false);
        double progress = Routes.progress(this.game.time);
        painter.rect(22.0d, 137.0d, 436.0d, 6.0d, 1612921162, 3.0d);
        painter.rect(22.0d, 137.0d, progress * 436.0d, 6.0d, GOLD, 3.0d);
        String str = "NEXT: " + Routes.NAMES[(this.game.route() + 1) % 5].toUpperCase(Locale.US) + "  ·  " + ((int) Math.ceil(20.0d - (progress * 20.0d))) + "s";
        boolean z = this.night;
        int i = INK;
        painter.text(str, 240.0d, 164.0d, 11.0d, z ? -2594 : -15454917, true, true);
        if (this.game.y > 0.0d && !this.game.boosted && this.game.time < 12.0d) {
            if (this.night) {
                i = GOLD;
            }
            painter.text("TAP AGAIN", 240.0d, 197.0d, 15.0d, i, true, true);
        }
    }

    private void overlay(Painter painter, boolean z) {
        painter.rect(0.0d, 0.0d, 480.0d, this.height, -1525669836, 0.0d);
        double d = (this.height / 2.0d) - 172.0d;
        painter.rect(28.0d, d, 424.0d, 344.0d, CREAM, 26.0d);
        painter.text(z ? "ARRE! BACHKE." : "TAKE A BREATHER.", 240.0d, d + 58.0d, 27.0d, INK, true, true);
        painter.text(z ? "One more run?" : "Your run is safely paused.", 240.0d, d + 87.0d, 15.0d, -12094858, false, true);
        GameCore gameCore = this.game;
        painter.text(z ? Integer.toString(gameCore.score()) : gameCore.stageName(), 240.0d, d + 143.0d, z ? 45.0d : 25.0d, INK, true, true);
        painter.text(z ? "BEST " + Math.max(this.best, this.game.score()) + "   ·   " + this.game.collected + " COINS" : "Tap resume when you are ready.", 240.0d, d + 174.0d, 12.0d, -12094858, true, true);
        button(painter, z ? "RUN AGAIN  →" : "RESUME  →", 52.0d, d + 199.0d, 376.0d, 59.0d, true);
        button(painter, "BACK TO HOME", 52.0d, d + 274.0d, 376.0d, 46.0d, false);
    }

    private void pill(Painter painter, String str, double d, double d2, int i) {
        double length = (str.length() * i * 0.59d) + 26.0d;
        painter.rect(d - (length / 2.0d), d2 - 23.0d, length, 34.0d, -350999237, 17.0d);
        painter.text(str, d, d2, i, GOLD, true, true);
    }

    private void button(Painter painter, String str, double d, double d2, double d3, double d4, boolean z) {
        painter.rect(d, d2 + 4.0d, d3, d4, -2143148763, 15.0d);
        painter.rect(d, d2, d3, d4, z ? GOLD : -267113157, 15.0d);
        painter.text(str, d + (d3 / 2.0d), d2 + (d4 / 2.0d) + 5.0d, d4 >= 59.0d ? 19.0d : 12.0d, z ? INK : CREAM, true, true);
    }

    public String touch(double d, double d2) {
        if (this.game.state == GameCore.State.MENU) {
            return this.home.touch(d, d2);
        }
        if (this.game.state == GameCore.State.PLAYING) {
            if (d < 386.0d || d2 < 25.0d || d2 > 105.0d) {
                this.game.tap();
                return "tap";
            }
            this.game.pause();
            return "pause";
        }
        double d3 = (this.height / 2.0d) - 172.0d;
        if (d >= 52.0d && d <= 428.0d && d2 >= 199.0d + d3 && d2 <= 258.0d + d3) {
            if (this.game.state == GameCore.State.OVER) {
                return "start";
            }
            this.game.resume();
            return "resume";
        }
        if (d < 52.0d || d > 428.0d || d2 < 274.0d + d3 || d2 > d3 + 320.0d) {
            return "none";
        }
        this.game.menu();
        return "menu";
    }
}
