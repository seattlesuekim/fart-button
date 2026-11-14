package com.nemesis.fart;

/** Tunables. Duration is fixed for v1; named for a later config pass. */
public final class FartConfig {
    public static final long DEFAULT_DURATION_MS = 8000;
    /** After a crash, restore if the active snapshot is younger than this. */
    public static final long STALE_SNAPSHOT_MS = 30000;

    public final long durationMs;
    public final long staleSnapshotMs;

    public FartConfig() {
        this(DEFAULT_DURATION_MS, STALE_SNAPSHOT_MS);
    }

    public FartConfig(long durationMs, long staleSnapshotMs) {
        this.durationMs = durationMs;
        this.staleSnapshotMs = staleSnapshotMs;
    }
}
