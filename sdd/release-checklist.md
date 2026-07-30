# Release Checklist

- [x] Core topology implementation is functional.
- [x] Five unit/topology/configuration tests pass.
- [x] Broker-backed Testcontainers integration passes with a pinned Kafka image.
- [x] Clean Compose baseline preserves output/state invariant 1.0 in all five samples.
- [x] Docker image is multi-stage, pinned, non-root, and read-only at runtime.
- [x] Benchmark V2 evidence has raw samples, clean provenance, and canonical artifact digest.
- [x] Project manifest validates against schema V2.
- [x] Local fallback secret scan passes.
- [x] Trivy filesystem and image scans pass for HIGH/CRITICAL without suppression.
- [x] SPDX 2.3 SBOM is generated, retained, and license-reviewed.
- [x] README, SDD, OpenSpec, review, and control artifacts describe the final local evidence.
- [x] Independent review completed; P0 is zero and four technical P1 findings are resolved.
- [x] Final structural validators pass after the evidence-documentation edit.
- [x] Clean baseline, SBOM, reviews, and documentation are committed by this release-evidence change.
- [ ] Publication-state P1 is closed by an open PR with observed green GitHub Actions.
- [ ] Merge is explicitly authorized.
