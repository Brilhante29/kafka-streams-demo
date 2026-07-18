# Reuse Improvement Review

Project: `28 - kafka-streams-demo`

## Review Points

- [x] after scaffold
- [x] after architecture decision
- [x] after first working slice
- [x] after benchmark harness
- [x] before publication

## Findings

| Finding | Classification | Kit Area | Action | Status |
|---|---|---|---|---|
| O kit jÃ¡ tinha schema de benchmark e gates de README/SDD; foram usados sem copiar cÃ³digo do projeto externo. | reject | contracts/harness | manter o contrato local e registrar o resultado especÃ­fico do projeto | resolved |
| O caminho broker-free precisava ser explicitado para nÃ£o parecer que Kafka foi abandonado. | patch_now | docs/sdd | documentar `TopologyTestDriver` como default e `KafkaStreams` como adapter real | resolved |
| O validator existente nÃ£o verifica Gradle/Kotlin. | backlog | validation | adicionar uma regra de linguagem JVM quando houver mais projetos Kotlin no kit | recorded |

## Patch Now Decisions

- Nenhuma mudanÃ§a foi feita em `.portfolio` ou `.portfolio-control`; o ganho Ã©
  especÃ­fico deste repositÃ³rio e estÃ¡ documentado nos SDDs.
- O benchmark JSON mantÃ©m o schema compartilhado e adiciona latÃªncias de
  topologia como campos numÃ©ricos em `summary`.

## Backlog Decisions

- Criar no kit um gate opcional para `gradle check`, wrapper e validaÃ§Ã£o de
  toolchain, evitando que o validator assuma apenas Python.
- Padronizar um template de topologia/benchmark broker-free para projetos de
  streaming futuros.

## Rejected Improvements

- NÃ£o copiar implementaÃ§Ã£o de `kafka-streams-examples`: a referÃªncia orienta
  conceitos; contratos, fixtures e topologia sÃ£o especÃ­ficos deste projeto.
- NÃ£o adicionar Kumo, AWS SDK ou broker ao kit sÃ³ para preencher o campo cloud;
  isso aumentaria custo sem um comportamento cloud no claim.

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects the deterministic broker-free benchmark and Gradle gate.
