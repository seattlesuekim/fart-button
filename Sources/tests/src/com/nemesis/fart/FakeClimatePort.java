package com.nemesis.fart;

/** In-memory climate for host tests. */
public final class FakeClimatePort implements ClimatePort {
    private ClimateSnapshot current;
    private boolean failRead;
    private boolean failWrite;
    private int writeCount;

    public FakeClimatePort(int fan, boolean recirculationOn) {
        this.current = new ClimateSnapshot(fan, recirculationOn);
    }

    public void setFailRead(boolean fail) {
        failRead = fail;
    }

    public void setFailWrite(boolean fail) {
        failWrite = fail;
    }

    public int writeCount() {
        return writeCount;
    }

    public ClimateSnapshot current() {
        return current;
    }

    public ClimateSnapshot read() throws ClimateException {
        if (failRead) {
            throw new ClimateException("fake read failed");
        }
        return current;
    }

    public void write(ClimateSnapshot desired) throws ClimateException {
        if (failWrite) {
            throw new ClimateException("fake write failed");
        }
        writeCount++;
        current = desired;
    }
}
