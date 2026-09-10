package com.bachke.goa;

import com.bachke.goa.RunnerCore;

/* loaded from: classes.dex */
public final class SwipeInput {
    private boolean tracking;
    private boolean used;
    private double x;
    private double y;

    public void down(double d, double d2) {
        this.x = d;
        this.y = d2;
        this.tracking = true;
        this.used = false;
    }

    public void cancel() {
        this.tracking = false;
        this.used = false;
    }

    public RunnerCore.Action move(double d, double d2) {
        if (!this.tracking || this.used) {
            return null;
        }
        double d3 = d - this.x;
        double d4 = d2 - this.y;
        if (Math.max(Math.abs(d3), Math.abs(d4)) < 28.0d) {
            return null;
        }
        this.used = true;
        return Math.abs(d3) > Math.abs(d4) ? d3 < 0.0d ? RunnerCore.Action.LEFT : RunnerCore.Action.RIGHT : d4 < 0.0d ? RunnerCore.Action.JUMP : RunnerCore.Action.SLIDE;
    }

    public RunnerCore.Action up(double d, double d2) {
        RunnerCore.Action move = move(d, d2);
        if (move == null && this.tracking && !this.used) {
            move = RunnerCore.Action.JUMP;
        }
        this.tracking = false;
        return move;
    }
}
