package com.portfolio.streaming

import com.portfolio.streaming.benchmark.BenchmarkReportV2
import com.portfolio.streaming.benchmark.Fixtures
import com.portfolio.streaming.benchmark.RealBrokerBenchmarkRunner
import com.portfolio.streaming.benchmark.TopologyBenchmarkRunner
import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.streams.KafkaStreams
import java.nio.file.Path

fun main(args: Array<String>) {
    when (args.firstOrNull()?.lowercase()) {
        "topology-benchmark", "benchmark" -> runTopologyBenchmark(args.drop(1))
        "broker-benchmark" -> runBrokerBenchmark(args.drop(1))
        "run" -> runWithKafka()
        "demo", null -> runDemo()
        else ->
            error(
                "Usage: demo | topology-benchmark [records] [warmups] [repeats] [output.json] | " +
                    "broker-benchmark [records] [warmups] [repeats] [output.json] | run",
            )
    }
}

private fun runDemo() {
    val first = Fixtures.event(0)
    val second = Fixtures.event(3)
    println("local-first demo input: ${first.eventId}, ${second.eventId}")
    println("topology: ${TopologyFactory.PURCHASES_TOPIC} -> join(profile) -> ${TopologyFactory.ENRICHED_TOPIC}")
    println("aggregation: ${TopologyFactory.ENRICHED_TOPIC} -> ${TopologyFactory.SUMMARY_TOPIC}")
    println("broker: none (TopologyTestDriver path)")
}

private fun runTopologyBenchmark(arguments: List<String>) {
    val recordCount = arguments.getOrNull(0)?.toIntOrNull() ?: 1_000
    val warmups = arguments.getOrNull(1)?.toIntOrNull() ?: 1
    val repeats = arguments.getOrNull(2)?.toIntOrNull() ?: 5
    val output = Path.of(arguments.getOrNull(3) ?: "benchmarks/results/topology-latest.json")
    val report = TopologyBenchmarkRunner.run(recordCount, warmups, repeats, output)
    printPrimaryMetric(report)
    println("result=$output")
}

private fun runBrokerBenchmark(arguments: List<String>) {
    val recordCount = arguments.getOrNull(0)?.toIntOrNull() ?: 1_000
    val warmups = arguments.getOrNull(1)?.toIntOrNull() ?: 1
    val repeats = arguments.getOrNull(2)?.toIntOrNull() ?: 5
    val output = Path.of(arguments.getOrNull(3) ?: "benchmarks/results/broker-latest.json")
    val bootstrapServers =
        requireNotNull(System.getenv("KAFKA_BOOTSTRAP_SERVERS")) {
            "KAFKA_BOOTSTRAP_SERVERS is required for broker-benchmark"
        }
    val report = RealBrokerBenchmarkRunner.run(bootstrapServers, recordCount, warmups, repeats, output)
    printPrimaryMetric(report)
    println("result=$output")
}

private fun printPrimaryMetric(report: BenchmarkReportV2) {
    val metric = report.metrics.first()
    println("${metric.name}=${"%.2f".format(java.util.Locale.ROOT, metric.value)} ${metric.unit}")
}

private fun runWithKafka() {
    val bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
    val properties =
        TopologyFactory.properties(
            applicationId = System.getenv("KAFKA_APPLICATION_ID") ?: "kafka-streams-demo",
            bootstrapServers = bootstrapServers,
            realBroker = true,
        )
    val streams = KafkaStreams(TopologyFactory.build(), properties)
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
    println("Kafka Streams started with bootstrap.servers=$bootstrapServers")
}
