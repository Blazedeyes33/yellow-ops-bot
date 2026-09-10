package com.bachke.goa;

/* loaded from: classes.dex */
public final class Routes {
    public static final double LEVEL_SECONDS = 20.0d;
    public static final double MAX_SPEED = 520.0d;
    public static final String[] NAMES = {"Panjim", "Margao", "Mapusa", "Old Goa", "Calangute"};
    public static final String[] ASSETS = {"panjim.png", "margao.png", "mapusa.png", "oldgoa.png", "calangute.png"};
    public static final String[] MOODS = {"Fontainhas & riverside", "The market square", "Friday market rush", "The heritage stretch", "The coastal sprint"};

    private Routes() {
    }

    public static int level(double d) {
        return ((int) Math.floor((Math.max(0.0d, d) / 20.0d) + 1.0E-9d)) + 1;
    }

    public static int index(double d) {
        return (level(d) - 1) % NAMES.length;
    }

    public static double progress(double d) {
        return Math.max(0.0d, Math.min(1.0d, (d - ((level(d) - 1) * 20.0d)) / 20.0d));
    }
}
