# Current Handoff

Generated: 2026-07-30
Trigger: conflict-free PR-CI boundary and quota-continuity requirement

## Goal

Close `#28 kafka-streams-demo` completely before opening another portfolio repository. Implementation, benchmark, scans, SBOM, technical review, and a full manual CI run are complete. Completion still requires green `pull_request` CI with dependency review; merge remains explicitly unauthorized.

## Canonical worktree

- Worktree: `kafka-wrapper-hardening-v2`.
- Branch: `agent/gradle-wrapper-hardening-v2`.
- Current published head before this documentation sync: `7e906b02dcac8ff5f8e2080c28bb5d472b99459d`.
- Origin: https://github.com/Brilhante29/kafka-streams-demo.git.
- PR: https://github.com/Brilhante29/kafka-streams-demo/pull/1, draft, base `main`, not merged.
- `main` was created at audited common ancestor `ab9535a`; the prior contract-sync branch remains preserved.

## Current facts

- Clean release baseline: 4,965.3503453386265 records/s median, 201.395658 ms median batch latency, 267.479334 ms p95, invariant 1.0.
- Evidence source: `eede933`; image `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`; artifact `sha256:e81a103aed906f48e91551b7f6099f148413d0a3ff12ff84bcc7eaa46200d4fd`.
- Manual workflow run `30557441814` is green for JVM/contracts/tests and broker/image-security/SBOM jobs.
- CI SBOM artifact `8765516776` is retained until 2026-08-13.
- Dependency review was skipped because the green run used `workflow_dispatch`.
- CI convergence fixed exactly three platform defects: PowerShell 7 constant collision, Unix dotfile metadata, and non-root bind-file creation.
- Independent review remains P0 0; four technical P1 resolved; publication P1 waits only for PR CI/dependency review.

## Decisions to preserve

- Real-broker end-to-end rate is primary; topology-driver metrics remain separate.
- Domain has no serialization, Kafka, framework, Docker, or cloud imports.
- Runtime stays UID 10001 and read-only; host preparation grants write only to the evidence file.
- Scanner policy has no `ignore-unfixed` or other suppression.
- Raw samples and variance accompany the median; this is not a capacity SLA.
- PR remains draft. Do not merge without explicit authorization.

## Exact next actions

1. Validate and commit this remote-evidence documentation update.
2. Push it to PR #1 so GitHub emits `pull_request/synchronize` against `main`.
3. Observe quality, dependency review, broker, Trivy, and SBOM jobs.
4. If all pass, update the PR review status without another code change and request explicit merge authorization.

## Limit handling

Exact weekly account balance is not exposed to repository code or this agent. At any Codex quota warning, finish only the smallest safe unit, refresh this file and `CONTINUITY_STATE.md`, run `git diff --check`, and stop heavy work. Record facts, decisions, commands, results, and exact next actions; never store credentials or private chain-of-thought.
