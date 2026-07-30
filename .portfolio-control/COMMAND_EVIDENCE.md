# Command Evidence

## Gradle incremental validation

Command: `./gradlew --no-daemon ktlintFormat check compileIntegrationTestKotlin` inside pinned Temurin JDK 21.

Result: build passed; five unit/topology/configuration tests passed; integration test source compiled. Exit code 0. Date: 2026-07-30.

## Docker image build

Command: `docker compose -f docker-compose.real.yml build streams-app`.

Result: multistage image built; Gradle clean/check/installDist passed in build stage. Image smoke digest before final commit was `sha256:4a04091d59d63fcb0c79e0f117f78d312bded60e2147728f2c0aa911f711c4a0`. Exit code 0. Date: 2026-07-30.

## Real broker smoke

Command: `tools/run-broker-benchmark.ps1 -Records 30 -Warmups 1 -Repeats 2 -OutputPath benchmarks/results/broker-smoke.json -AllowDirty`.

Result: Kafka 4.3.1, exactly-once-v2, three partitions; median 121.92524101013193 records/s; p95 batch latency 246.052415 ms; output invariant 1.0. Exit code 0. Dirty development evidence only. Date: 2026-07-30.

## Benchmark validation

Command: `tools/validate-benchmark-result.ps1 -Path benchmarks/results/broker-smoke.json -ExpectedBenchmarkId real-broker-end-to-end`.

Result: schema version, workload, samples, metrics, invariants, environment, and provenance semantics passed. Exit code 0. Date: 2026-07-30.

## Manifest schema

Command: Python `jsonschema.Draft202012Validator(...).validate(project.yaml)`.

Result: manifest V2 passed the vendored project schema. Exit code 0. Date: 2026-07-30.

## Secret fallback

Command: `tools/scan-secrets.ps1`.

Result: 159 candidate files scanned; no configured credential pattern found. Exit code 0. Date: 2026-07-30.

Full release commands and logs remain pending and will be recorded once, after the implementation commit is clean.
