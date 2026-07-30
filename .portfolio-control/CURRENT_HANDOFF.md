# Current Handoff

Generated: 2026-07-30
Trigger: phase boundary and prior weekly-limit history

## Goal

Close `#28 kafka-streams-demo` completely before opening another repository. Completion requires clean benchmark evidence, security/SBOM gates, no P0/P1 review findings, and observed green GitHub Actions.

## Canonical worktree

- Repository alias: `kafka-streams-demo` worktree `kafka-wrapper-hardening-v2`.
- Branch: `agent/gradle-wrapper-hardening-v2`.
- Base HEAD: `ab9535a6f448c59b4452df440a45d7fbfbc77e31`.
- Remote: not configured in this worktree; expected public repository must be verified before push.
- Dirty files are intentional implementation, synchronized kit 1.2.0 assets, docs, and controls. Do not discard them.

## Current facts

- Kotlin/Gradle check passes; five unit/topology/configuration tests pass; integration source compiles.
- Real Kafka 4.3.1 Compose smoke passes with exactly-once-v2, three partitions, and invariant 1.0.
- Latest dirty smoke median is 121.93 records/s; it is not release evidence.
- Manifest V2 and benchmark semantic validation pass.
- Local fallback secret scan passes.
- README/SDD/OpenSpec/reuse/control artifacts are aligned but CI/validators still need final execution.

## Decisions to preserve

- Real-broker end-to-end rate is primary; topology-driver metric remains separate.
- Domain remains Kafka/framework/cloud-free.
- No Spring, API protocol, database, Kumo, or AWS without a requirement.
- Dedicated executable tmpfs is limited to RocksDB JNI; `/tmp` remains noexec.
- One independent reviewer runs only at release candidate.
- Do not merge without explicit authorization.

## Strict next actions

1. Finish `.github/workflows/ci.yml` with pinned actions, Gradle/test/integration, broker smoke, Trivy gates, dependency review, and SBOM.
2. Run project/Gradle structural validators, Compose config, JSON/YAML parsing, secret fallback, `git diff --check`, and scan for stale versions/placeholders/mojibake.
3. Stage `gradlew` as mode 100755 and create a logical implementation/docs commit.
4. From the clean commit, run the single release-candidate sequence: clean tests, integration, Docker demo, full five-sample broker baseline, dependency/image scans, and SBOM.
5. Validate and commit release evidence; update README/manifest/control number and status.
6. Run one independent P0/P1 review and fix blockers only.
7. Verify/configure remote, push branch, open PR, observe CI. Do not merge automatically.

## Limit handling

Exact weekly balance is not available to repository code. At any product warning or before a long command near a limit, finish the smallest safe unit, run `tools/capture-continuity-state.ps1`, update this file with facts and the exact next command, run `git diff --check`, and stop spawning work. Never store secrets or private chain-of-thought.
