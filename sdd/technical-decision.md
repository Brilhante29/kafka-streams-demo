# Technical Decision

## Status

Accepted

## Selected options

- Stack: Kotlin 2.0.21 + Java 21 + Gradle Kotlin DSL.
- API style: CLI (`demo`, `benchmark`, `run`), porque nÃ£o hÃ¡ API HTTP no claim.
- Messaging: Kafka Streams DSL; stream/table join e state store materializada.
- Cloud: `adapter-fake`/none in scope. Kumo fica documentado como provider local
  futuro para portas AWS; nÃ£o hÃ¡ chamada AWS que justifique rodÃ¡-lo.
- Runtime/database: JVM local ou imagem JRE 21; nenhum banco.
- Libraries: Kafka Streams/test-utils, Kotlin serialization, JUnit 5, AssertJ e
  Ktlint; cada uma expÃµe o conceito medido e mantÃ©m o Docker pequeno.

## Messaging decision

Kafka Ã© justificado por stream processing, estado agregado e possibilidade de
replay/consumer groups no adaptador real. O default usa `TopologyTestDriver`,
nÃ£o Redpanda, pois o broker nÃ£o contribui para a prova local de join/agregaÃ§Ã£o.

Delivery semantics no default: processamento sÃ­ncrono do driver. No runtime
real, Kafka Streams oferece os semÃ¢nticos configurados pelo cluster; exatamente-
uma-vez, retries, retenÃ§Ã£o, partiÃ§Ãµes e DLQ exigem uma decisÃ£o operacional futura.
Particionamento deve usar `customerId` como chave para preservar a ordem por
cliente. `customer-profiles` deve ser uma KTable/compactada na implantaÃ§Ã£o real.

## SOLID, KISS and testability

- SRP: eventos/polÃ­tica, topologia, Serde, benchmark e CLI tÃªm razÃµes de mudanÃ§a
  separadas.
- OCP/LSP: `TopologyTestDriver` e `KafkaStreams` consomem o mesmo `Topology`; a
  polÃ­tica pura nÃ£o conhece as implementaÃ§Ãµes externas.
- ISP/DIP: a regra de enriquecimento recebe dados concretos do contrato e o
  acesso ao broker fica na borda de `TopologyFactory`/`run`.
- DRY: fixtures, nomes de tÃ³picos e Serdes sÃ£o compartilhados por teste e
  benchmark; nÃ£o hÃ¡ abstraÃ§Ã£o genÃ©rica para futuros brokers.
- KISS/YAGNI: sem Spring, banco, microserviÃ§os, registry ou Kumo obrigatÃ³rio.
- Law of Demeter: agregador manipula apenas o evento e seu estado direto.

## Cloud/Kumo adapter

NÃ£o hÃ¡ serviÃ§o cloud no escopo e, portanto, Kumo nÃ£o Ã© uma dependÃªncia teatral.
Quando o pipeline precisar armazenar payloads, emitir notificaÃ§Ãµes ou guardar
segredos, a aplicaÃ§Ã£o deverÃ¡ adicionar uma porta pequena e implementar Kumo
local/AWS externamente, selecionando por configuraÃ§Ã£o. O domÃ­nio nÃ£o importa
SDK AWS nem endpoint Kumo.

## Rejected options

| Option | Reason |
|---|---|
| Redpanda obrigatÃ³rio | Custo operacional e nÃ£o-determinismo no caminho padrÃ£o. |
| RabbitMQ | SemÃ¢ntica de fila/ack nÃ£o prova stream/table join ou agregaÃ§Ã£o replayable. |
| Kinesis | NÃ£o Ã© necessÃ¡rio e dificultaria o laboratÃ³rio local. |
| Spring WebFlux/REST | NÃ£o existe endpoint e o benchmark Ã© de topologia, nÃ£o HTTP. |

## Benchmark impact

O desenho elimina broker/network overhead do baseline e mede exatamente o custo
de processar um lote fixo dentro do driver. O JSON separa throughput do custo
por registro (`topology_latency_*`) e registra ambiente para comparaÃ§Ã£o.

Validation:

```powershell
gradle --no-daemon clean check
gradle --no-daemon run --args="benchmark 1000 benchmarks/results/latest.json"
powershell -File tools/validate-project.ps1 -SkipDocker
```
