package com.bachke.goa;

import com.bachke.goa.GameCore;
import java.util.Locale;

/* loaded from: classes.dex */
public final class Hud3D {
    private static final int GOLD = -13226;
    private static final int INK = -15454917;
    private static final int WHITE = -2594;

    public static void draw(Painter painter, RunnerCore runnerCore, Scene scene) {
        painter.rect(20.0d, 32.0d, 245.0d, 97.0d, -283890373, 20.0d);
        painter.text("SCORE", 38.0d, 57.0d, 11.0d, -4466211, true, false);
        painter.text(Integer.toString(runnerCore.score()), 38.0d, 91.0d, 32.0d, -2594, true, false);
        painter.text("×" + runnerCore.combo, 222.0d, 89.0d, 24.0d, -13226, true, true);
        painter.text("L" + runnerCore.level() + " · " + Routes.NAMES[runnerCore.route()] + " · " + runnerCore.closeCalls + " close calls", 38.0d, 114.0d, 11.0d, -4466211, false, false);
        painter.rect(396.0d, 32.0d, 62.0d, 59.0d, -15454917, 17.0d);
        painter.text("Ⅱ", 427.0d, 70.0d, 21.0d, -2594, true, true);
        painter.rect(22.0d, 143.0d, 436.0d, 5.0d, -2144977584, 3.0d);
        painter.rect(22.0d, 143.0d, Routes.progress(runnerCore.time) * 436.0d, 5.0d, -13226, 3.0d);
        if (runnerCore.time % 20.0d < 2.1d && runnerCore.level() > 1) {
            badge(painter, "LEVEL " + runnerCore.level() + " · " + Routes.NAMES[runnerCore.route()].toUpperCase(Locale.US), 208.0d);
        }
        if (runnerCore.time - runnerCore.closeAt < 1.0d) {
            badge(painter, "BACHKE! CLOSE CALL ×" + runnerCore.combo, 260.0d);
        }
        if (runnerCore.time < 5.0d) {
            badge(painter, "DRAG SIDEWAYS TO THREAD THE GAP", scene.height - 85.0d);
        } else if (runnerCore.time < 10.0d) {
            badge(painter, "UP: JUMP   ·   DOWN: SLIDE", scene.height - 85.0d);
        }
        if (runnerCore.state == GameCore.State.PAUSED || runnerCore.state == GameCore.State.OVER) {
            boolean z = runnerCore.state == GameCore.State.OVER;
            double d = scene.height / 2.0d;
            painter.rect(0.0d, 0.0d, 480.0d, scene.height, -2011943621, 0.0d);
            painter.rect(24.0d, d - 180.0d, 432.0d, 380.0d, -2594, 26.0d);
            painter.text(z ? "ARRE! BACHKE." : "TAKE A BREATHER", 240.0d, d - 121.0d, 27.0d, -15454917, true, true);
            painter.text(z ? "Ready for another run?" : "Your run is paused.", 240.0d, d - 89.0d, 15.0d, -15454917, false, true);
            painter.text(z ? Integer.toString(runnerCore.score()) : "LEVEL " + runnerCore.level(), 240.0d, d - 31.0d, 37.0d, -15454917, true, true);
            painter.text("BEST " + Math.max(scene.best, runnerCore.score()) + "   ·   " + runnerCore.closeCalls + " CLOSE CALLS", 240.0d, d + 1.0d, 13.0d, -15454917, true, true);
            painter.rect(44.0d, d + 25.0d, 392.0d, 68.0d, -13226, 18.0d);
            painter.text(z ? "RUN AGAIN  →" : "RESUME  →", 240.0d, d + 67.0d, 22.0d, -15454917, true, true);
            painter.rect(44.0d, d + 111.0d, 392.0d, 56.0d, -15454917, 17.0d);
            painter.text("BACK TO HOME", 240.0d, d + 145.0d, 16.0d, -2594, true, true);
        }
    }

    private static void badge(Painter painter, String str, double d) {
        painter.rect(30.0d, d - 27.0d, 420.0d, 41.0d, -401330885, 18.0d);
        painter.text(str, 240.0d, d, 14.0d, -13226, true, true);
    }
}
