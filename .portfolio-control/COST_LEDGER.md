# Cost Ledger

| Date | Operation/failure | Count | Useful outcome | Optimization |
|---|---|---:|---|---|
| 2026-07-26 | Weekly usage gate | 1 | State was preserved before more heavy work | on any future warning, checkpoint immediately and stop starting long commands |
| 2026-07-30 | Gradle full/incremental validations | 8 | isolated build, lint, dependency, integration, and review regressions | run formatter and check separately; reuse the Gradle volume; rerun only invalidated tasks |
| 2026-07-30 | Docker application builds | 9 | proved runtime layers, removed a vulnerable unused binary, and produced final evidence image | rebuild only when source/runtime inputs change; never rebuild for docs |
| 2026-07-30 | Broker smoke/baseline generations | 5 | resolved health, tmpfs, startup, and evidence issues; retained one clean baseline | use fail-fast startup, one smoke during development, one baseline after a clean commit |
| 2026-07-30 | Testcontainers host/compatibility failures | 2 | established the Docker Desktop host override and native Kafka compatibility declaration | set the host override and pinned compatibility once in the harness |
| 2026-07-30 | Trivy filesystem/image runs | 6 | closed dependency/base-image findings and proved the final image without suppression | keep the DB cache volume and scan only the final digest after dependency changes |
| 2026-07-30 | Docker daemon unavailable | 1 | distinguished environment state from source failure | check Docker readiness before heavy integration commands |
| 2026-07-30 | Long-command timeout/output truncation | 3 | authoritative files/container exit status preserved the result | inspect artifacts and running containers before rerunning; wait on the existing container |
| 2026-07-30 | Integrated patch sandbox failure | 2 | confirmed the Windows split-root limitation | use asserted unique-anchor transformations; do not retry the unavailable patch path |
| 2026-07-30 | PowerShell/tool-side edit mistakes | 12 | no lasting data loss; exact anchors prevented partial writes | remove template-literal metacharacters, inspect diffs immediately, and keep edits batched by concern |
| 2026-07-30 | Final validator contract mismatch | 1 | detected renamed mandatory reusability clauses before commit | preserve machine-required checklist wording and add detail around it instead of replacing it |
| 2026-07-30 | Python shim quoting failure | 1 | established the pyenv Windows batch quoting constraint | use a double-quoted command and single-quoted Python literals; avoid f-strings in the shim |
| 2026-07-30 | Host-Java shortcut failure | 1 | confirmed that `-SkipDocker` is not valid on this host | use the Docker-backed validator directly when Java is absent |
| 2026-07-30 | Heavy subagents | 1 | independent reviewer found five release-relevant P1 gaps | keep one read-only release review; principal agent owns all writes |

This ledger tracks work that changes the next execution strategy. Exact weekly account balance remains unavailable; the observed quota-warning count is the only defensible limit metric.
