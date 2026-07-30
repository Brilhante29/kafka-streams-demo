# Article Draft

## Exactly-once is not a checkbox

The useful question is not how many records an in-memory topology can process. It is where measurement starts and ends. This project times a producer batch through Kafka, Kafka Streams `exactly_once_v2`, read-committed output, and materialized aggregate convergence.

The post will publish five raw samples, median throughput, p95 batch latency, invariant ratio, workload, image/lock/source digests, and the single-node limitation. TopologyTestDriver remains a correctness/microbenchmark tool and is reported separately.
