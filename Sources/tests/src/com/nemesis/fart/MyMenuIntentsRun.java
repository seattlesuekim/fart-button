package com.nemesis.fart;

/** Host checks for My Menu intent allow-list. */
public class MyMenuIntentsRun {
    private static int failed;

    public static void main(String[] args) {
        check(MyMenuIntents.isTrigger(MyMenuIntents.HKMC_MYMENU), "hkmc mymenu accepted");
        check(MyMenuIntents.isTrigger(MyMenuIntents.MOBIS_CUSTOM), "mobis custom accepted");
        check(!MyMenuIntents.isTrigger("mobis.intent.action.SHORTCUT_NAVI"), "navi rejected");
        check(!MyMenuIntents.isTrigger("mobis.intent.action.SHORTCUT_SETUP"), "setup rejected");
        check(!MyMenuIntents.isTrigger(null), "null rejected");
        check(!MyMenuIntents.isTrigger(""), "empty rejected");

        FartSession s = new FartSession(
                new StubClimatePort(),
                new SnapshotStore(new java.io.File(System.getProperty("java.io.tmpdir"), "fart-stub")),
                new FakeClock(0),
                new FartConfig());
        check(!s.onArmed(), "stub climate refuses arm");
        check(s.state() == FartSession.State.IDLE, "stays idle with stub");
        check(s.lastError().indexOf("not implemented") >= 0, "stub error mentions missing OEM");

        if (failed != 0) {
            System.err.println("FAIL: " + failed + " mymenu/stub checks");
            System.exit(1);
        }
        System.out.println("ALL MYMENU / STUB TESTS PASSED");
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
