# Inventory: #28 kafka-streams-demo

## Identity

- Program: `mlops-data-platform`.
- Status: benchmarked; local release evidence complete, publication pending.
- Claim: stateful stream-table enrichment and per-customer aggregation.
- Primary metric: `end_to_end_input_records_per_second`.
- Public result: 4,965.35 records/s median; 267.48 ms batch p95; invariant 1.0.

## Evidence map

| Evidence | Location | State |
|---|---|---|
| Manifest V2 | `project.yaml` | schema passed; evidence current |
| Reuse contract | `reuse.manifest.yaml` | current |
| SDD and requirements | `sdd/spec.md` | current |
| ADRs | `sdd/architecture-decision.md`, `sdd/technical-decision.md` | accepted |
| Benchmark plan | `sdd/benchmark-plan.md` | current |
| Release baseline | `benchmarks/results/baseline.json` | clean, five samples, semantic/digest validation passed |
| Independent review | `.portfolio-control/INDEPENDENT_REVIEW.md` | four technical P1 resolved; publication P1 pending CI |
| SPDX SBOM | `.portfolio-control/evidence/sbom.spdx.json` | 158 packages; retained and reviewed |
| SBOM review | `.portfolio-control/evidence/SBOM_REVIEW.md` | 25 `NOASSERTION` entries recorded |
| OpenSpec graph | `openspec/artifacts/` | aligned to release evidence |
| CI | `.github/workflows/ci.yml` | configured; remote result pending |
| Handoff | `.portfolio-control/CURRENT_HANDOFF.md` | current |
