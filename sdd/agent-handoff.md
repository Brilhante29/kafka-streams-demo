# Agent Handoff

## Objective

Finish publication of `#28 kafka-streams-demo` before opening another portfolio repository.

## Canonical state

- Worktree: `kafka-wrapper-hardening-v2`.
- Branch: `agent/gradle-wrapper-hardening-v2`.
- Clean evidence source: `eede9335508b239c6c88ca105965ab67dce5eb34`.
- Remote is not configured in this worktree; verify the expected public repository before push.
- Baseline, SBOM, reviews, and final documentation are intentionally uncommitted until final structural validation.

## Decisions

- Kotlin 2.4.10, Java 21, Gradle Wrapper 9.3.0, Kafka Streams 4.3.1.
- Real-broker benchmark is the public metric; TopologyTestDriver remains a separate microbenchmark.
- Broker mode uses `exactly_once_v2`, three partitions, idempotent production, and read-committed output.
- Domain records import no serialization, Kafka, framework, Docker, or cloud type; infrastructure owns wire DTO mapping.
- No Spring, HTTP/GraphQL/gRPC, database, Kumo, or AWS because none contributes to the claim.
- Scans use no `ignore-unfixed` or finding suppression.
- Do not merge without explicit authorization.

## Final local evidence

- Five JVM tests: passed.
- Testcontainers Kafka integration: passed.
- Clean five-sample broker baseline: 4,965.3503453386265 records/s median; 201.395658 ms median latency; 267.479334 ms p95; invariant 1.0.
- Throughput population CV: 13.47%; max/min ratio: 1.42.
- Benchmark semantic and canonical digest validation: passed; tampered metric rejected.
- Image: `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`.
- Evidence: `sha256:e81a103aed906f48e91551b7f6099f148413d0a3ff12ff84bcc7eaa46200d4fd`.
- Trivy filesystem and image HIGH/CRITICAL gates: passed without suppression.
- SPDX 2.3 SBOM: 158 packages; 25 `NOASSERTION` licenses reviewed; SHA-256 `5653a530a575798d907aa42d9bdc2fc889d9b8f00167014271aa89f68665496f`.
- Independent review: P0 0; five P1 total; four technical P1 resolved; publication P1 pending PR/CI.

## Bounded claims and backlog

- Single local KRaft broker is not a scale or availability result.
- Exactly-once crash/restart/retry behavior is not yet measured.
- Five samples with 13.47% CV require publishing raw values, not only the median.
- Runtime image still includes topology-test tooling for the benchmark CLI.
- General `run` service retains a writable results mount.

## Exact next action

Verify the GitHub remote, push the validated branch, open a PR, and observe CI without merging.

## Limit continuity

Exact weekly usage balance is unavailable. At any product quota warning, finish the current atomic action, refresh `.portfolio-control/CURRENT_HANDOFF.md` and `CONTINUITY_STATE.md`, run `git diff --check`, and stop heavy commands. Record facts and decisions, never private chain-of-thought or credentials.
