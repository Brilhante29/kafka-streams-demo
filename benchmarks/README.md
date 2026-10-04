# Benchmarks

## Evidence modes

`topology-test-driver` is a deterministic topology microbenchmark. It proves join/aggregate behavior and supports fast regression feedback without a broker.

`real-broker-end-to-end` is the public benchmark. It measures producer batch submission through Kafka 4.3.1, Kafka Streams `exactly_once_v2`, read-committed output, and aggregate-store convergence.

The modes use different benchmark IDs and must not be compared as equivalent throughput measurements.

## Release evidence

The committed release artifact is `results/baseline.json`. It must:

- follow benchmark-result-v2;
- come from a clean Git worktree;
- contain five primary samples after one warmup;
- name `end_to_end_input_records_per_second` as the first metric;
- report `output_invariant_ratio=1.0` with zero failures;
- include full source, image, dependency-lock, workload, and environment provenance.

Development/CI JSON files are ignored. `archive/` preserves explicitly labeled historical evidence that cannot satisfy current gates.

See `sdd/benchmark-plan.md` for the measurement boundary, command, interpretation, and limitations.
