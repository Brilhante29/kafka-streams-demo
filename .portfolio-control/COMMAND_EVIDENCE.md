# Command Evidence

## JVM validation

Command: `./gradlew --no-daemon check --rerun-tasks` inside pinned Temurin JDK 21.

Result: five unit/topology/configuration tests passed. Exit code 0. Date: 2026-07-30.

Command: `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal ./gradlew --no-daemon integrationTest --rerun-tasks` inside Docker.

Result: broker-backed Testcontainers integration passed against digest-pinned Kafka 4.3.1. Exit code 0. Date: 2026-07-30.

## Release image and runtime

Command: `docker compose -f docker-compose.real.yml build streams-app` as part of the release benchmark.

Result: clean/check/installDist passed in the build stage; image ID `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`. Exit code 0. Date: 2026-07-30.

Commands: `docker run --rm --entrypoint id kafka-streams-demo:local -u` and `docker run --rm kafka-streams-demo:local demo`.

Result: UID `10001` and local demo output both passed. Exit code 0. Date: 2026-07-30.

## Clean real-broker baseline

Command: `tools/run-broker-benchmark.ps1 -Records 1000 -Warmups 1 -Repeats 5 -OutputPath benchmarks/results/baseline.json`.

Result: Kafka 4.3.1, `exactly_once_v2`, three partitions. Throughput samples: 4122.476926115315, 3738.606587079359, 4965.3503453386265, 5237.007723565175, 5308.418391934937 records/s. Median: 4965.3503453386265 records/s. Batch-latency samples: 242.572613, 267.479334, 201.395658, 190.948735, 188.380027 ms; median 201.395658 ms; p95 267.479334 ms. Invariant ratio 1.0 in all five runs. Exit code 0. Date: 2026-07-30.

Provenance: source `eede9335508b239c6c88ca105965ab67dce5eb34`, clean tree true, image digest `sha256:913190bf4f387cca93a09d66a3c7ca8969f9636223aad993462e39f21a6af61c`, lock digest `sha256:cc32026725b8b8e31a8b3b6ea6eb159772d9a29a089ad6cf4ef37360249970f6`, artifact digest `sha256:e81a103aed906f48e91551b7f6099f148413d0a3ff12ff84bcc7eaa46200d4fd`.

## Benchmark validation

Command: `tools/validate-benchmark-result.ps1 -Path benchmarks/results/baseline.json -ExpectedBenchmarkId real-broker-end-to-end -RequireClean`.

Result: V2 schema, canonical artifact digest, workload, raw samples, broker mode, exactly-once guarantee, partition count, invariants, environment, and provenance passed. A cross-language tamper check rejected a modified metric. Exit code 0. Date: 2026-07-30.

## Security scans

Command: digest-pinned Trivy 0.69.3 `fs --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --exit-code 1 /repo`.

Result: Gradle vulnerabilities 0; Dockerfile misconfigurations 0; no secret finding. No `ignore-unfixed` or suppression was used. Exit code 0. Date: 2026-07-30.

Command: digest-pinned Trivy 0.69.3 `image --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --exit-code 1 kafka-streams-demo:local`.

Result: scan container exited 0. No `ignore-unfixed` or suppression was used. The parent shell timeout did not terminate the container; `docker wait` captured the final zero exit. Date: 2026-07-30.

## SBOM

Command: digest-pinned Trivy 0.69.3 image scan with `--format spdx-json`.

Result: SPDX 2.3, 158 packages, SHA-256 `5653a530a575798d907aa42d9bdc2fc889d9b8f00167014271aa89f68665496f`. Twenty-five upstream package license fields are `NOASSERTION` and are recorded in `evidence/SBOM_REVIEW.md`. Exit code 0. Date: 2026-07-30.

## Final structural and document gates

Commands: `tools/validate-project.ps1`, `tools/validate-gradle-project.ps1 -SkipBuild`, clean benchmark validation, `tools/scan-secrets.ps1`, `docker compose -f docker-compose.real.yml config --quiet`, Python JSON/YAML parsing, README section count, stale-version/placeholder/mojibake/domain-leak/scan-suppression/trailing-whitespace searches, and `git diff --check`.

Result: all passed. Secret scan covered 182 candidate files; 27 JSON and 40 YAML files parsed; README has exactly 19 numbered sections; forbidden searches and diff check returned no finding. The first full validator attempt exposed three renamed mandatory reuse-gate clauses; the clauses were restored and the complete validator then passed. Date: 2026-07-30.

Remote GitHub Actions evidence remains pending.
## Remote CI convergence

PR: https://github.com/Brilhante29/kafka-streams-demo/pull/1, draft, never merged automatically.

- Run `30555827379` on `2d6a1a5`: JVM/integration and benchmark evidence passed; repository contracts failed because `$isWindows` collided with the read-only PowerShell 7 `$IsWindows` constant.
- Run `30556208061` on `43a2bc0`: repository contracts passed; fallback secret scan failed because provider-based `Get-Item` could not inspect Linux dotfiles.
- Run `30556641635` on `444c784`: quality job passed; broker smoke failed because UID 10001 could not create the output file in a runner-owned bind directory.
- Run `30557441814` on `7e906b0`: JVM/contracts/tests and broker smoke/image security/SBOM jobs all passed. Trivy filesystem/image scans and non-root runtime checks passed; SBOM upload succeeded.
- Artifact: `kafka-streams-demo-sbom`, ID `8765516776`, archive digest `sha256:0b9b46f1f60c6b46f3a0eba981af066da7f5bece5a93107facb8f21bec68be0f`, expires 2026-08-13.

Dependency review was skipped in manual dispatches by workflow design. A conflict-free `main` was created at audited ancestor `ab9535a` and PR #1 was retargeted without merging; the next synchronization commit must prove the actual `pull_request` event.
