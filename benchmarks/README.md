# Benchmark

The benchmark uses a fixed deterministic fixture with seed `42`, three profile
records and a caller-selected batch of purchase events. It feeds the topology
through `TopologyTestDriver`, so the result measures topology processing rather
than broker or network overhead.

```bash
gradle run --args="benchmark 1000 benchmarks/results/latest.json"
```

The JSON reports `messages_per_second` as the primary metric and includes batch
duration, average/p95/p99 synchronous topology latency, output counts and the
JVM/OS environment. `baseline.json` is the committed evidence file; generated
`latest.json` and CI results are ignored by Git.
