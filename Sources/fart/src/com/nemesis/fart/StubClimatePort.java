package com.nemesis.fart;

/**
 * Placeholder until the on-car HVAC spike produces a real OEM adapter.
 * Reads/writes fail loudly so we never pretend climate changed.
 */
public final class StubClimatePort implements ClimatePort {
    public static final String MESSAGE =
            "climate OEM adapter not implemented; complete HVAC spike first";

    public ClimateSnapshot read() throws ClimateException {
        throw new ClimateException(MESSAGE);
    }

    public void write(ClimateSnapshot desired) throws ClimateException {
        throw new ClimateException(MESSAGE);
    }
}
