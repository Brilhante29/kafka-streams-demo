# Article Draft

## Exactly-once is not a checkbox

The useful question is not how many records an in-memory topology can process. It is where measurement starts and ends. This project times a producer batch through Kafka 4.3.1, Kafka Streams `exactly_once_v2`, read-committed output, and materialized aggregate convergence.

The clean run produced 4,965.35 records/s median throughput and 267.48 ms batch p95 across five samples, with output invariant 1.0 every time. The throughput samples were 4,122.48, 3,738.61, 4,965.35, 5,237.01, and 5,308.42 records/s. Their 13.47% population CV is why the post includes every raw value instead of presenting one polished number as an SLA.

The evidence also binds source commit, application image, dependency lock, workload/configuration, and its own canonical SHA-256 digest. Changing a metric without recomputing the artifact fails validation.

The boundary matters: this is one local KRaft broker with replication factor one. It proves normal broker-backed read-process-write behavior and state convergence, not cluster availability, crash/restart recovery, or external-side-effect exactly-once semantics. TopologyTestDriver remains a separate correctness and microbenchmark tool.
