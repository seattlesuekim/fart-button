package com.nemesis.fart;

/**
 * Counts faceplate key releases. Two inside {@link #GAP_MS} arm fart mode.
 * Debounce and mobis/hkmc family rules match Manager {@code SetupPress}.
 */
public final class PressCounter {
    public static final long GAP_MS = 1500;
    /** ModeManager and PhoneWindowManager can both announce one press. */
    public static final long DEBOUNCE_MS = 300;
    public static final int NEED = 2;

    private int count;
    private long lastMs;
    /** Once a mobis.* broadcast has been seen, com.hkmc.* ones are echoes. */
    private boolean sawMobis;

    public void restore(int count, long lastMs) {
        this.count = count;
        this.lastMs = lastMs;
    }

    public void restoreFamily(boolean sawMobis) {
        this.sawMobis = sawMobis;
    }

    public int count() {
        return count;
    }

    public long lastMs() {
        return lastMs;
    }

    public boolean sawMobis() {
        return sawMobis;
    }

    /**
     * One action family per press: mobis.* when the unit sends it,
     * com.hkmc.* only on units that never do.
     */
    public boolean note(long now, String action) {
        boolean mobis = action != null && action.startsWith("mobis.");
        if (!mobis && sawMobis) {
            return false;
        }
        if (mobis) {
            sawMobis = true;
        }
        return note(now);
    }

    public boolean note(long now) {
        if (lastMs != 0 && now >= lastMs && now - lastMs < DEBOUNCE_MS) {
            return false;
        }
        if (now - lastMs > GAP_MS) {
            count = 0;
        }
        lastMs = now;
        count++;
        if (count >= NEED) {
            count = 0;
            return true;
        }
        return false;
    }
}
