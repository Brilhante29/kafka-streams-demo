# Benchmark Proof

Primary benchmark: `real-broker-end-to-end`.

- Metric: `end_to_end_input_records_per_second`, records/s, higher is better.
- Workload: 1,000 records, seed 42, one warmup, five measured iterations, one producer, three partitions.
- Correctness: output invariant ratio is 1.0 in every measured iteration.
- Runtime: Kafka 4.3.1, `exactly_once_v2`, read-committed output, Docker Desktop amd64.
- Result: `benchmarks/results/baseline.json`.
- Source: `eede9335508b239c6c88ca105965ab67dce5eb34`, clean tree.
- Image: `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`.
- Artifact: `sha256:e81a103aed906f48e91551b7f6099f148413d0a3ff12ff84bcc7eaa46200d4fd`.

## Results

Throughput samples: 4,122.48; 3,738.61; 4,965.35; 5,237.01; 5,308.42 records/s.

Median throughput: **4,965.35 records/s**.

Batch latency samples: 242.57; 267.48; 201.40; 190.95; 188.38 ms.

Median/p95 batch latency: **201.40/267.48 ms**.

The throughput population CV is 13.47% and max/min ratio is 1.42. These raw samples and the single-node limitation are part of the proof; the median is not a scale or SLA claim.

Command: `pwsh -File tools/run-broker-benchmark.ps1 -Records 1000 -Warmups 1 -Repeats 5 -OutputPath benchmarks/results/baseline.json`.
