# #28 kafka-streams-demo

**Status:** scaffold

**Proves:** processamento streaming.

**Benchmark target:** messages_per_second.

**Stack:** java21, kafka-streams, redpanda, testcontainers, docker.

## Next milestone

Implement the smallest Docker-runnable version and produce the first JSON benchmark under enchmarks/results/.

## Run

`ash
docker build -t kafka-streams-demo .
docker run --rm kafka-streams-demo
`

## Benchmark

`ash
docker run --rm kafka-streams-demo benchmark
`

| Metric | Value | Unit |
|---|---:|---|
| messages_per_second | pending | pending |

## Architecture

Defined in sdd/spec.md before implementation.

## References

See REFERENCES.md.