package com.nemesis.fart;

/** Production clock using {@link System#currentTimeMillis()}. */
public final class SystemClock implements Clock {
    public long nowMs() {
        return System.currentTimeMillis();
    }
}
