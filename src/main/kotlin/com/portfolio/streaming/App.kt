package com.portfolio.streaming

import com.portfolio.streaming.benchmark.BenchmarkRunner
import com.portfolio.streaming.benchmark.Fixtures
import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.streams.KafkaStreams
import java.nio.file.Path

fun main(args: Array<String>) {
    when (args.firstOrNull()?.lowercase()) {
        "benchmark" -> runBenchmark(args.drop(1))
        "run" -> runWithKafka()
        "demo", null -> runDemo()
        else -> error("Usage: demo | benchmark [records] [output.json] | run")
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

private fun runBenchmark(arguments: List<String>) {
    val recordCount = arguments.getOrNull(0)?.toIntOrNull() ?: 1_000
    val output = Path.of(arguments.getOrNull(1) ?: "benchmarks/results/latest.json")
    val report = BenchmarkRunner.run(recordCount, output)
    println("${report.metric}=${"%.2f".format(java.util.Locale.ROOT, report.value)} ${report.unit}")
    println(
        "topology_latency_p95_ms=${"%.4f".format(
            java.util.Locale.ROOT,
            report.summary.getValue("topology_latency_p95_ms"),
        )}",
    )
    println("result=$output")
}

private fun runWithKafka() {
    val bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
    val properties =
        TopologyFactory.properties(
            applicationId = System.getenv("KAFKA_APPLICATION_ID") ?: "kafka-streams-demo",
            bootstrapServers = bootstrapServers,
        )
    val streams = KafkaStreams(TopologyFactory.build(), properties)
    Runtime.getRuntime().addShutdownHook(Thread { streams.close() })
    streams.start()
    println("Kafka Streams started with bootstrap.servers=$bootstrapServers")
}
