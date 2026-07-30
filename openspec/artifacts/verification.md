# Verification

Verified locally on 2026-07-30:

- Five JVM unit/topology/configuration tests pass.
- Digest-pinned Kafka 4.3.1 Testcontainers integration passes.
- Docker/Compose runtime is healthy; application runs as UID 10001.
- Clean five-sample real-broker baseline passes with invariant 1.0.
- V2 semantic and canonical artifact-digest validation passes; tampered evidence fails.
- Project manifest V2 JSON Schema validation passes.
- Fallback secret scan passes.
- Trivy filesystem and application-image HIGH/CRITICAL gates pass without suppression.
- SPDX 2.3 SBOM contains 158 packages and has a retained license review.
- Independent read-only review reports P0 0; four technical P1 findings are resolved in `eede933`.

Pending publication evidence:

- GitHub PR dependency review, CI jobs, and uploaded SBOM artifact.
- Explicit merge authorization.
