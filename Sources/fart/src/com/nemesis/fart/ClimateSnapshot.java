package com.nemesis.fart;

/** Fan level and recirculation only (v1). */
public final class ClimateSnapshot {
    public final int fanLevel;
    /** True when recirculation is on (cabin air). Fart mode forces this off. */
    public final boolean recirculationOn;

    public ClimateSnapshot(int fanLevel, boolean recirculationOn) {
        this.fanLevel = fanLevel;
        this.recirculationOn = recirculationOn;
    }

    public boolean sameClimate(ClimateSnapshot other) {
        return other != null
                && fanLevel == other.fanLevel
                && recirculationOn == other.recirculationOn;
    }
}
