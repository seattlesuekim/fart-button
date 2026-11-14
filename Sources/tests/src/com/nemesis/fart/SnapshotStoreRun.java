package com.nemesis.fart;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

/** Host driver for SnapshotStore. */
public class SnapshotStoreRun {
    private static int failed;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        File dir = File.createTempFile("fart-snap-", ".dir");
        if (!dir.delete() || !dir.mkdir()) {
            System.err.println("FAIL: temp dir");
            System.exit(1);
        }
        try {
            roundTrip(dir);
            missingIsNull(dir);
            truncatedIsNull(dir);
            clearRemoves(dir);
        } finally {
            deleteTree(dir);
        }
        if (failed != 0) {
            System.err.println("FAIL: " + failed + " snapshot-store checks");
            System.exit(1);
        }
        System.out.println("ALL SNAPSHOT STORE TESTS PASSED");
    }

    private static void roundTrip(File dir) throws Exception {
        SnapshotStore store = new SnapshotStore(dir);
        ClimateSnapshot snap = new ClimateSnapshot(3, true);
        store.save(snap, 12345L);
        SnapshotStore.Stored loaded = store.load();
        check(loaded != null, "load after save");
        check(loaded.climate.fanLevel == 3, "fan round-trip");
        check(loaded.climate.recirculationOn, "recirc round-trip");
        check(loaded.startedAtMs == 12345L, "startedAt round-trip");
    }

    private static void missingIsNull(File dir) {
        File other = new File(dir, "other");
        other.mkdir();
        SnapshotStore store = new SnapshotStore(other);
        check(store.load() == null, "missing file is null");
    }

    private static void truncatedIsNull(File dir) throws Exception {
        SnapshotStore store = new SnapshotStore(dir);
        FileOutputStream out = new FileOutputStream(store.file());
        try {
            out.write("active=1\nfan=2\n".getBytes(UTF8));
        } finally {
            out.close();
        }
        check(store.load() == null, "truncated file is null");
    }

    private static void clearRemoves(File dir) throws Exception {
        SnapshotStore store = new SnapshotStore(dir);
        store.save(new ClimateSnapshot(1, false), 1L);
        check(store.file().isFile(), "file exists before clear");
        store.clear();
        check(!store.file().exists(), "clear deletes file");
        check(store.load() == null, "load after clear is null");
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
