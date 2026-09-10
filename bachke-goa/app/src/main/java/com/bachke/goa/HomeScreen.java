package com.bachke.goa;

import com.bachke.goa.GameCore;
import java.util.Locale;

/* loaded from: classes.dex */
public final class HomeScreen {
    private static final int GOLD = -13226;
    private static final int INK = -15454917;
    private static final int WHITE = -2594;
    public boolean modelBackdrop;
    public Page page = Page.HOME;
    private final CharacterRig[] rigs = {new CharacterRig(), new CharacterRig(), new CharacterRig(), new CharacterRig()};
    private final Scene s;
    public int tour;

    public enum Page {
        HOME,
        CREW,
        TOUR,
        HELP,
        SETTINGS
    }

    HomeScreen(Scene scene) {
        this.s = scene;
    }

    public boolean back() {
        if (this.page == Page.HOME) {
            return false;
        }
        this.page = Page.HOME;
        return true;
    }

    private boolean hit(double d, double d2, double d3, double d4, double d5, double d6) {
        return d >= d3 && d <= d3 + d5 && d2 >= d4 && d2 <= d4 + d6;
    }

    private void panel(Painter painter, double d, double d2, double d3, double d4, int i) {
        painter.rect(d, d2 + 5.0d, d3, d4, 1428172863, 22.0d);
        painter.rect(d, d2, d3, d4, i, 22.0d);
    }

    private void button(Painter painter, String str, double d, double d2, double d3, double d4, boolean z) {
        panel(painter, d, d2, d3, d4, z ? -13226 : -15454917);
        painter.rect(d + 12.0d, d2 + 5.0d, d3 - 24.0d, 3.0d, 905969663, 2.0d);
        painter.text(str, d + (d3 / 2.0d), d2 + (d4 / 2.0d) + 6.0d, d4 > 65.0d ? 25.0d : 15.0d, z ? -15454917 : -2594, true, true);
    }

    private String who() {
        return this.s.character == 0 ? "JOJO" : "MAYA";
    }

    private String outfit() {
        return this.s.skin == 0 ? "Everyday" : "Carnival";
    }

    private void runner(Painter painter, int i, double d, double d2, double d3) {
        if (this.modelBackdrop && this.page == Page.HOME) {
            return;
        }
        double d4 = d3 * 70.0d;
        painter.ellipse(d - (d4 / 2.0d), d2 - 5.0d, d4, 14.0d, 1429357899);
        this.rigs[i].draw(painter, i, d, d2, d3, this.s.ambience * 125.0d, this.s.ambience, GameCore.Motion.RUN);
    }

    private void world(Painter painter) {
        if (this.modelBackdrop) {
            return;
        }
        int i = this.page == Page.TOUR ? this.tour : 0;
        int i2 = this.s.night ? -15258030 : -8334113;
        double d = this.s.height - 160.0d;
        double d2 = d * (i != 3 ? 6 : 3);
        double d3 = (new double[]{0.56d, 0.35d, 0.48d, 0.12d, 0.27d}[i] * d2) + ((this.s.ambience * 6.0d) % 180.0d);
        painter.rect(0.0d, 0.0d, 480.0d, this.s.height, i2, 0.0d);
        for (int i3 = -1; i3 < 2; i3++) {
            painter.image(Routes.ASSETS[i], 1, 2, this.s.night ? 1 : 0, (i3 * d2) - d3, 100.0d, d2, d, i3 % 2 != 0, 1.0d);
        }
        for (int i4 = 0; i4 < 96; i4++) {
            painter.rect(0.0d, i4 + 90, 480.0d, 1.0d, (((int) (Math.max(0.0d, 1.0d - (i4 / 96.0d)) * 255.0d)) << 24) | (16777215 & i2), 0.0d);
        }
        for (int i5 = 0; i5 < 5; i5++) {
            double d4 = (((i5 * 109) + (this.s.ambience * 8.0d)) % 530.0d) - 25.0d;
            double d5 = ((i5 % 3) * 48) + 210;
            if (this.s.night) {
                painter.ellipse(d4, d5, 4.0d, 4.0d, -1711278642);
            } else {
                double sin = d5 + (Math.sin((this.s.ambience * 4.0d) + i5) * 4.0d);
                painter.line(d4 - 5.0d, sin, d4, d5, 2.0d, 1888005816);
                painter.line(d4, d5, d4 + 5.0d, sin, 2.0d, 1888005816);
            }
        }
        for (int i6 = 0; i6 < 180; i6++) {
            painter.rect(0.0d, (this.s.height - 180.0d) + i6, 480.0d, 1.0d, (((int) ((i6 * 225) / 180.0d)) << 24) | 1322299, 0.0d);
        }
    }

    public void draw(Painter painter) {
        String str;
        world(painter);
        if (this.page == Page.HOME) {
            home(painter);
            return;
        }
        painter.rect(0.0d, 0.0d, 480.0d, this.s.height, 1880370491, 0.0d);
        panel(painter, 18.0d, 24.0d, 444.0d, this.s.height - 48.0d, -134220322);
        if (this.page == Page.CREW) {
            str = "YOUR CREW";
        } else if (this.page == Page.TOUR) {
            str = "THE GOA TOUR";
        } else {
            str = this.page == Page.HELP ? "GET SET. GO!" : "SETTINGS";
        }
        painter.text(str, 40.0d, 78.0d, 28.0d, -15454917, true, false);
        button(painter, "×", 388.0d, 42.0d, 52.0d, 48.0d, false);
        if (this.page != Page.CREW) {
            if (this.page != Page.TOUR) {
                if (this.page != Page.HELP) {
                    settings(painter);
                    return;
                } else {
                    help(painter);
                    return;
                }
            }
            tour(painter);
            return;
        }
        crew(painter);
    }

    private void home(Painter painter) {
        panel(painter, 22.0d, 28.0d, 170.0d, 51.0d, -15454917);
        painter.text("PERSONAL BEST", 37.0d, 47.0d, 10.0d, -4662826, true, false);
        painter.text(Integer.toString(this.s.best), 37.0d, 69.0d, 21.0d, -13226, true, false);
        button(painter, this.s.night ? "NIGHT" : "DAY", 276.0d, 28.0d, 91.0d, 51.0d, false);
        button(painter, "•••", 382.0d, 28.0d, 76.0d, 51.0d, false);
        painter.text("BACHKE!", 243.0d, 155.0d, 66.0d, -15454917, true, true);
        painter.text("BACHKE!", 238.0d, 149.0d, 66.0d, -13226, true, true);
        painter.text("G O A", 240.0d, 189.0d, 26.0d, -2594, true, true);
        double d = this.s.height - 300.0d;
        if (!this.modelBackdrop) {
            painter.ellipse(107.0d, d - 250.0d, 266.0d, 266.0d, 822080990);
            painter.ellipse(134.0d, d - 210.0d, 212.0d, 212.0d, 637534207);
        }
        runner(painter, (this.s.character * 2) + this.s.skin, 239.0d, d, 2.15d);
        double d2 = d - 124.0d;
        button(painter, "‹", 28.0d, d2, 52.0d, 56.0d, false);
        button(painter, "›", 400.0d, d2, 52.0d, 56.0d, false);
        panel(painter, 118.0d, d + 17.0d, 244.0d, 65.0d, -283890373);
        painter.text(who(), 240.0d, d + 45.0d, 24.0d, -2594, true, true);
        painter.text(outfit() + "  ·  Ready to run", 240.0d, d + 68.0d, 12.0d, -4662826, false, true);
        button(painter, "PLAY  →", 36.0d, this.s.height - 196.0d, 408.0d, 76.0d, true);
        painter.text("PANJIM START  ·  ENDLESS GOA RUN", 240.0d, this.s.height - 102.0d, 10.0d, -2594, true, true);
        String[] strArr = {"CREW", "GOA TOUR", "HOW TO PLAY"};
        for (int i = 0; i < 3; i++) {
            button(painter, strArr[i], (i * 148) + 22, this.s.height - 82.0d, 140.0d, 58.0d, false);
        }
    }

    private void crew(Painter painter) {
        painter.text("Pick your runner. Make it your run.", 40.0d, 116.0d, 15.0d, -11373961, false, false);
        double min = Math.min(310.0d, this.s.height - 430.0d);
        int i = 0;
        while (i < 2) {
            int i2 = i * 212;
            panel(painter, i2 + 34, 144.0d, 200.0d, min, i == this.s.character ? -10123 : -2299420);
            double d = i2 + 134;
            double d2 = 144.0d + min;
            runner(painter, (i * 2) + (i == this.s.character ? this.s.skin : 0), d, d2 - 77.0d, 1.45d);
            painter.text(i == 0 ? "JOJO" : "MAYA", d, d2 - 42.0d, 25.0d, -15454917, true, true);
            painter.text(i == 0 ? "Local all-rounder" : "Futsal captain", d, d2 - 19.0d, 12.0d, -15454917, false, true);
            i++;
        }
        double d3 = 144.0d + min;
        painter.text("OUTFIT", 40.0d, d3 + 43.0d, 13.0d, -15454917, true, false);
        double d4 = d3 + 60.0d;
        button(painter, "EVERYDAY", 34.0d, d4, 200.0d, 57.0d, this.s.skin == 0);
        button(painter, "CARNIVAL", 246.0d, d4, 200.0d, 57.0d, this.s.skin == 1);
        painter.text("Same skills. Your style.", 240.0d, d3 + 150.0d, 14.0d, -11373961, false, true);
        button(painter, "PLAY AS " + who() + "  →", 36.0d, this.s.height - 132.0d, 408.0d, 76.0d, true);
    }

    private void tour(Painter painter) {
        painter.text("Five places. One continuous run.", 40.0d, 116.0d, 15.0d, -11373961, false, false);
        double min = Math.min(215.0d, this.s.height * 0.23d);
        double d = min * (this.tour != 3 ? 6 : 3);
        painter.clip(34.0d, 143.0d, 412.0d, min);
        painter.image(Routes.ASSETS[this.tour], 1, 2, this.s.night ? 1 : 0, 36.0d - (0.32d * d), 143.0d, d, min, false, 1.0d);
        painter.unclip();
        painter.text(Routes.NAMES[this.tour].toUpperCase(Locale.US), 40.0d, min + 181.0d, 25.0d, -15454917, true, false);
        painter.text(Routes.MOODS[this.tour], 40.0d, min + 208.0d, 14.0d, -11373961, false, false);
        double d2 = min + 230.0d;
        int i = 0;
        while (i < 5) {
            int i2 = i + 1;
            button(painter, Integer.toString(i2), (i * 84) + 36, d2, 72.0d, 51.0d, i == this.tour);
            i = i2;
        }
        painter.text(new String[]{"Read the moving carts. Find a gap.", "Thread between crossing carts.", "Choose your own path through the market.", "Watch the direction of crossing carts.", "Keep moving as the pace increases."}[this.tour], 240.0d, d2 + 93.0d, 14.0d, -15454917, false, true);
        painter.text("Reach this stop at " + (this.tour * 20) + " seconds.", 240.0d, d2 + 123.0d, 14.0d, -15454917, true, true);
        painter.text("Runs begin in Panjim. New stop every 20s.", 240.0d, this.s.height - 164.0d, 13.0d, -11373961, false, true);
        button(painter, "LET'S GO  →", 36.0d, this.s.height - 132.0d, 408.0d, 76.0d, true);
    }

    private void help(Painter painter) {
        String[] strArr = {"01   DRAG TO DODGE", "02   JUMP OR SLIDE", "03   KEEP YOUR RHYTHM", "04   CHASE YOUR BEST"};
        String[][] strArr2 = {new String[]{"Drag sideways to position your runner.", "Watch for carts crossing the street."}, new String[]{"Swipe up to jump. Down to slide.", "You can also tap to jump."}, new String[]{"Goa changes every 20 seconds.", "Speed and crossing traffic increase."}, new String[]{"Pass close without touching a cart.", "Chain close calls for up to 5×."}};
        double min = Math.min(128.0d, (this.s.height - 330.0d) / 4.0d);
        int i = 0;
        while (i < 4) {
            double d = (i * min) + 136.0d;
            int i2 = i;
            panel(painter, 34.0d, d, 412.0d, min - 12.0d, -1970970);
            painter.text(strArr[i2], 50.0d, d + 29.0d, 17.0d, -15454917, true, false);
            painter.text(strArr2[i2][0], 50.0d, d + 56.0d, 14.0d, -15454917, false, false);
            painter.text(strArr2[i2][1], 50.0d, d + 78.0d, 14.0d, -15454917, false, false);
            i = i2 + 1;
        }
        button(painter, "GOT IT. PLAY!  →", 36.0d, this.s.height - 132.0d, 408.0d, 76.0d, true);
    }

    private void settings(Painter painter) {
        painter.text("Make yourself comfortable.", 40.0d, 116.0d, 15.0d, -11373961, false, false);
        String[] strArr = {"TIME OF DAY", "SOUND EFFECTS", "HAPTICS"};
        String[] strArr2 = {this.s.night ? "NIGHT" : "DAY", this.s.sound ? "ON" : "OFF", this.s.haptics ? "ON" : "OFF"};
        int i = 0;
        while (i < 3) {
            double d = (i * 100) + 154;
            panel(painter, 34.0d, d, 412.0d, 80.0d, -1970970);
            painter.text(strArr[i], 50.0d, d + 46.0d, 16.0d, -15454917, true, false);
            button(painter, strArr2[i], 314.0d, d + 12.0d, 116.0d, 52.0d, i == 0 ? this.s.night : i == 1 ? this.s.sound : this.s.haptics);
            i++;
        }
        painter.text("Saved automatically on this phone.", 240.0d, 489.0d, 14.0d, -11373961, false, true);
        painter.text("BACHKE! GOA  ·  0.4 3D ALPHA", 240.0d, this.s.height - 162.0d, 12.0d, -11373961, true, true);
        button(painter, "BACK TO GOA", 36.0d, this.s.height - 132.0d, 408.0d, 76.0d, true);
    }

    public String touch(double d, double d2) {
        String str = "none";
        if (d < 0.0d || d > 480.0d || d2 < 0.0d) {
            return "none";
        }
        if (d2 > this.s.height) {
            return "none";
        }
        if (this.page == Page.HOME) {
            if (hit(d, d2, 276.0d, 28.0d, 91.0d, 51.0d)) {
                this.s.night = !this.s.night;
                return "settings";
            }
            if (hit(d, d2, 382.0d, 28.0d, 76.0d, 51.0d)) {
                this.page = Page.SETTINGS;
                return "navigate";
            }
            if (hit(d, d2, 28.0d, this.s.height - 424.0d, 52.0d, 56.0d) || hit(d, d2, 400.0d, this.s.height - 424.0d, 52.0d, 56.0d)) {
                this.s.character = 1 - this.s.character;
                return "settings";
            }
            if (hit(d, d2, 118.0d, this.s.height - 283.0d, 244.0d, 65.0d)) {
                this.page = Page.CREW;
                return "navigate";
            }
            if (hit(d, d2, 36.0d, this.s.height - 196.0d, 408.0d, 76.0d)) {
                return "start";
            }
            int i = 0;
            while (i < 3) {
                int i2 = i;
                if (hit(d, d2, (i * 148) + 22, this.s.height - 82.0d, 140.0d, 58.0d)) {
                    this.page = i2 == 0 ? Page.CREW : i2 == 1 ? Page.TOUR : Page.HELP;
                    return "navigate";
                }
                i = i2 + 1;
            }
            return "none";
        }
        if (hit(d, d2, 388.0d, 42.0d, 52.0d, 48.0d)) {
            this.page = Page.HOME;
            return "navigate";
        }
        if (hit(d, d2, 36.0d, this.s.height - 132.0d, 408.0d, 76.0d)) {
            boolean z = this.page == Page.SETTINGS;
            this.page = Page.HOME;
            return z ? "navigate" : "start";
        }
        if (this.page != Page.CREW) {
            if (this.page == Page.TOUR) {
                double min = Math.min(215.0d, this.s.height * 0.23d) + 230.0d;
                int i3 = 0;
                while (i3 < 5) {
                    double d3 = min;
                    double d4 = min;
                    int i4 = i3;
                    if (hit(d, d2, (i3 * 84) + 36, d3, 72.0d, 51.0d)) {
                        this.tour = i4;
                        return "navigate";
                    }
                    i3 = i4 + 1;
                    min = d4;
                }
                return "none";
            }
            if (this.page != Page.SETTINGS) {
                return "none";
            }
            for (int i5 = 0; i5 < 3; i5++) {
                if (hit(d, d2, 34.0d, (i5 * 100) + 154, 412.0d, 80.0d)) {
                    if (i5 == 0) {
                        this.s.night = !this.s.night;
                    } else {
                        Scene scene = this.s;
                        Scene scene2 = this.s;
                        if (i5 == 1) {
                            scene.sound = !scene2.sound;
                        } else {
                            scene.haptics = !scene2.haptics;
                        }
                    }
                    return "settings";
                }
            }
            return "none";
        }
        double min2 = Math.min(310.0d, this.s.height - 430.0d);
        int i6 = 0;
        while (i6 < 2) {
            double d5 = (i6 * 212) + 34;
            String str2 = str;
            int i7 = i6;
            if (hit(d, d2, d5, 144.0d, 200.0d, min2)) {
                this.s.character = i7;
                return "settings";
            }
            if (hit(d, d2, d5, 144.0d + min2 + 60.0d, 200.0d, 57.0d)) {
                this.s.skin = i7;
                return "settings";
            }
            i6 = i7 + 1;
            str = str2;
        }
        return str;
    }
}
