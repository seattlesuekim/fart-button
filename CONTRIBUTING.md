# Contributing / development setup

This repository is **fart-button** (Hyundai Ioniq 5), based on [xwtk/hmg_nemesis](https://github.com/xwtk/hmg_nemesis). This document covers **environment and tooling only** — do not treat it as a product roadmap.

## Prerequisites (host Linux recommended)

Upstream builds from `Sources/` with GNU Make. From the Makefiles and test docs, you need:

| Tool | Used for | Notes from upstream |
|------|----------|---------------------|
| **Python 3** | Package builders, tar packaging, host tests | `Sources/tools/*.py`, `make_troubleshooter_tar.py` |
| **GNU Make** + **bash** | Top-level and per-component builds | `Sources/Makefile` |
| **C compiler (`cc`)** | Host unit test `make test-meta` | Not the car binary |
| **Android NDK r21e** | Native `nemesis` ARM binary | Default `NDK=$(HOME)/android-ndk-r21e`; override with `make NDK=/path/to/android-ndk-r21e` |
| **JDK** (javac / jarsigner / keytool) | Manager APK (Java 7 source/target) | `Sources/manager/Makefile` |
| **Android SDK platform android-30** | `android.jar` bootclasspath | Default `ANDROID_JAR=/usr/lib/android-sdk/platforms/android-30/android.jar` |
| **Android build-tools** | `aapt`, `d8`, `zipalign` | On `PATH` as `AAPT` / `D8` / `ZIPALIGN` |
| **zip** | APK assembly | |
| **unshare** (+ optional **mksh**) | Some hook tests | `Sources/tests/README.md` |

Optional / not published:

- **QEMU Gen5W lab** — `make test-emu` requires `NEMESIS_EMU_DIR`; the system image is **not** redistributed with the repo.
- **smali** — needed for `Sources/engine_sound` APK builds.

### Typical host build

```bash
cd Sources
make                    # native + manager + packages + examples + troubleshooter tar
make test-meta          # host C test
make test-fart          # fart-button host unit tests (PressCounter / session; needs JDK)
make test-hooks         # includes test-fart, plus Manager/store/hook checks (needs JDK; APK checks need a built Manager)
```

Signing keystore for Manager is **generated locally** by `make -C manager keystore` (password in Makefile for the stock NEMESIS debug key). Do not commit `*.keystore` or build outputs.

## Hardware / USB (vehicle)

NEMESIS targets **Hyundai Motor Group Gen5W** head units with **S5W_M** and **OTA update** support. Ioniq 5 units that meet those conditions are in scope for this project; verify SW Info on the car before any install.

High-level USB flow (details in root [README.md](README.md) and `Sources/usb_bundle/`):

1. Blank **exFAT or FAT32** USB stick.
2. Copy `ota_package.tar` to the stick root (do not extract/repack).
3. Car HU USB port → Engineering Mode → USB Copy → Update DSP → **Update OTA**.
4. Expect “No Supported Files” style messages; watch for OTA loading animation, then HU reboot.

Further device verification notes: `Sources/usb_bundle/DEVICE_TEST.md`, `Sources/usb_bundle/README.md`.

**Risk:** Modifying head-unit software can brick or impair the vehicle. You are responsible for any procedure on your car.

## Git remotes

```bash
git remote -v
# origin    https://github.com/seattlesuekim/fart-button.git
# upstream  https://github.com/xwtk/hmg_nemesis.git
```

Fetch upstream periodically:

```bash
git fetch upstream
# merge or rebase upstream/main into your feature branch as appropriate
```

## IDE (Cursor / VS Code)

Workspace recommendations live under [`.vscode/`](.vscode/):

- `extensions.json` — Java, C/C++, Python, Makefile Tools, EditorConfig
- `settings.json` — excludes build junk, file associations
- `tasks.json` — stubs for common `make` targets under `Sources/`
- `launch.json` — placeholder debug configs (host tests / attach); fill in paths after installing the NDK/SDK

Open the repo root as the workspace folder.

## Cursor Cloud Agents

Workers on this Project **cannot** author or install a custom Cloud Agent `environment.json`. If you need SDK/NDK preinstalled in the cloud VM, create or edit an environment yourself:

https://cursor.com/dashboard/cloud-agents/environments

No repository secrets are required for the stock upstream host build. Vehicle access, Discord, or xwtk.Portal credentials (for package store publishing) stay with you — do not commit them.

## Scope reminder

Push **setup / docs / IDE / config** changes freely. Do **not** land fart-button product logic in foundation PRs unless that work is explicitly requested.
