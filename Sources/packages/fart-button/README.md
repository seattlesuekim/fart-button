# fart.button (NEMESIS package)

Installable package skeleton for the Ioniq 5 fart-button feature.

| Piece | Status |
|-------|--------|
| Host core (`Sources/fart`, `make test-fart`) | Done |
| This package zip (`fart.button`) | Scaffold — Launch logs only |
| OEM `ClimatePort` | Blocked on on-car HVAC spike |
| 2× My Menu receiver | Next after climate (or parallel APK) |

Build:

```bash
cd Sources
python3 tools/make_nemesis_package.py packages/fart-button -o out/fart.button.zip
```

Install on a unit that already runs NEMESIS (USB install or Manager). Launching the app does **not** change HVAC until the OEM adapter exists.
