package com.nemesis.fart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Persists an in-progress session so a restart can restore climate.
 * Line format: {@code key=value} (UTF-8).
 */
public final class SnapshotStore {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final File file;

    public SnapshotStore(File stateDir) {
        this.file = new File(stateDir, "active.snapshot");
    }

    public File file() {
        return file;
    }

    public void save(ClimateSnapshot climate, long startedAtMs) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("cannot create " + parent);
        }
        File tmp = new File(file.getPath() + ".tmp");
        BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(tmp), UTF8));
        try {
            w.write("active=1\n");
            w.write("fan=" + climate.fanLevel + "\n");
            w.write("recirc=" + (climate.recirculationOn ? "1" : "0") + "\n");
            w.write("startedAtMs=" + startedAtMs + "\n");
        } finally {
            w.close();
        }
        if (!tmp.renameTo(file)) {
            // Windows/host fallback
            if (file.exists() && !file.delete()) {
                throw new IOException("cannot replace " + file);
            }
            if (!tmp.renameTo(file)) {
                throw new IOException("cannot rename " + tmp);
            }
        }
    }

    /** Null if missing or corrupt. */
    public Stored load() {
        if (!file.isFile()) {
            return null;
        }
        int fan = Integer.MIN_VALUE;
        int recirc = -1;
        long started = Long.MIN_VALUE;
        boolean active = false;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    return null;
                }
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if ("active".equals(key)) {
                    active = "1".equals(val);
                } else if ("fan".equals(key)) {
                    fan = Integer.parseInt(val);
                } else if ("recirc".equals(key)) {
                    recirc = Integer.parseInt(val);
                } else if ("startedAtMs".equals(key)) {
                    started = Long.parseLong(val);
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (!active || fan == Integer.MIN_VALUE || recirc < 0 || started == Long.MIN_VALUE) {
            return null;
        }
        return new Stored(new ClimateSnapshot(fan, recirc != 0), started);
    }

    public void clear() {
        if (file.exists()) {
            file.delete();
        }
    }

    public static final class Stored {
        public final ClimateSnapshot climate;
        public final long startedAtMs;

        public Stored(ClimateSnapshot climate, long startedAtMs) {
            this.climate = climate;
            this.startedAtMs = startedAtMs;
        }
    }
}
