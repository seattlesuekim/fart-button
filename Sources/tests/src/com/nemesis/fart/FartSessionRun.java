package com.nemesis.fart;

import java.io.File;

/** Host driver for FartSession with fake clock and climate. */
public class FartSessionRun {
    private static int failed;

    public static void main(String[] args) throws Exception {
        File dir = File.createTempFile("fart-sess-", ".dir");
        if (!dir.delete() || !dir.mkdir()) {
            System.err.println("FAIL: temp dir");
            System.exit(1);
        }
        try {
            armAppliesHighFanAndFreshAir(dir);
            tickBeforeDeadlineDoesNothing(dir);
            tickAtDeadlineRestoresSnapshot(dir);
            secondArmWhileActiveRestoresEarly(dir);
            failedReadLeavesClimateUntouched(dir);
            restartWithActiveSnapshotRestores(dir);
            staleSnapshotIsCleared(dir);
        } finally {
            deleteTree(dir);
        }
        if (failed != 0) {
            System.err.println("FAIL: " + failed + " fart-session checks");
            System.exit(1);
        }
        System.out.println("ALL FART SESSION TESTS PASSED");
    }

    private static FartSession session(FakeClimatePort port, FakeClock clock, File dir) {
        return new FartSession(port, new SnapshotStore(dir), clock, new FartConfig());
    }

    private static void armAppliesHighFanAndFreshAir(File dir) {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(2, true);
        FakeClock clock = new FakeClock(1000);
        FartSession s = session(port, clock, dir);
        check(s.onArmed(), "arm succeeds");
        check(s.state() == FartSession.State.ACTIVE, "state active");
        check(port.current().fanLevel == FartSession.HIGH_FAN, "fan high");
        check(!port.current().recirculationOn, "recirc off");
        check(s.deadlineMs() == 1000 + FartConfig.DEFAULT_DURATION_MS, "deadline 8s out");
    }

    private static void tickBeforeDeadlineDoesNothing(File dir) {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(2, true);
        FakeClock clock = new FakeClock(0);
        FartSession s = session(port, clock, dir);
        s.onArmed();
        ClimateSnapshot during = port.current();
        clock.advance(FartConfig.DEFAULT_DURATION_MS - 1);
        check(!s.onTick(), "tick before deadline does nothing");
        check(s.state() == FartSession.State.ACTIVE, "still active");
        check(during.sameClimate(port.current()), "climate unchanged before deadline");
    }

    private static void tickAtDeadlineRestoresSnapshot(File dir) {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(4, true);
        FakeClock clock = new FakeClock(0);
        FartSession s = session(port, clock, dir);
        s.onArmed();
        clock.set(FartConfig.DEFAULT_DURATION_MS);
        check(s.onTick(), "tick at deadline restores");
        check(s.state() == FartSession.State.IDLE, "idle after deadline");
        check(port.current().fanLevel == 4, "fan restored");
        check(port.current().recirculationOn, "recirc restored");
    }

    private static void secondArmWhileActiveRestoresEarly(File dir) {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(1, false);
        FakeClock clock = new FakeClock(50);
        FartSession s = session(port, clock, dir);
        check(s.onArmed(), "arm");
        clock.advance(1000);
        check(s.onArmed(), "second arm cancels");
        check(s.state() == FartSession.State.IDLE, "idle after cancel");
        check(port.current().fanLevel == 1, "fan restored on cancel");
        check(!port.current().recirculationOn, "recirc restored on cancel");
    }

    private static void failedReadLeavesClimateUntouched(File dir) {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(5, true);
        port.setFailRead(true);
        FakeClock clock = new FakeClock(0);
        FartSession s = session(port, clock, dir);
        int writes = port.writeCount();
        check(!s.onArmed(), "arm fails when read fails");
        check(s.state() == FartSession.State.IDLE, "stays idle");
        check(port.writeCount() == writes, "no write after failed read");
        check(port.current().fanLevel == 5, "climate untouched");
    }

    private static void restartWithActiveSnapshotRestores(File dir) throws Exception {
        clean(dir);
        FakeClimatePort port = new FakeClimatePort(3, true);
        FakeClock clock = new FakeClock(10000);
        FartSession first = session(port, clock, dir);
        first.onArmed();
        check(port.current().fanLevel == FartSession.HIGH_FAN, "applied before crash");

        FakeClimatePort port2 = new FakeClimatePort(FartSession.HIGH_FAN, false);
        FakeClock clock2 = new FakeClock(10000 + 1000);
        FartSession restarted = session(port2, clock2, dir);
        check(restarted.recoverIfNeeded(), "recover restores");
        check(port2.current().fanLevel == 3, "fan from snapshot");
        check(port2.current().recirculationOn, "recirc from snapshot");
        check(restarted.state() == FartSession.State.IDLE, "idle after recover");
    }

    private static void staleSnapshotIsCleared(File dir) throws Exception {
        clean(dir);
        SnapshotStore store = new SnapshotStore(dir);
        store.save(new ClimateSnapshot(2, true), 0);
        FakeClimatePort port = new FakeClimatePort(9, false);
        FakeClock clock = new FakeClock(FartConfig.STALE_SNAPSHOT_MS + 1);
        FartSession s = session(port, clock, dir);
        check(!s.recoverIfNeeded(), "stale does not restore");
        check(store.load() == null, "stale file cleared");
        check(port.current().fanLevel == 9, "climate unchanged on stale");
    }

    private static void clean(File dir) {
        File[] kids = dir.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                deleteTree(kids[i]);
            }
        }
    }

    private static void deleteTree(File f) {
        File[] kids = f.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                deleteTree(kids[i]);
            }
        }
        f.delete();
    }

    private static void check(boolean cond, String msg) {
        if (cond) {
            System.out.println("OK: " + msg);
            return;
        }
        failed++;
        System.err.println("FAIL: " + msg);
    }
}
