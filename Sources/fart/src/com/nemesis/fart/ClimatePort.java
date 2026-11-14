package com.nemesis.fart;

/** Read/write cabin fan and recirculation. OEM details stay behind this port. */
public interface ClimatePort {
    ClimateSnapshot read() throws ClimateException;

    void write(ClimateSnapshot desired) throws ClimateException;
}
