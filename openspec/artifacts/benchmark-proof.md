# Benchmark Proof

Primary benchmark: `real-broker-end-to-end`.

- Metric: `end_to_end_input_records_per_second`, records/s, higher is better.
- Workload: 1,000 records, seed 42, one warmup, five measured iterations, one producer, three partitions.
- Correctness: output and aggregate ratios must both equal 1.0.
- Result: `benchmarks/results/baseline.json`.
- Command: `pwsh -File tools/run-broker-benchmark.ps1 -Records 1000 -Warmups 1 -Repeats 5 -OutputPath benchmarks/results/baseline.json`.

Current dirty smoke: 121.93 records/s median; it is development evidence and cannot satisfy the release gate.
