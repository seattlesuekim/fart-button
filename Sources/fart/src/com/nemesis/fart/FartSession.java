package com.nemesis.fart;

import java.io.IOException;

/**
 * Snapshot → apply high fan + fresh air → restore after duration or cancel.
 * Android/OEM stay behind {@link ClimatePort}; time behind {@link Clock}.
 */
public final class FartSession {
    public enum State {
        IDLE,
        ACTIVE
    }

    /** Fan level used while fart mode is active. */
    public static final int HIGH_FAN = 8;

    private final ClimatePort climate;
    private final SnapshotStore store;
    private final Clock clock;
    private final FartConfig config;

    private State state = State.IDLE;
    private ClimateSnapshot prior;
    private long deadlineMs;
    private String lastError = "";

    public FartSession(ClimatePort climate, SnapshotStore store, Clock clock, FartConfig config) {
        this.climate = climate;
        this.store = store;
        this.clock = clock;
        this.config = config;
    }

    public State state() {
        return state;
    }

    public String lastError() {
        return lastError;
    }

    public long deadlineMs() {
        return deadlineMs;
    }

    /**
     * After process restart: restore climate if a fresh active snapshot exists.
     *
     * @return true if a restore ran
     */
    public boolean recoverIfNeeded() {
        SnapshotStore.Stored stored = store.load();
        if (stored == null) {
            return false;
        }
        long now = clock.nowMs();
        if (now - stored.startedAtMs > config.staleSnapshotMs) {
            store.clear();
            lastError = "stale snapshot cleared";
            return false;
        }
        try {
            climate.write(stored.climate);
            store.clear();
            state = State.IDLE;
            prior = null;
            deadlineMs = 0;
            lastError = "";
            return true;
        } catch (ClimateException e) {
            lastError = e.getMessage() != null ? e.getMessage() : "recover write failed";
            return false;
        }
    }

    /**
     * Double-press armed. From IDLE: capture and apply. From ACTIVE: cancel/restore.
     *
     * @return true if climate was changed (apply or restore)
     */
    public boolean onArmed() {
        if (state == State.ACTIVE) {
            return restore("cancel");
        }
        return arm();
    }

    /** Advance time; restore when deadline is reached. */
    public boolean onTick() {
        if (state != State.ACTIVE) {
            return false;
        }
        if (clock.nowMs() < deadlineMs) {
            return false;
        }
        return restore("deadline");
    }

    private boolean arm() {
        lastError = "";
        ClimateSnapshot snap;
        try {
            snap = climate.read();
        } catch (ClimateException e) {
            lastError = e.getMessage() != null ? e.getMessage() : "read failed";
            return false;
        }
        long now = clock.nowMs();
        try {
            store.save(snap, now);
        } catch (IOException e) {
            lastError = e.getMessage() != null ? e.getMessage() : "store failed";
            return false;
        }
        ClimateSnapshot desired = new ClimateSnapshot(HIGH_FAN, false);
        try {
            climate.write(desired);
        } catch (ClimateException e) {
            store.clear();
            lastError = e.getMessage() != null ? e.getMessage() : "write failed";
            return false;
        }
        prior = snap;
        deadlineMs = now + config.durationMs;
        state = State.ACTIVE;
        return true;
    }

    private boolean restore(String reason) {
        ClimateSnapshot target = prior;
        if (target == null) {
            SnapshotStore.Stored stored = store.load();
            if (stored != null) {
                target = stored.climate;
            }
        }
        if (target == null) {
            state = State.IDLE;
            deadlineMs = 0;
            store.clear();
            lastError = "restore without snapshot (" + reason + ")";
            return false;
        }
        try {
            climate.write(target);
        } catch (ClimateException e) {
            lastError = e.getMessage() != null ? e.getMessage() : "restore write failed";
            return false;
        }
        store.clear();
        prior = null;
        deadlineMs = 0;
        state = State.IDLE;
        lastError = "";
        return true;
    }
}
