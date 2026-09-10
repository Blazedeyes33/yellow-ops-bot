package com.bachke.goa;

/* loaded from: classes.dex */
public interface Painter {
    void clip(double d, double d2, double d3, double d4);

    void ellipse(double d, double d2, double d3, double d4, int i);

    void image(String str, int i, int i2, int i3, double d, double d2, double d3, double d4, boolean z, double d5);

    void line(double d, double d2, double d3, double d4, double d5, int i);

    void part(int i, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8);

    void rect(double d, double d2, double d3, double d4, int i, double d5);

    void text(String str, double d, double d2, double d3, int i, boolean z, boolean z2);

    void unclip();
}
