package com.nemesis.fart;

/** Host-only clock for session tests. */
public final class FakeClock implements Clock {
    private long now;

    public FakeClock(long startMs) {
        this.now = startMs;
    }

    public void set(long ms) {
        now = ms;
    }

    public void advance(long deltaMs) {
        now += deltaMs;
    }

    public long nowMs() {
        return now;
    }
}
