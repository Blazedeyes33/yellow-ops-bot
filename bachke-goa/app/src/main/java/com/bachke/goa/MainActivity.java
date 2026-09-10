package com.bachke.goa;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.bachke.goa.GameCore;
import com.bachke.goa.HomeScreen;
import com.bachke.goa.MainActivity;
import com.bachke.goa.RunnerCore;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public final class MainActivity extends Activity {
    private GameView game;
    private GLSurfaceView gl;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        try {
            this.gl = new GLSurfaceView(this);
            this.gl.setEGLContextClientVersion(3);
            this.gl.setEGLConfigChooser(8, 8, 8, 0, 24, 0);
            this.gl.setPreserveEGLContextOnPause(true);
            this.game = new GameView(this);
            this.gl.setRenderer(new GlRenderer3D(this.game.core, this.game.scene, getAssets()));
            this.gl.setRenderMode(0);
            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.addView(this.gl);
            frameLayout.addView(this.game);
            setContentView(frameLayout);
        } catch (IOException e) {
            TextView textView = new TextView(this);
            textView.setText("Bachke! Goa could not load its game files. Please reinstall this APK.");
            setContentView(textView);
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.gl != null) {
            this.gl.onResume();
        }
        if (this.game != null) {
            this.game.activate();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        if (this.game != null) {
            this.game.suspend();
        }
        if (this.gl != null) {
            this.gl.onPause();
        }
        super.onPause();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        if (this.game != null) {
            this.game.pool.release();
        }
        super.onDestroy();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.game == null) {
            super.onBackPressed();
            return;
        }
        synchronized (this.game.core) {
            this.game.swipe.cancel();
            if (this.game.core.state == GameCore.State.PLAYING) {
                this.game.core.pause();
            } else if (this.game.core.state != GameCore.State.MENU) {
                this.game.core.menu();
            } else if (!this.game.scene.home.back()) {
                super.onBackPressed();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    final class GameView extends View implements Choreographer.FrameCallback {
        boolean active;
        final AudioManager audio;
        int bottom;
        final RunnerCore core;
        double dragX;
        boolean dragged;
        final AudioFocusRequest focus;
        boolean gesture;
        boolean hasFocus;
        long lastFrame;
        double offsetX;
        double offsetY;
        final AndroidPainter painter;
        final SoundPool pool;
        final SharedPreferences prefs;
        double scale;
        final Scene scene;
        final EnumMap<GameCore.Event, Integer> sounds;
        final SwipeInput swipe;
        int top;
        boolean wasOver;

        GameView(Context context) throws IOException {
            super(context);
            this.core = new RunnerCore();
            this.scene = new Scene(new GameCore());
            this.swipe = new SwipeInput();
            this.sounds = new EnumMap<>(GameCore.Event.class);
            this.scale = 1.0d;
            setFocusable(true);
            setContentDescription("Bachke Goa. Swipe left or right, up to jump, down to slide.");
            this.painter = new AndroidPainter(context.getAssets());
            this.prefs = context.getSharedPreferences("bachke-alpha", 0);
            this.scene.home.modelBackdrop = true;
            this.scene.character = Math.max(0, Math.min(1, this.prefs.getInt("character", 0)));
            this.scene.skin = Math.max(0, Math.min(1, this.prefs.getInt("skin", 0)));
            this.scene.best = Math.max(0, this.prefs.getInt("bestStreet", 0));
            this.scene.night = this.prefs.getBoolean("night", false);
            this.scene.sound = this.prefs.getBoolean("sound", true);
            this.scene.haptics = this.prefs.getBoolean("haptics", true);
            AudioAttributes build = new AudioAttributes.Builder().setUsage(14).setContentType(4).build();
            this.pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(build).build();
            for (GameCore.Event event : GameCore.Event.values()) {
                AssetFileDescriptor openFd = context.getAssets().openFd(event.name().toLowerCase(Locale.US) + ".wav");
                try {
                    this.sounds.put(event, Integer.valueOf(this.pool.load(openFd, 1)));
                    if (openFd != null) {
                        openFd.close();
                    }
                } catch (Throwable th) {
                    if (openFd != null) {
                        try {
                            openFd.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            }
            this.audio = (AudioManager) context.getSystemService("audio");
            this.focus = new AudioFocusRequest.Builder(3).setAudioAttributes(build).setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() { // from class: com.bachke.goa.MainActivity$GameView$$ExternalSyntheticLambda0
                @Override // android.media.AudioManager.OnAudioFocusChangeListener
                public final void onAudioFocusChange(int i) {
                    MainActivity.GameView.this.m0lambda$new$0$combachkegoaMainActivity$GameView(i);
                }
            }).build();
            setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.bachke.goa.MainActivity$GameView$$ExternalSyntheticLambda1
                @Override // android.view.View.OnApplyWindowInsetsListener
                public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    return MainActivity.GameView.this.m1lambda$new$1$combachkegoaMainActivity$GameView(view, windowInsets);
                }
            });
        }

        /* renamed from: lambda$new$0$com-bachke-goa-MainActivity$GameView, reason: not valid java name */
        /* synthetic */ void m0lambda$new$0$combachkegoaMainActivity$GameView(int i) {
            synchronized (this.core) {
                this.hasFocus = i == 1;
                if (i < 0) {
                    this.core.pause();
                    this.swipe.cancel();
                    this.pool.autoPause();
                }
            }
        }

        /* renamed from: lambda$new$1$com-bachke-goa-MainActivity$GameView, reason: not valid java name */
        /* synthetic */ WindowInsets m1lambda$new$1$combachkegoaMainActivity$GameView(View view, WindowInsets windowInsets) {
            this.top = windowInsets.getSystemWindowInsetTop();
            this.bottom = windowInsets.getSystemWindowInsetBottom();
            if (Build.VERSION.SDK_INT >= 28 && windowInsets.getDisplayCutout() != null) {
                this.top = Math.max(this.top, windowInsets.getDisplayCutout().getSafeInsetTop());
            }
            return windowInsets;
        }

        void activate() {
            if (this.active) {
                return;
            }
            this.active = true;
            this.lastFrame = 0L;
            Choreographer.getInstance().postFrameCallback(this);
        }

        void suspend() {
            this.active = false;
            this.lastFrame = 0L;
            synchronized (this.core) {
                this.core.pause();
                this.swipe.cancel();
                this.pool.autoPause();
                this.audio.abandonAudioFocusRequest(this.focus);
                this.hasFocus = false;
                save();
            }
            Choreographer.getInstance().removeFrameCallback(this);
        }

        void save() {
            this.prefs.edit().putInt("character", this.scene.character).putInt("skin", this.scene.skin).putInt("bestStreet", this.scene.best).putBoolean("night", this.scene.night).putBoolean("sound", this.scene.sound).putBoolean("haptics", this.scene.haptics).apply();
        }

        void soundFocus() {
            if (this.scene.sound) {
                this.hasFocus = this.audio.requestAudioFocus(this.focus) == 1;
            }
        }

        void start() {
            this.core.streetMode = true;
            this.core.start(System.nanoTime());
            this.scene.home.page = HomeScreen.Page.HOME;
            this.scene.toastUntil = 0.0d;
            this.wasOver = false;
            this.lastFrame = 0L;
            this.swipe.cancel();
            soundFocus();
        }

        @Override // android.view.Choreographer.FrameCallback
        public void doFrame(long j) {
            if (this.active) {
                synchronized (this.core) {
                    double d = this.lastFrame == 0 ? 0.0d : (j - this.lastFrame) / 1.0E9d;
                    this.lastFrame = j;
                    this.core.advance(d);
                    this.scene.game.state = this.core.state;
                    if (this.core.state == GameCore.State.PLAYING || this.core.state == GameCore.State.MENU) {
                        this.scene.ambience += Math.min(d, 0.05d);
                    }
                    Iterator<GameCore.Event> it = this.core.events.iterator();
                    while (true) {
                        int i = 0;
                        if (!it.hasNext()) {
                            break;
                        }
                        GameCore.Event next = it.next();
                        if (this.scene.sound && this.hasFocus) {
                            this.pool.play(this.sounds.get(next).intValue(), 0.35f, 0.35f, 1, 0, 1.0f);
                        }
                        if (this.scene.haptics && (next == GameCore.Event.JUMP || next == GameCore.Event.HIT)) {
                            if (next != GameCore.Event.HIT) {
                                i = 3;
                            }
                            performHapticFeedback(i);
                        }
                    }
                    this.core.events.clear();
                    if (this.core.state != GameCore.State.PLAYING && this.hasFocus) {
                        this.audio.abandonAudioFocusRequest(this.focus);
                        this.hasFocus = false;
                    }
                    if (this.core.state == GameCore.State.OVER && !this.wasOver) {
                        this.scene.best = Math.max(this.scene.best, this.core.score());
                        save();
                    }
                    this.wasOver = this.core.state == GameCore.State.OVER;
                }
                invalidate();
                MainActivity.this.gl.requestRender();
                Choreographer.getInstance().postFrameCallback(this);
            }
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            double max = Math.max(1, (getHeight() - this.top) - this.bottom);
            this.scale = Math.min(getWidth() / 480.0d, max / 800.0d);
            this.scene.height = max / this.scale;
            this.offsetX = (getWidth() - (this.scale * 480.0d)) / 2.0d;
            this.offsetY = this.top;
            canvas.save();
            canvas.translate((float) this.offsetX, (float) this.offsetY);
            canvas.scale((float) this.scale, (float) this.scale);
            this.painter.canvas = canvas;
            synchronized (this.core) {
                if (this.core.state == GameCore.State.MENU) {
                    this.scene.home.draw(this.painter);
                } else {
                    Hud3D.draw(this.painter, this.core, this.scene);
                }
            }
            canvas.restore();
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            synchronized (this.core) {
                int actionMasked = motionEvent.getActionMasked();
                double x = (motionEvent.getX() - this.offsetX) / this.scale;
                double y = (motionEvent.getY() - this.offsetY) / this.scale;
                if (actionMasked != 3 && actionMasked != 5) {
                    if (actionMasked == 0) {
                        this.gesture = false;
                        if (this.core.state == GameCore.State.MENU) {
                            String str = this.scene.home.touch(x, y);
                            if ("start".equals(str)) {
                                start();
                            } else if ("settings".equals(str)) {
                                save();
                            }
                        } else if (this.core.state != GameCore.State.PLAYING) {
                            double d = this.scene.height / 2.0d;
                            if (x < 44.0d || x > 436.0d || y < 25.0d + d || y > 93.0d + d) {
                                if (x >= 44.0d && x <= 436.0d && y >= 111.0d + d && y <= d + 167.0d) {
                                    this.core.menu();
                                    this.scene.home.page = HomeScreen.Page.HOME;
                                }
                            } else if (this.core.state == GameCore.State.OVER) {
                                start();
                            } else {
                                this.core.resume();
                                this.lastFrame = 0L;
                                soundFocus();
                            }
                        } else if (x < 386.0d || y < 25.0d || y > 105.0d) {
                            this.swipe.down(x, y);
                            this.dragX = x;
                            this.dragged = false;
                            this.gesture = true;
                        } else {
                            this.core.pause();
                            this.swipe.cancel();
                        }
                        performClick();
                        return true;
                    }
                    if (this.gesture && (actionMasked == 2 || actionMasked == 1)) {
                        if (this.core.streetMode && actionMasked == 2 && Math.abs(x - this.dragX) > 2.0d) {
                            this.core.steer((x - this.dragX) * 0.025d);
                            this.dragX = x;
                            this.dragged = true;
                        }
                        RunnerCore.Action up = actionMasked == 1 ? this.swipe.up(x, y) : this.swipe.move(x, y);
                        if (up != null && ((!this.core.streetMode || (up != RunnerCore.Action.LEFT && up != RunnerCore.Action.RIGHT)) && (!this.dragged || actionMasked != 1))) {
                            this.core.act(up);
                        }
                        if (actionMasked == 1) {
                            this.gesture = false;
                        }
                    }
                    return true;
                }
                this.swipe.cancel();
                this.gesture = false;
                return true;
            }
        }

        @Override // android.view.View
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }
}
