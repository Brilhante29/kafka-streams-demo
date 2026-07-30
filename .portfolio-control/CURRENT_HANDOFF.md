# Current Handoff

Generated: 2026-07-30
Trigger: local release-evidence boundary and quota-continuity requirement

## Goal

Close `#28 kafka-streams-demo` completely before opening another portfolio repository. Local implementation, benchmark, scans, SBOM, and technical review are complete. Completion still requires committed evidence and observed green GitHub Actions.

## Canonical worktree

- Worktree: `kafka-wrapper-hardening-v2`.
- Branch: `agent/gradle-wrapper-hardening-v2`.
- Evidence source commit: `eede9335508b239c6c88ca105965ab67dce5eb34`.
- Remote: not configured in this worktree; verify `Brilhante29/kafka-streams-demo` before adding it.
- Current uncommitted files are the clean baseline, final SBOM, review records, and aligned documentation. Do not discard them.

## Current facts

- Five JVM tests and the broker-backed Testcontainers integration test pass.
- Clean real-broker baseline: 4,965.3503453386265 records/s median, 201.395658 ms median batch latency, 267.479334 ms p95, invariant 1.0.
- Baseline source is clean commit `eede933`; image digest is `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`.
- Canonical evidence digest is `sha256:e81a103aed906f48e91551b7f6099f148413d0a3ff12ff84bcc7eaa46200d4fd` and rejects metric tampering.
- Trivy filesystem and application-image scans pass for HIGH/CRITICAL without suppression.
- SPDX 2.3 SBOM contains 158 packages; SHA-256 is `5653a530a575798d907aa42d9bdc2fc889d9b8f00167014271aa89f68665496f`.
- Independent review found zero P0 and five P1. Four technical P1 are fixed in `eede933`; the publication-state P1 remains until PR CI is green.

## Decisions to preserve

- Real-broker end-to-end rate is primary; topology-driver metrics remain separate.
- Domain has no serialization, Kafka, framework, Docker, or cloud imports; infrastructure owns wire DTOs.
- No Spring, API protocol, database, Kumo, or AWS without a requirement.
- Dedicated executable tmpfs is limited to RocksDB JNI; `/tmp` remains `noexec`.
- Raw samples and variance accompany the median; this is not a capacity SLA.
- Do not regenerate baseline/SBOM unless source or runtime inputs change.
- Do not merge without explicit authorization.

## Exact next actions

1. Verify/configure the GitHub remote, push the branch, open a PR, and observe CI.
2. Fix CI failures if any; update the publication review finding only after green CI. Do not merge.

## Limit handling

Exact weekly account balance is not exposed to repository code or this agent. At any Codex quota warning, finish only the smallest safe unit, run `tools/capture-continuity-state.ps1`, update this handoff with the exact next command, run `git diff --check`, and stop heavy work. The handoff records facts, decisions, commands, results, and next actions; it never stores credentials or private chain-of-thought.
