# fart — host-testable core

Pure Java (no `android.*`) for the fart-button session: press counting, climate port, snapshot store, and state machine.

Run on a development Mac/Linux host:

```bash
cd Sources
make test-fart
```

On-unit OEM climate and My Menu wiring live outside this module (see Project technical design).
