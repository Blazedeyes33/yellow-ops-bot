package com.bachke.goa;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/* loaded from: classes.dex */
public final class AndroidPainter implements Painter {
    Canvas canvas;
    private final Paint paint = new Paint(3);
    private final Rect source = new Rect();
    private final RectF target = new RectF();
    private final HashMap<String, Bitmap> images = new HashMap<>();
    private final Bitmap[] parts = new Bitmap[32];
    private final Typeface regular = Typeface.create("sans-serif", 0);
    private final Typeface bold = Typeface.create("sans-serif-condensed", 1);

    @Override // com.bachke.goa.Painter
    public void clip(double d, double d2, double d3, double d4) {
        this.canvas.save();
        this.canvas.clipRect((float) d, (float) d2, (float) (d + d3), (float) (d2 + d4));
    }

    @Override // com.bachke.goa.Painter
    public void unclip() {
        this.canvas.restore();
    }

    public AndroidPainter(AssetManager assetManager) throws IOException {
        InputStream open;
        for (String str : Routes.ASSETS) {
            open = assetManager.open(str);
            try {
                Bitmap decodeStream = BitmapFactory.decodeStream(open);
                if (decodeStream == null) {
                    throw new IOException("Unable to decode " + str);
                }
                this.images.put(str, decodeStream);
                if (open != null) {
                    open.close();
                }
            } finally {
            }
        }
        open = assetManager.open("rig-matte.png");
        try {
            Bitmap decodeStream2 = BitmapFactory.decodeStream(open);
            if (open != null) {
                open.close();
            }
            if (decodeStream2 == null) {
                throw new IOException("Unable to decode character rig");
            }
            int i = 0;
            for (int i2 = 32; i < i2; i2 = 32) {
                int i3 = i % 8;
                int i4 = i / 8;
                int width = (decodeStream2.getWidth() * i3) / 8;
                int height = (decodeStream2.getHeight() * i4) / 4;
                int width2 = (((i3 + 1) * decodeStream2.getWidth()) / 8) - width;
                int height2 = (((i4 + 1) * decodeStream2.getHeight()) / 4) - height;
                int i5 = width2 * height2;
                int[] iArr = new int[i5];
                decodeStream2.getPixels(iArr, 0, width2, width, height, width2, height2);
                for (int i6 = 0; i6 < i5; i6++) {
                    iArr[i6] = RigAssets.alphaPixel(iArr[i6]);
                }
                int[] largestBounds = RigAssets.largestBounds(iArr, width2, height2);
                Bitmap createBitmap = Bitmap.createBitmap(iArr, width2, height2, Bitmap.Config.ARGB_8888);
                this.parts[i] = Bitmap.createBitmap(createBitmap, largestBounds[0], largestBounds[1], largestBounds[2] - largestBounds[0], largestBounds[3] - largestBounds[1]);
                if (this.parts[i] != createBitmap) {
                    createBitmap.recycle();
                }
                i++;
            }
            decodeStream2.recycle();
        } finally {
        }
    }

    private void color(int i) {
        this.paint.setColor(i);
        this.paint.setStyle(Paint.Style.FILL);
    }

    @Override // com.bachke.goa.Painter
    public void rect(double d, double d2, double d3, double d4, int i, double d5) {
        color(i);
        this.target.set((float) d, (float) d2, (float) (d + d3), (float) (d2 + d4));
        float f = (float) d5;
        this.canvas.drawRoundRect(this.target, f, f, this.paint);
    }

    @Override // com.bachke.goa.Painter
    public void ellipse(double d, double d2, double d3, double d4, int i) {
        color(i);
        this.target.set((float) d, (float) d2, (float) (d + d3), (float) (d2 + d4));
        this.canvas.drawOval(this.target, this.paint);
    }

    @Override // com.bachke.goa.Painter
    public void line(double d, double d2, double d3, double d4, double d5, int i) {
        color(i);
        this.paint.setStrokeWidth((float) d5);
        this.paint.setStrokeCap(Paint.Cap.ROUND);
        this.canvas.drawLine((float) d, (float) d2, (float) d3, (float) d4, this.paint);
    }

    @Override // com.bachke.goa.Painter
    public void text(String str, double d, double d2, double d3, int i, boolean z, boolean z2) {
        color(i);
        this.paint.setTypeface(z ? this.bold : this.regular);
        this.paint.setTextSize((float) d3);
        this.paint.setTextAlign(z2 ? Paint.Align.CENTER : Paint.Align.LEFT);
        this.canvas.drawText(str, (float) d, (float) d2, this.paint);
    }

    @Override // com.bachke.goa.Painter
    public void image(String str, int i, int i2, int i3, double d, double d2, double d3, double d4, boolean z, double d5) {
        Bitmap bitmap = this.images.get(str);
        double d6 = d + d3;
        if (d6 < 0.0d || d > 480.0d) {
            return;
        }
        int i4 = i3 % i;
        int i5 = i3 / i;
        this.source.set((bitmap.getWidth() * i4) / i, (bitmap.getHeight() * i5) / i2, ((i4 + 1) * bitmap.getWidth()) / i, ((i5 + 1) * bitmap.getHeight()) / i2);
        this.target.set((float) d, (float) d2, (float) d6, (float) (d2 + d4));
        color(-1);
        this.paint.setAlpha((int) (255.0d * d5));
        if (z) {
            this.canvas.save();
            this.canvas.scale(-1.0f, 1.0f, (float) (d + (d3 / 2.0d)), (float) (d2 + (d4 / 2.0d)));
        }
        this.canvas.drawBitmap(bitmap, this.source, this.target, this.paint);
        if (z) {
            this.canvas.restore();
        }
        this.paint.setAlpha(255);
    }

    @Override // com.bachke.goa.Painter
    public void part(int i, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        color(-1);
        this.paint.setAlpha((int) (255.0d * d8));
        this.canvas.save();
        this.canvas.translate((float) d, (float) d2);
        this.canvas.rotate((float) d5);
        this.target.set((float) ((-d6) * d3), (float) ((-d7) * d4), (float) ((1.0d - d6) * d3), (float) ((1.0d - d7) * d4));
        this.canvas.drawBitmap(this.parts[i], (Rect) null, this.target, this.paint);
        this.canvas.restore();
        this.paint.setAlpha(255);
    }
}
