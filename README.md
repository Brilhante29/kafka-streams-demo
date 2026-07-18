# #28 kafka-streams-demo

**Claim:** processamento streaming com enriquecimento por tabela e agregaÃ§Ã£o por cliente.

**Benchmark atual:** `10.2940 messages/s`, `97.1087 ms` p95, lote de `1000` eventos, seed `42`. O JSON versionado registra ambiente e comando.

## 1. O que roda

Uma topologia Kafka Streams recebe `purchases`, consulta o Ãºltimo `customer-profile`, publica `enriched-purchases` e agrega `customer-summaries` por `customerId`.

O caminho padrÃ£o Ã© local-first e broker-free:

```text
purchase stream -> KTable join -> enriched purchase -> customer aggregate
```

`TopologyTestDriver` processa os registros de forma sÃ­ncrona sem broker real. Isso torna testes e benchmark determinÃ­sticos e evita que o setup de infraestrutura esconda a lÃ³gica da topologia.

## 2. Stack e decisÃµes

- Kotlin 2.0.21, Java 21 e Gradle Kotlin DSL.
- Apache Kafka Streams 3.8.1 para DSL de stream/table join e state store.
- JUnit 5, AssertJ e `TopologyTestDriver` para testes determinÃ­sticos.
- Ktlint como gate de lint.
- Docker multistage com Gradle e JRE 21.
- Redpanda Ã© somente o adaptador opcional de runtime real em `docker-compose.real.yml`.
- NÃ£o hÃ¡ banco nem serviÃ§o cloud no domÃ­nio. Kumo nÃ£o entra no caminho padrÃ£o porque esta topologia nÃ£o chama AWS; uma futura porta de object storage, secrets ou event bus pode usar Kumo local e AWS atrÃ¡s de adaptadores equivalentes.

## 3. ExecuÃ§Ã£o

Com Java 21 e Gradle 8.10+:

```bash
gradle test
gradle run --args="demo"
gradle run --args="benchmark 1000 benchmarks/results/latest.json"
```

Com Docker:

```bash
docker build -t kafka-streams-demo .
docker run --rm kafka-streams-demo demo
docker run --rm -v "${PWD}/benchmarks/results:/app/benchmarks/results" kafka-streams-demo benchmark 1000 /app/benchmarks/results/latest.json
```

O primeiro comando Docker executa `clean check installDist` durante o build. O segundo usa `TopologyTestDriver`; nÃ£o requer Kafka, Redpanda, segredo ou conta paga.

Para conectar infraestrutura Kafka real:

```bash
docker compose -f docker-compose.real.yml up --build
```

Ou execute a aplicaÃ§Ã£o com `run` e defina `KAFKA_BOOTSTRAP_SERVERS`. Esse adaptador mantÃ©m o domÃ­nio independente do broker; tÃ³picos, autenticaÃ§Ã£o TLS/SASL e observabilidade de produÃ§Ã£o ficam na configuraÃ§Ã£o da implantaÃ§Ã£o.

## 4. Testes e validaÃ§Ã£o estrita

```bash
gradle clean check
powershell -File tools/validate-project.ps1 -SkipDocker
powershell -File tools/validate-gradle.ps1
```

Os testes cobrem a polÃ­tica pura de enriquecimento, o join com perfil, a agregaÃ§Ã£o incremental e o caso de perfil desconhecido. O CI repete testes, lint, benchmark JSON e `docker build`.

## 5. Benchmark reproduzÃ­vel

O lote padrÃ£o Ã© de `1000` eventos com seed `42`, trÃªs perfis e timestamps determinÃ­sticos. O resultado inclui:

- `messages_per_second` como mÃ©trica primÃ¡ria;
- latÃªncia sÃ­ncrona mÃ©dia, p95 e p99 da topologia;
- contagem de saÃ­das enriquecidas e agregadas;
- timestamp, comando, JVM, SO, arquitetura, processadores, seed e modo `topology-test-driver`.

O arquivo de referÃªncia Ã© `benchmarks/results/baseline.json`. Como throughput e latÃªncia dependem de CPU/JVM, o nÃºmero sÃ³ Ã© atualizado por uma execuÃ§Ã£o real e nunca Ã© estimado por documentaÃ§Ã£o.

## 6. Estrutura

```text
src/main/kotlin/com/portfolio/streaming/
  domain/       eventos e polÃ­tica de negÃ³cio pura
  infra/        serde, topologia e configuraÃ§Ã£o Kafka
  benchmark/    fixture fixa e relatÃ³rio JSON
src/test/kotlin/com/portfolio/streaming/
benchmarks/results/
docs/topology.md
sdd/
```

O domÃ­nio nÃ£o depende de Kafka, Docker, cloud, transporte ou banco. As funÃ§Ãµes da topologia dependem de ports implÃ­citos do Kafka Streams somente na borda `infra`; o teste injeta o runtime determinÃ­stico do driver. A divisÃ£o mantÃ©m SRP, DIP, ISP e LSP sem criar microserviÃ§os, outbox ou cloud artificialmente: KISS/YAGNI sÃ£o parte da decisÃ£o.

## 7. SDD e referÃªncias

- [EspecificaÃ§Ã£o](sdd/spec.md)
- [DecisÃ£o de arquitetura](sdd/architecture-decision.md)
- [DecisÃ£o tÃ©cnica](sdd/technical-decision.md)
- [Plano de benchmark](sdd/benchmark-plan.md)
- [Topologia](docs/topology.md)
- [ReferÃªncias](REFERENCES.md)
