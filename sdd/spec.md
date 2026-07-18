# Spec: kafka-streams-demo

## Number

#28

## Claim

Este projeto prova processamento streaming local-first: uma compra Ã© enriquecida
com o perfil vigente e acumulada em um resumo por cliente.

## Stack

Kotlin 2.0.21, Java 21, Gradle Kotlin DSL, Apache Kafka Streams 3.8.1, JUnit 5,
TopologyTestDriver, Ktlint e Docker. Redpanda Ã© apenas o runtime opcional de
integraÃ§Ã£o em `docker-compose.real.yml`.

## Scope

In:

- stream/table join de `purchases` com `customer-profiles`;
- saÃ­da enriquecida e agregaÃ§Ã£o incremental por cliente;
- testes determinÃ­sticos sem broker;
- benchmark de lote fixo com JSON e ambiente;
- Docker padrÃ£o sem segredo e adaptador de runtime Kafka real;
- CI com testes, lint, benchmark e build Docker.

Out:

- prometer throughput de broker, rede ou cluster a partir do benchmark local;
- cloud/AWS/Kumo no caminho padrÃ£o, pois nÃ£o existe dependÃªncia cloud no domÃ­nio;
- autenticaÃ§Ã£o, DLQ e compatibilidade total de produÃ§Ã£o sem um ambiente real;
- microserviÃ§os, banco, outbox e schema registry sem evidÃªncia de necessidade.

## Runtime padrÃ£o sem broker

O comando `demo` Ã© uma demonstraÃ§Ã£o textual e `benchmark` constrÃ³i a topologia e
usa `TopologyTestDriver`. Esse driver processa registros sincronicamente e nÃ£o
precisa de broker, portanto o caminho padrÃ£o Ã© rÃ¡pido, determinÃ­stico e gratuito.
O mesmo `TopologyFactory` Ã© entregue ao `KafkaStreams` no comando `run`; esse Ã© o
adaptador explÃ­cito que conecta `KAFKA_BOOTSTRAP_SERVERS` a uma infraestrutura
Kafka/Redpanda real. A troca Ã© operacional, nÃ£o uma dependÃªncia do domÃ­nio.

## Contrato

- `PurchaseEvent`: `eventId`, `customerId`, `sku`, `quantity`, `amountCents`, `occurredAtEpochMs`.
- `CustomerProfile`: `customerId`, `segment`, `country`, `riskScore`.
- `EnrichedPurchase`: fatos da compra mais `segment` e `country`.
- `CustomerSummary`: `totalOrders`, `totalUnits`, `totalAmountCents` por cliente.

## Benchmark

- mÃ©trica primÃ¡ria: `messages_per_second`;
- unidade: `messages/s`;
- lote: `1000` eventos por padrÃ£o;
- seed: `42`;
- comando: `gradle run --args="benchmark 1000 benchmarks/results/latest.json"`;
- resultado de referÃªncia: `benchmarks/results/baseline.json`;
- mÃ©tricas adicionais: duraÃ§Ã£o do lote e latÃªncia mÃ©dia/p95/p99 por `pipeInput`.

## Definition of done

- [x] Gradle, Kotlin, topologia, Docker e CI versionados.
- [x] Domain behavior e topologia cobertos por testes determinÃ­sticos.
- [x] Caminho default nÃ£o exige broker, cloud ou segredo pago.
- [x] Adaptador `run` e compose opcional documentam infraestrutura real.
- [x] README, SDD, referÃªncias e decisÃ£o de reÃºso nÃ£o tÃªm placeholders.
- [ ] MediÃ§Ã£o numÃ©rica local atualizada apÃ³s uma execuÃ§Ã£o com Java/Docker disponÃ­vel.
