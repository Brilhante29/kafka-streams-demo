# Release Checklist

- [x] Core topology implementation is functional.
- [x] Unit and topology tests pass.
- [x] Integration test sources compile and use a pinned Kafka image.
- [x] Broker-backed Compose smoke passes with output/state invariant 1.0.
- [x] Docker image is multi-stage, pinned, non-root, and read-only at runtime.
- [x] Benchmark modes and evidence contract are implemented.
- [x] Project manifest validates against schema V2.
- [x] Local secret fallback scan passes.
- [ ] README, SDD, OpenSpec, and controls are fully aligned.
- [ ] Clean full test and Testcontainers integration test pass in release environment.
- [ ] Clean five-sample broker baseline is committed.
- [ ] Dependency and container scans pass or exceptions are documented.
- [ ] SPDX/CycloneDX SBOM is generated and retained as CI artifact.
- [ ] Independent review reports no P0/P1.
- [ ] Branch is pushed, PR is open, and GitHub Actions is green.
- [ ] Merge is explicitly authorized.
