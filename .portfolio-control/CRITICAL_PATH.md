# Critical Path: #28 kafka-streams-demo

```text
local release evidence complete
  -> final structural/document validation
  -> evidence/docs commit
  -> verify remote + push branch
  -> open PR + observed green CI
  -> resolve publication-state review finding
  -> explicit merge decision
  -> next repository
```

Stop rules:

- Do not regenerate the baseline, image, or SBOM unless source/runtime inputs change.
- After two equivalent failures, change strategy and record the cause.
- P2/P3 findings enter the measured backlog; only P0/P1 block publication.
- Exact weekly balance is not exposed to repository code. At a product quota warning, finish the smallest safe unit, capture continuity, and stop heavy work.
- Never merge without explicit authorization.
