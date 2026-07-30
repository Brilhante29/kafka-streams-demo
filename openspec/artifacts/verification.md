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

Remote evidence:

- Manual GitHub Actions run 30557441814 is green for JVM/contracts/tests and broker/image-security/SBOM jobs on commit 7e906b0.
- SBOM artifact 8765516776 is retained until 2026-08-13.
- Three demonstrated cross-platform CI defects were fixed by commits 43a2bc0, 444c784, and 7e906b0.

Pending publication evidence:

- Pull-request-triggered CI and dependency review on conflict-free base ~main~.
- Explicit merge authorization.
