package com.nemesis.fart;

/** Monotonic-ish time source for session deadlines (injectable in tests). */
public interface Clock {
    long nowMs();
}
