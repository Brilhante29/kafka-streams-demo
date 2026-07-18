# Benchmark Plan: kafka-streams-demo

## Hypothesis

Uma topologia Kafka Streams pequena pode enriquecer e agregar um lote fixo sem
broker, produzindo uma baseline comparÃ¡vel de `messages_per_second` e latÃªncia.

## Command

```bash
gradle run --args="benchmark 1000 benchmarks/results/latest.json"
```

Docker equivalente:

```bash
docker run --rm -v "${PWD}/benchmarks/results:/app/benchmarks/results" kafka-streams-demo benchmark 1000 /app/benchmarks/results/latest.json
```

## Environment

O relatÃ³rio captura timestamp, Java, Kotlin, SO, arquitetura, processadores,
seed, quantidade e `mode=topology-test-driver`. A execuÃ§Ã£o nÃ£o exige GPU,
broker, Kumo, AWS ou segredo. O resultado committed deve ser produzido no
ambiente real de execuÃ§Ã£o, nÃ£o preenchido Ã  mÃ£o.

## Inputs

- Fixture: `Fixtures` no cÃ³digo.
- Lote padrÃ£o: 1000 eventos.
- RepetiÃ§Ãµes: 1 por comando; repetiÃ§Ãµes externas podem comparar `samples`.
- Warmup: nÃ£o hÃ¡ warmup oculto; a primeira execuÃ§Ã£o deve ser reportada como tal.
- Seed: 42; trÃªs perfis; `customerId` distribuÃ­do deterministicamente.

## Metrics

| Metric | Unit | Source | Why it matters |
|---|---:|---|---|
| messages_per_second | messages/s | elapsed fixed batch | prova throughput da topologia local |
| topology_latency_avg_ms | ms | each synchronous `pipeInput` | custo mÃ©dio por evento |
| topology_latency_p95_ms | ms | each synchronous `pipeInput` | cauda de latÃªncia do processamento |
| topology_latency_p99_ms | ms | each synchronous `pipeInput` | outliers do processamento |
| enriched_output_records | records | output topic drain | confirma join completo |
| summary_output_records | records | output topic drain | confirma atualizaÃ§Ã£o incremental |

## Result schema

O JSON segue `.portfolio/contracts/benchmark-result.schema.json` e inclui
`project`, `metric`, `value`, `unit`, `timestamp`, `command`, `repeat`,
`samples`, `summary` e `environment`.

## Interpretation

Este baseline nÃ£o Ã© throughput de Redpanda/Kafka. Ele isola a topologia para
feedback rÃ¡pido. A prÃ³xima mediÃ§Ã£o, se necessÃ¡ria, deve executar o comando
`run` em Redpanda e reportar separadamente broker/network/consumer lag.
