# Voice Check

- Claim is specific and measurable: 4,965.35 records/s median, 267.48 ms batch p95, invariant 1.0.
- All five raw throughput and latency samples are published.
- Variance, single-node scope, and untested crash/restart semantics are explicit.
- No generic claim of robustness, scale, production readiness, or end-to-end external exactly-once appears.
- Broker and topology-driver metrics are not compared as equivalent.
- Image, source, lock, workload, and artifact provenance are named.
- Security wording states the exact severity gate and that no suppression was used.
- Commands are included only after local execution; remote CI remains explicitly pending.
