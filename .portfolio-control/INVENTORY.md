# Inventory: #28 kafka-streams-demo

## Identity

- Program: `mlops-data-platform`.
- Status: implementation; release candidate not yet executed.
- Claim: stateful stream-table enrichment and per-customer aggregation.
- Primary metric: `end_to_end_input_records_per_second`.

## Evidence map

| Evidence | Location | State |
|---|---|---|
| Manifest V2 | `project.yaml` | schema passed |
| Reuse contract | `reuse.manifest.yaml` | current |
| SDD and requirements | `sdd/spec.md` | current |
| ADRs | `sdd/architecture-decision.md`, `sdd/technical-decision.md` | accepted |
| Benchmark plan | `sdd/benchmark-plan.md` | current |
| Broker smoke | ignored `benchmarks/results/broker-smoke.json` | passed, dirty development evidence |
| Release baseline | `benchmarks/results/baseline.json` | pending clean run |
| OpenSpec graph | `openspec/artifacts/` | current |
| CI | `.github/workflows/ci.yml` | being hardened; remote result pending |
| Security/SBOM | CI and release controls | pending full RC |
| Handoff | `.portfolio-control/CURRENT_HANDOFF.md` | current |
