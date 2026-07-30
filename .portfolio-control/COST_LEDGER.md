# Cost Ledger

| Date | Operation/failure | Count | Useful outcome | Optimization |
|---|---|---:|---|---|
| 2026-07-26 | Weekly usage gate | 1 | Preserved state before implementation | checkpoint immediately on future warning; no heavy command starts afterward |
| 2026-07-30 | Gradle incremental/full checks | 4 | isolated accessor, lint, deprecation, and parse failures | run formatter/compile before full Docker build; warm shared Gradle cache |
| 2026-07-30 | Docker image builds | 2 | proved runtime and cached unchanged layers | rebuild only after source/runtime layer changes |
| 2026-07-30 | Broker smoke attempts | 3 | fixed healthcheck, RocksDB noexec, and hidden tmpdir causes | use fail-fast state listener and specific tmpfs mounts |
| 2026-07-30 | Integrated patch sandbox failure | 2 | confirmed Windows split-root limitation | stop retrying; use asserted unique-anchor transformations |
| 2026-07-30 | PowerShell edit-script mistakes | 2 | no lasting data loss; test restored | inspect exact lines/diff before recompilation; avoid embedded quote interpolation |
| 2026-07-30 | Heavy subagents | 0 | principal agent retained file ownership | reserve one independent reviewer for release candidate |

Avoidable work is recorded to change the next action, not to create more reports. Full suites/builds/benchmarks are capped by the master execution budget.
