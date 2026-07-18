# Architecture Decision

## Status

Accepted

## Context

Projeto `#28 kafka-streams-demo`. O problema Ã© um pipeline de eventos com
enriquecimento por tabela e agregaÃ§Ã£o. A mÃ©trica Ã© `messages_per_second`, com
latÃªncia sÃ­ncrona da topologia em lote fixo.

ForÃ§as: domÃ­nio baixo/mÃ©dio, integraÃ§Ã£o alta, estado de UI inexistente,
reprodutibilidade alta, auditabilidade mÃ©dia, throughput/assÃ­ncrono alto e
deploy independente baixo.

## Decision

Arquitetura escolhida: **event-driven com borda hexagonal mÃ­nima**.

`domain` contÃ©m eventos e polÃ­tica pura. `infra` contÃ©m Kafka Streams, Serdes e
configuraÃ§Ã£o. `benchmark` e testes sÃ£o adaptadores de entrada que usam o mesmo
factory de topologia. A regra Ã© inward-only: domÃ­nio nÃ£o depende de broker,
framework, cloud, persistÃªncia ou transporte.

## Why

Kafka Streams Ã© a prÃ³pria unidade de processamento a provar; esconder a
topologia atrÃ¡s de uma camada de serviÃ§os criaria cÃ³digo sem aumentar evidÃªncia.
O `TopologyTestDriver` permite testar join, filtragem e agregaÃ§Ã£o sem broker. O
comando `run` preserva um caminho de produÃ§Ã£o real sem forÃ§ar esse custo no
runtime padrÃ£o.

## Rejected alternatives

| Alternative | Why rejected |
|---|---|
| Microservices | NÃ£o hÃ¡ fronteira de deploy independente que justifique a complexidade. |
| Spring Boot | O claim Ã© a topologia Kafka Streams; adicionar HTTP e contexto Spring desviaria o benchmark. |
| Broker obrigatÃ³rio no default | Mediria setup/IO e quebraria o caminho determinÃ­stico pedido. |
| CQRS/event sourcing completo | O resumo materializado Ã© suficiente; retenÃ§Ã£o, replay e auditoria completa estÃ£o fora do escopo. |

## Folder layout

```text
src/main/kotlin/com/portfolio/streaming/
  domain/       contratos e regra pura
  infra/        Kafka Streams, Serde e configuraÃ§Ã£o
  benchmark/    fixture e JSON de mediÃ§Ã£o
src/test/kotlin/com/portfolio/streaming/
benchmarks/results/
docs/
sdd/
```

## Testing strategy

- Unit: `EnrichmentPolicy` e `CustomerSummary` sem Kafka.
- Topology: `TopologyTestDriver` cobre join, aggregate e perfil ausente.
- Validation: Ktlint, Gradle `check`, JSON vÃ¡lido, Docker build e CI.
- Benchmark: lote determinÃ­stico que confirma contagem enriquecida e agregada.

## Consequences

Positivas: execuÃ§Ã£o rÃ¡pida, sem credencial, topologia visÃ­vel, teste determinÃ­stico
e migraÃ§Ã£o clara para Kafka real.

Tradeoffs: benchmark nÃ£o inclui latÃªncia de rede, broker, serializaÃ§Ã£o externa,
rebalanceamento ou persistÃªncia distribuÃ­da; isso deve ser medido separadamente
antes de qualquer alegaÃ§Ã£o de produÃ§Ã£o.

MigraÃ§Ã£o: configurar `KAFKA_BOOTSTRAP_SERVERS`, seguranÃ§a e tÃ³picos no adapter
`run`; manter os contratos e adicionar testes de contrato contra a implantaÃ§Ã£o.
