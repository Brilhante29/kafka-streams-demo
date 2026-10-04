# Benchmark diagnosis: what `messages_per_second` actually measures

Date: 2026-10-04. Source: commit `8093e18`, Java 21.0.11, Kotlin 2.0.21, Linux amd64,
4 vCPU (Intel Xeon @ 2.10 GHz). Raw results: [`benchmarks/diagnostics/`](../benchmarks/diagnostics/).

## Question

The committed baseline reports `17.92 messages/s` with a `55.81 ms` average per record.
A stream-table join plus one aggregation should cost microseconds per record, so the
number points at the harness rather than at the topology.

## Hypothesis

`TopologyTestDriver` commits after every processed record. A commit flushes the
persistent (RocksDB) state stores behind the profile `KTable` and the
`customer-summary-store` aggregate, and writes a checkpoint. The benchmark therefore
measures one storage flush per record, which depends on the host disk.

## Experiment

Same host, same fixture (seed `42`), `2000` records, three runs per variant:

1. **RocksDB (published topology):** `benchmark 2000 <file>` from this commit.
2. **In-memory stores (diagnostic patch, not committed):** the profile table and the
   aggregate were materialized with `Stores.inMemoryKeyValueStore(...)`; nothing else
   changed.

```kotlin
// diagnostic patch applied only for variant 2
builder.table(
    PROFILES_TOPIC,
    Consumed.with(Serdes.String(), profileSerde),
    Materialized.`as`<String, CustomerProfile>(Stores.inMemoryKeyValueStore("profile-store"))
        .withKeySerde(Serdes.String())
        .withValueSerde(profileSerde),
)
// ...
Materialized.`as`<String, CustomerSummary>(Stores.inMemoryKeyValueStore(SUMMARY_STORE))
```

## Result

| Variant | Runs (messages/s) | Median messages/s | Median p95 per record |
|---|---|---:|---:|
| RocksDB stores (published topology) | 166.37 / 150.90 / 158.62 | 158.62 | 8.923 ms |
| In-memory stores (diagnostic) | 4503.30 / 4720.61 / 4621.16 | 4621.16 | 0.720 ms |

Both variants produced 2000 enriched records and 2000 summary updates. Switching only the
store type made the harness about 29 times faster on this host. The original
`17.92 messages/s` came from a different host (16 processors, run inside Docker), where the
same per-record flush was slower still.

## Conclusion

- The published number is a property of `TopologyTestDriver` plus persistent stores plus
  the host disk. It is valid as a regression signal for this harness, not as Kafka Streams
  throughput.
- The topology stays on RocksDB: persistent stores are the right default for state that must
  survive restarts without a full changelog replay. Switching stores to win a harness
  benchmark would optimize the wrong thing.
- A throughput claim needs a broker-backed benchmark (Redpanda through
  `docker-compose.real.yml`) that measures end-to-end produce-to-output latency with
  realistic commit intervals. Until then, the README labels the number for what it is.

The same review found that the real-broker `run` command reused the test-driver settings
(record cache disabled, commit after every record). Commit `8093e18` gives the broker path
Kafka Streams defaults through `TopologyFactory.runtimeProperties`.
