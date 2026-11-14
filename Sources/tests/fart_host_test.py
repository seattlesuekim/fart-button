#!/usr/bin/env python3
"""Compile and run fart-button host unit suites (no Android)."""
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FART_SRC = ROOT / "fart" / "src"
TEST_SRC = ROOT / "tests" / "src"

RUNNERS = (
    "com.nemesis.fart.PressCounterRun",
    "com.nemesis.fart.SnapshotStoreRun",
    "com.nemesis.fart.FartSessionRun",
)


def javac_sources() -> list[Path]:
    files = sorted(FART_SRC.rglob("*.java")) + sorted(TEST_SRC.rglob("*.java"))
    # Only fart package tests/fakes for this suite
    return [p for p in files if "com/nemesis/fart" in str(p)]


def main() -> None:
    sources = javac_sources()
    if not sources:
        print("FAIL: no fart sources", file=sys.stderr)
        sys.exit(1)
    out = Path(tempfile.mkdtemp(prefix="nemesis-fart-host-"))
    try:
        cmd = [
            "javac",
            "-source",
            "1.7",
            "-target",
            "1.7",
            "-Xlint:-options",
            "-d",
            str(out),
        ] + [str(p) for p in sources]
        subprocess.check_call(cmd)
        for runner in RUNNERS:
            print("==>", runner, flush=True)
            subprocess.check_call(["java", "-cp", str(out), runner])
    finally:
        shutil.rmtree(out, ignore_errors=True)
    print("ALL FART HOST TESTS PASSED", flush=True)


if __name__ == "__main__":
    main()
