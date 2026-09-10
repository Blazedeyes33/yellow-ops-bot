package com.bachke.goa;

/**
 * Runner animation state machine. Consumes RunnerCore state each frame, owns clip selection,
 * playback rate (foot speed matched to world speed so planted feet do not skate) and a short
 * crossfade between clips. Pure Java: exercised by desktop tests.
 */
public final class CharacterAnimator {
    public enum State { IDLE, RUN, JUMP_START, JUMP_AIR, JUMP_LAND, SLIDE_START, SLIDE, SLIDE_EXIT, DODGE_LEFT, DODGE_RIGHT, HIT }

    /**
     * Forward velocity of the planted foot in the in-place Run_Anime loop, measured per character from the
     * source clip (tools/measure_foot_speed). Playback rate = world speed / this, so feet do not skate.
     */
    public static final float JOJO_RUN_CLIP_SPEED = 5.15f;
    public static final float MAYA_RUN_CLIP_SPEED = 3.97f;
    public final float runClipSpeed;
    public static final float CROSSFADE = 0.12f;

    public final BgmModel model;
    public final Pose pose, previous, current;
    public State state = State.IDLE;
    BgmModel.Clip clip;
    float clipTime, rate = 1f, fade = 1f;
    double lastActionTime = -10;
    boolean wasAirborne, wasSliding, wasOver;

    public CharacterAnimator(BgmModel model, float runClipSpeed) {
        this.model = model;
        this.runClipSpeed = runClipSpeed;
        pose = new Pose(model.boneCount); previous = new Pose(model.boneCount); current = new Pose(model.boneCount);
        play("Idle_A", 1f, State.IDLE, false);
        previous.set(current); pose.set(current);
    }

    void play(String name, float rate, State s, boolean crossfade) {
        BgmModel.Clip c = model.clipByName.get(name);
        if (c == null) c = model.clips[0];
        if (crossfade) { previous.set(pose); fade = 0f; } else fade = 1f;
        clip = c; clipTime = 0; this.rate = rate; state = s;
    }

    boolean finished() { return !clip.loop && clipTime >= clip.duration(); }

    /** Advance by dt seconds using the simulation state. Menu spin/idle when core is in MENU. */
    public void update(RunnerCore core, boolean menu, double dt) {
        boolean airborne = core.y > 0.02 || core.vy > 0.0;
        boolean sliding = core.sliding();
        boolean over = core.state == GameCore.State.OVER;
        if (menu) {
            if (state != State.IDLE) play("Idle_A", 1f, State.IDLE, true);
        } else if (over) {
            if (!wasOver) play("Hit_Knockback", 1.3f, State.HIT, true);
        } else {
            if (state == State.IDLE || state == State.HIT) play("Run_Anime", runRate(core), State.RUN, true);
            if (airborne && !wasAirborne) play("Jump_Start", 2.6f, State.JUMP_START, true);
            if (state == State.JUMP_START && (clipTime > 0.2f || !airborne)) play("Jump_air", 1f, State.JUMP_AIR, true);
            if (!airborne && wasAirborne && (state == State.JUMP_AIR || state == State.JUMP_START)) play("Jump_Land", 2.4f, State.JUMP_LAND, true);
            if (state == State.JUMP_LAND && clipTime > 0.28f) play("Run_Anime", runRate(core), State.RUN, true);
            if (sliding && !wasSliding && !airborne) play("Slide_Start", 3.0f, State.SLIDE_START, true);
            if (state == State.SLIDE_START && clipTime > 0.22f) play("Slide", 1f, State.SLIDE, true);
            if (!sliding && wasSliding && (state == State.SLIDE || state == State.SLIDE_START)) play("Slide_Exit", 2.2f, State.SLIDE_EXIT, true);
            if (state == State.SLIDE_EXIT && clipTime > 0.2f) play("Run_Anime", runRate(core), State.RUN, true);
            if (core.lastActionTime > lastActionTime && state == State.RUN && !airborne && !sliding) {
                if (core.lastAction == RunnerCore.Action.LEFT) play("Dodge_left", 2.4f, State.DODGE_LEFT, true);
                else if (core.lastAction == RunnerCore.Action.RIGHT) play("Dodge_right", 2.4f, State.DODGE_RIGHT, true);
            }
            if ((state == State.DODGE_LEFT || state == State.DODGE_RIGHT) && clipTime > 0.3f) play("Run_Anime", runRate(core), State.RUN, true);
            if (state == State.RUN) rate = runRate(core);
        }
        lastActionTime = core.lastActionTime;
        wasAirborne = airborne; wasSliding = sliding; wasOver = over;

        clipTime += (float) dt * rate;
        if (!clip.loop && clipTime > clip.duration()) clipTime = clip.duration();
        current.sample(clip, clipTime);
        if (fade < 1f) {
            fade = Math.min(1f, fade + (float) dt / CROSSFADE);
            pose.blend(previous, current, smooth(fade));
        } else pose.set(current);
    }

    float runRate(RunnerCore core) { return Math.max(0.8f, Math.min(2.0f, (float) core.speed() / runClipSpeed)); }

    static float smooth(float x) { return x * x * (3 - 2 * x); }
}
