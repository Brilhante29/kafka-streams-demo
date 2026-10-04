# Benchmark Plan

## Claims and separation

The public claim is the real-broker end-to-end rate. `TopologyTestDriver` is a deterministic topology microbenchmark and is never described as broker throughput.

## Primary workload

- Benchmark ID: `real-broker-end-to-end`.
- Metric: `end_to_end_input_records_per_second` (`records/s`, higher is better).
- Secondary: `end_to_end_batch_latency_ms` and `output_invariant_ratio`.
- Broker: Kafka Native 4.3.1, one KRaft node.
- Processing: `exactly_once_v2`, three partitions, read-committed output.
- Fixture: seed 42, three profiles, deterministic purchases.
- Release size: 1,000 records, one warmup, five measured iterations, concurrency one.

```powershell
pwsh -File tools/run-broker-benchmark.ps1 -Records 1000 -Warmups 1 -Repeats 5 -OutputPath benchmarks/results/baseline.json
pwsh -File tools/validate-benchmark-result.ps1 -Path benchmarks/results/baseline.json -ExpectedBenchmarkId real-broker-end-to-end -RequireClean
```

The runner refuses dirty release evidence unless `-AllowDirty` is explicitly used for smoke work.

## Measurement boundary

Timing begins before producing a batch and ends after all enriched records are consumed and aggregate state reaches the expected count. Producer flush, broker I/O, Streams processing, transaction visibility, output polling, and state convergence are included.

## Correctness invariants

Every measured input must produce one enriched output and increment aggregate order count once. `output_invariant_ratio` must equal 1.0 for every measured iteration; otherwise the run fails and no evidence is written as successful.

## Evidence

The V2 JSON includes raw samples, warmups/repeats, fixture/config digests, runtime/OS/architecture, broker image, processing guarantee, partition count, source commit, clean-tree flag, image digest, dependency-lock digest, producer, and comparability key.

## Interpretation limits

This is a single-node Docker Desktop measurement, not a cluster capacity result or SLA. Compare results only when workload and comparability key match. CI runs a smaller smoke workload; only the committed clean baseline is used in public README numbers.
