# Software Design Description

Project: `#28 kafka-streams-demo`
Status: implementation validated; release evidence pending

## Context and problem

A data platform must enrich purchase events with the latest customer profile and maintain a per-customer summary without coupling domain policy to broker operations. The repository must prove both topology correctness and real broker behavior. A broker-free test cannot be presented as Kafka throughput, so the two evidence modes are separate.

Primary consumers are backend/data engineers evaluating the topology, and reviewers evaluating Kotlin/JVM, Kafka Streams, testing, Docker, CI, and benchmark discipline.

## Goals

- Enrich each valid purchase with the latest profile for the same `customerId`.
- Emit one enriched record and one aggregate update for each accepted purchase.
- Run against Kafka 4.3.1 with three partitions and `exactly_once_v2`.
- Keep domain contracts and policies independent from Kafka and runtime code.
- Produce benchmark-result-v2 evidence with raw samples and provenance.
- Run locally without credentials or paid services.

## Non-goals

- HTTP, REST, GraphQL, gRPC, or a user interface.
- Schema Registry, a database, cloud storage, or Kumo when no cloud behavior exists.
- Multi-region deployment, cluster sizing, or a production SLA.
- DLQ/quarantine behavior without a product requirement defining recovery ownership.
- Claiming broker throughput from `TopologyTestDriver`.

## Requirements

| ID | Requirement | Verification |
|---|---|---|
| FR-01 | Join `purchases` with the latest `customer-profiles` row by key. | `TopologyTest`, `RealBrokerBenchmarkTest` |
| FR-02 | Publish enriched records to `enriched-purchases`. | topology and broker output invariant |
| FR-03 | Maintain `customer-summary-store` and publish `customer-summaries`. | topology test and aggregate delta invariant |
| FR-04 | Ignore purchases whose profile does not exist. | `TopologyTest` unknown-profile case |
| FR-05 | Expose `demo`, `topology-benchmark`, `broker-benchmark`, and `run` CLI modes. | Docker smoke and CLI tests/build |
| NFR-01 | Broker mode uses three partitions and `exactly_once_v2`. | manifest, properties, benchmark environment |
| NFR-02 | Domain source imports no Kafka, Docker, cloud, transport, or framework type. | dependency review and source audit |
| NFR-03 | Evidence records fixture/config digests, samples, source commit, lock digest, image digest, and environment. | benchmark validator |
| NFR-04 | Runtime is non-root, read-only, capability-dropped, and uses pinned images. | Dockerfile/Compose validation and image scan |
| NFR-05 | Build uses Gradle Wrapper 9.3.0 with audited wrapper/distribution checksums. | `validate-gradle-project.ps1` |

## Architecture

The selected style is event-driven with minimal inward dependency boundaries.

```text
CLI / benchmark harness
        |
        v
Kafka Streams topology and Serdes ----> Kafka broker and state stores
        |
        v
Domain events and pure policies
```

Dependencies point inward from `infra` and `benchmark` to `domain`. Kafka Streams remains visible at the application boundary because its DSL, state model, and processing guarantee are the subject of the repository. Wrapping every DSL class behind custom ports would add indirection without isolating a volatile business dependency.

## Components

- `domain`: purchase/profile/enriched/summary contracts and pure enrichment/aggregation policy.
- `infra`: JSON Serdes, topic/store names, topology, and runtime properties.
- `benchmark`: deterministic fixtures, topology driver harness, real broker harness, metrics, and evidence provenance.
- `App.kt`: CLI mode selection and runtime startup only.
- `docker-compose.real.yml`: disposable Kafka KRaft broker and hardened benchmark container.

## Data and flow

1. `customer-profiles` is keyed by `customerId` and materialized as `customer-profile-store`.
2. `purchases` is keyed by `customerId` and joins the latest profile table.
3. Joined records are emitted to `enriched-purchases`.
4. Enriched records are keyed by `customerId`, aggregated into `customer-summary-store`, and emitted to `customer-summaries`.
5. Broker benchmark consumes output with `read_committed` and queries the store until both output and state invariants hold.

No event-time window is used. Ordering is per customer key. The profile topic is compacted in the benchmark broker.

## Public interfaces

The public interface is the CLI:

- `demo`
- `topology-benchmark [records] [warmups] [repeats] [output]`
- `broker-benchmark [records] [warmups] [repeats] [output]`
- `run`

`run` and `broker-benchmark` require `KAFKA_BOOTSTRAP_SERVERS`. Benchmark output follows `.portfolio/contracts/benchmark-result-v2.schema.json`.

## Error model

Invalid numeric CLI input reaches explicit `require` checks and terminates non-zero. Missing broker configuration fails before startup. Broker startup observes state and uncaught exceptions and fails with the terminal state/cause. Producer/consumer/store waits have 60-second deadlines. Deserialization or processing failures are fail-fast; no record is silently dropped into an undefined DLQ.

## Security

No secret is needed. Images and wrapper artifacts are pinned. The runtime uses UID/GID 10001, a read-only root filesystem, dropped capabilities, `no-new-privileges`, bounded tmpfs mounts, and loopback-only host broker exposure. RocksDB JNI receives a dedicated executable tmpfs; `/tmp` remains `noexec`. CI permissions are read-only except artifact upload performed with the workflow token's standard Actions permission.

## Observability

The benchmark emits structured JSON metrics and explicit failures. Kafka client logs remain available at warning/error level. This CLI topology has no HTTP health endpoint; broker readiness is verified by the Compose healthcheck, and Kafka Streams readiness is its `RUNNING` state.

## Test strategy

- Unit: pure domain enrichment behavior.
- Topology: join, aggregate, unknown profile, and benchmark evidence using `TopologyTestDriver`.
- Integration: Testcontainers Kafka 4.3.1 processes records and verifies output/state invariants.
- End-to-end smoke: Compose broker plus hardened application image.
- Release: clean-tree benchmark, validators, secret/dependency/image scans, SBOM, and independent review.

## Benchmark design

The primary public metric is `end_to_end_input_records_per_second` from the real broker path. It includes producer flush, Kafka Streams processing, committed output consumption, and aggregate-store convergence. The full release workload is 1,000 records, one warmup, five measured iterations, one producer, three partitions, seed 42.

`topology_input_records_per_second` is a separate deterministic microbenchmark. It must never be compared directly with broker throughput.

## Deployment

The default image runs `demo`. `docker-compose.real.yml` creates one Kafka 4.3.1 KRaft node for local evidence. Production deployment must add authenticated listeners, replication greater than one, retention/capacity policy, multi-instance rebalance testing, and operational alerting.

## Risks and trade-offs

- One local broker does not prove high availability or cluster scaling.
- JSON Serdes favor inspectability over schema-registry evolution controls.
- Interactive store queries run in-process; remote query routing is out of scope.
- Exactly-once-v2 protects Kafka read-process-write semantics, not external side effects.
- Throughput depends on Docker Desktop CPU/memory and is only comparable with the same comparability key and workload.

## Acceptance criteria

- All functional and affected integration tests pass.
- Docker image builds and `demo` runs as UID 10001.
- Broker smoke reports a positive primary metric and invariant ratio 1.0.
- Release baseline validates with `clean_tree=true` and non-zero commit/image/lock digests.
- README, manifest, SDD, and baseline name the same primary metric.
- CI is green; scans and SBOM have evidence; independent review has no P0/P1.

## Traceability

| Requirement | Implementation | Test/evidence |
|---|---|---|
| FR-01/FR-02 | `TopologyFactory.build` | `TopologyTest`, broker baseline |
| FR-03 | named aggregate store | topology aggregate assertions, state delta |
| FR-04 | stream-table inner join | unknown-profile test |
| FR-05 | `App.kt` | Gradle/Docker smoke |
| NFR-01 | `TopologyFactory.properties`, topic creation | broker evidence environment |
| NFR-02 | `domain/**` | source/dependency review |
| NFR-03 | `BenchmarkEvidence.kt` | `validate-benchmark-result.ps1` |
| NFR-04 | Dockerfile/Compose | Docker smoke and Trivy |
| NFR-05 | wrapper files and manifest | Gradle validator |
