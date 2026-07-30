# Quality Gates: #28 kafka-streams-demo

- [x] Functional stream-table join and aggregate exist.
- [x] Domain is independent from serialization, Kafka, framework, and runtime imports.
- [x] Five unit/topology/configuration tests pass.
- [x] Host Testcontainers integration passes against digest-pinned Kafka 4.3.1.
- [x] Clean five-sample real-broker baseline validates with V2 provenance and canonical artifact digest.
- [x] Broker output/state invariant is 1.0 in all measured iterations.
- [x] Gradle wrapper, toolchain, lockfile, Docker, and Compose are reproducible.
- [x] Manifest V2 names the real-broker primary metric and current evidence.
- [x] README retains exactly 19 numbered sections and publishes raw samples plus limitations.
- [x] Dependency/image scans and retained SPDX SBOM complete locally.
- [x] Independent read-only review completed; four technical P1 findings are resolved in `eede933`.
- [x] Final project/Gradle/document validators pass after evidence documentation edits.
- [x] Baseline, SBOM, review, and aligned documentation are committed by this release-evidence change.
- [x] Manual GitHub Actions run 30557441814 is green for quality, broker smoke, security scans, and SBOM upload.
- [ ] Publication-state P1 is resolved by conflict-free PR CI including dependency review.
- [ ] Publication/merge is explicitly authorized.
