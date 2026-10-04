# Independent Release Review

Reviewer: Darwin, read-only subagent
Review date: 2026-07-30
Reviewed release source: `eede9335508b239c6c88ca105965ab67dce5eb34`

## Summary

- P0: 0.
- P1: 5 total.
- Technical P1: 4 resolved in `eede933` and revalidated locally.
- Publication-state P1: 1 open until conflict-free PR CI, including dependency review, is observed green.
- P2/P3: retained as explicit backlog; they do not weaken the current bounded claim.

## Findings

| Severity | Finding | Resolution/evidence | State |
|---|---|---|---|
| P1 | Secret-bearing local files lacked complete ignore coverage. | `.gitignore` covers environment variants, credential JSON, secret directories, key/certificate stores, and common SSH keys while keeping `.env.example` trackable. Ignore behavior was exercised locally. | resolved |
| P1 | Trivy used `ignore-unfixed`, contradicting the no-suppression claim. | Both CI entries and final local scans omit `ignore-unfixed` and all finding suppressions; filesystem and image scans exit zero. | resolved |
| P1 | `artifact_digest` was decorative and did not bind evidence content. | Producer and validator now compute canonical SHA-256 over the full evidence payload with the digest field zeroed. Valid evidence passes; a tampered metric fails. | resolved |
| P1 | Domain records imported serialization annotations. | Wire DTOs and JSON mapping moved to `infra/DomainJsonSerdes.kt`; domain records have no serialization/framework import. Unit and broker integration tests pass. | resolved |
| P1 | Release evidence and remote CI were not yet published. | Evidence is committed; manual run 30557441814 is green and uploaded SBOM artifact 8765516776. Final resolution requires pull-request CI and dependency review on ~main~. | open |
| P2 | `kafka-streams-test-utils` remains an application dependency and reaches the runtime image for the topology-benchmark CLI. | Separate benchmark tooling from the minimal runtime in a measured follow-up. | backlog |
| P2 | Exactly-once behavior lacks crash/restart/retry evidence. | Current claim is normal broker-backed read-process-write only; add a failure-injection benchmark later. | backlog |
| P2 | Five local samples retain meaningful variance. | Publish raw samples, 13.47% population CV, and max/min 1.42; do not claim an SLA. Increase warmups/repeats in a later comparison study. | bounded |
| P3 | The results bind mount is writable for the general `run` service. | Restrict the mount to a benchmark-specific service in a later hardening change. | backlog |

## Review boundary

This report stores findings, decisions, evidence, and next actions. It does not contain private chain-of-thought. The reviewer made no merge decision.
