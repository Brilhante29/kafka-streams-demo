# Topology

```text
customer-profiles (compacted KTable, customerId)
                  \
                   +-- stream-table join --> enriched-purchases
                  /                            |
purchases (customerId)                        +-- selectKey(customerId)
                                               |
                                               +-- customer-summary-store
                                               |
                                               +-- customer-summaries
```

## Boundaries

`domain` owns immutable event/state contracts and policy. `infra` owns Serdes, topic/store names, topology composition, and Kafka properties. `benchmark` owns deterministic fixtures, broker orchestration, measurements, and evidence.

## State and semantics

- Profile store: `customer-profile-store`.
- Aggregate store: `customer-summary-store`.
- Broker guarantee: `exactly_once_v2`.
- Ordering: per `customerId` key.
- Broker benchmark: three partitions, replication factor one for local-only evidence.
- Output consumer: `read_committed`.

TopologyTestDriver verifies semantics without a broker. The real-broker harness verifies producer, broker, transactions, output, and store convergence separately.
