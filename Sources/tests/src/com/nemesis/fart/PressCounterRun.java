package com.nemesis.fart;

/** Host driver for PressCounter. Not packaged onto the head unit. */
public class PressCounterRun {
    private static int failed;

    public static void main(String[] args) {
        twoQuickPressesFire();
        singlePressDoesNotFire();
        gapResets();
        boundaryStaysInTheSameRun();
        debounceIgnoresEcho();
        secondDoubleFiresAgain();
        countSurvivesANewProcess();
        lateEchoOfOtherFamilyIgnored();
        hkmcOnlyUnitStillCounts();
        if (failed != 0) {
            System.err.println("FAIL: " + failed + " press-counter checks");
            System.exit(1);
        }
        System.out.println("ALL PRESS COUNTER TESTS PASSED");
    }

    private static void twoQuickPressesFire() {
        PressCounter p = new PressCounter();
        check(!p.note(10000), "first press does not fire");
        check(p.note(10400), "second press inside the gap fires");
    }

    private static void singlePressDoesNotFire() {
        PressCounter p = new PressCounter();
        check(!p.note(10000), "single press does not fire");
        check(p.count() == 1, "count is one after a single press");
    }

    private static void gapResets() {
        PressCounter p = new PressCounter();
        check(!p.note(5000), "first");
        check(!p.note(5000 + PressCounter.GAP_MS + 1), "gap longer than the limit resets");
        check(p.count() == 1, "new run has one press");
        check(p.note(5000 + PressCounter.GAP_MS + 400), "second of the new run fires");
    }

    private static void boundaryStaysInTheSameRun() {
        PressCounter p = new PressCounter();
        p.note(1000);
        check(p.note(1000 + PressCounter.GAP_MS), "exactly one gap stays in the same run and fires");
    }

    private static void debounceIgnoresEcho() {
        PressCounter p = new PressCounter();
        check(!p.note(10000), "press");
        check(!p.note(10000 + PressCounter.DEBOUNCE_MS - 1), "echo inside the debounce");
        check(p.count() == 1, "debounce keeps one");
        check(p.note(10400), "second real press fires");
    }

    private static void secondDoubleFiresAgain() {
        PressCounter p = new PressCounter();
        check(!p.note(0), "one");
        check(p.note(400), "first double fires");
        check(!p.note(800), "one of second run");
        check(p.note(1200), "second double fires");
    }

    private static void countSurvivesANewProcess() {
        PressCounter first = new PressCounter();
        check(!first.note(10000), "first process, first press");
        PressCounter restarted = new PressCounter();
        restarted.restore(first.count(), first.lastMs());
        check(restarted.note(10400), "second press after the process died fires");
    }

    private static final String M = "mobis.intent.action.SHORTCUT_MODE";
    private static final String H = "com.hkmc.intent.action.MYMENU_SHORTCUT";

    private static void lateEchoOfOtherFamilyIgnored() {
        PressCounter p = new PressCounter();
        check(!p.note(10000, M), "mobis press");
        check(!p.note(10600, H), "hkmc echo 600 ms later is ignored");
        check(p.count() == 1, "one press after a late echo");
        check(p.note(10700, M), "second mobis press fires");
    }

    private static void hkmcOnlyUnitStillCounts() {
        PressCounter p = new PressCounter();
        check(!p.note(0, H), "hkmc one");
        check(p.note(500, H), "hkmc two fires when no mobis was seen");
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
