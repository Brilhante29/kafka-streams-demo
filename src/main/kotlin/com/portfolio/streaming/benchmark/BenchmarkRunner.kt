package com.portfolio.streaming.benchmark

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.CustomerSummary
import com.portfolio.streaming.domain.EnrichedPurchase
import com.portfolio.streaming.domain.PurchaseEvent
import com.portfolio.streaming.infra.JsonSerde
import com.portfolio.streaming.infra.TopologyFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createDirectories
import kotlin.math.ceil

@Serializable
data class BenchmarkReport(
    val project: String,
    val metric: String,
    val value: Double,
    val unit: String,
    val timestamp: String,
    val command: String,
    val repeat: Int,
    val samples: List<Double>,
    val summary: Map<String, Double>,
    val environment: Map<String, String>,
)

object BenchmarkRunner {
    private const val DEFAULT_RECORDS = 1_000
    private const val SEED = 42

    fun run(
        recordCount: Int = DEFAULT_RECORDS,
        outputPath: Path,
    ): BenchmarkReport {
        require(recordCount > 0) { "recordCount must be positive" }
        val topology = TopologyFactory.build()
        val purchaseSerde = JsonSerde(PurchaseEvent.serializer())
        val profileSerde = JsonSerde(CustomerProfile.serializer())
        val enrichedSerde = JsonSerde(EnrichedPurchase.serializer())
        val summarySerde = JsonSerde(CustomerSummary.serializer())
        val driver = TopologyTestDriver(topology, TopologyFactory.properties("benchmark-$SEED"))

        return try {
            val profileInput =
                driver.createInputTopic(
                    TopologyFactory.PROFILES_TOPIC,
                    Serdes.String().serializer(),
                    profileSerde.serializer(),
                )
            val purchaseInput =
                driver.createInputTopic(
                    TopologyFactory.PURCHASES_TOPIC,
                    Serdes.String().serializer(),
                    purchaseSerde.serializer(),
                )
            val enrichedOutput =
                driver.createOutputTopic(
                    TopologyFactory.ENRICHED_TOPIC,
                    Serdes.String().deserializer(),
                    enrichedSerde.deserializer(),
                )
            val summaryOutput =
                driver.createOutputTopic(
                    TopologyFactory.SUMMARY_TOPIC,
                    Serdes.String().deserializer(),
                    summarySerde.deserializer(),
                )

            Fixtures.profiles().forEach { profile ->
                profileInput.pipeInput(profile.customerId, profile)
            }

            val timings = LongArray(recordCount)
            val batchStart = System.nanoTime()
            repeat(recordCount) { index ->
                val event = Fixtures.event(index)
                val start = System.nanoTime()
                purchaseInput.pipeInput(event.customerId, event)
                timings[index] = System.nanoTime() - start
            }
            val batchElapsedNanos = System.nanoTime() - batchStart

            var enrichedCount = 0
            while (!enrichedOutput.isEmpty) {
                enrichedOutput.readKeyValue()
                enrichedCount++
            }
            var summaryCount = 0
            while (!summaryOutput.isEmpty) {
                summaryOutput.readKeyValue()
                summaryCount++
            }

            val messagesPerSecond = recordCount / (batchElapsedNanos / 1_000_000_000.0)
            val latencyMs = timings.map { it / 1_000_000.0 }
            val sortedLatencies = latencyMs.sorted()
            val averageLatencyMs = latencyMs.average()
            val p95LatencyMs = percentile(sortedLatencies, 0.95)
            val p99LatencyMs = percentile(sortedLatencies, 0.99)

            val report =
                BenchmarkReport(
                    project = "28-kafka-streams-demo",
                    metric = "messages_per_second",
                    value = messagesPerSecond,
                    unit = "messages/s",
                    timestamp = Instant.now().toString(),
                    command = System.getProperty("sun.java.command", "benchmark $recordCount"),
                    repeat = 1,
                    samples = listOf(messagesPerSecond),
                    summary =
                        mapOf(
                            "record_count" to recordCount.toDouble(),
                            "seed" to SEED.toDouble(),
                            "enriched_output_records" to enrichedCount.toDouble(),
                            "summary_output_records" to summaryCount.toDouble(),
                            "batch_elapsed_ms" to batchElapsedNanos / 1_000_000.0,
                            "topology_latency_avg_ms" to averageLatencyMs,
                            "topology_latency_p95_ms" to p95LatencyMs,
                            "topology_latency_p99_ms" to p99LatencyMs,
                        ),
                    environment = environment(recordCount),
                )
            write(report, outputPath)
            report
        } finally {
            driver.close()
        }
    }

    private fun percentile(
        sortedValues: List<Double>,
        quantile: Double,
    ): Double {
        val index = ceil(quantile * sortedValues.size).toInt().coerceAtLeast(1) - 1
        return sortedValues[index.coerceAtMost(sortedValues.lastIndex)]
    }

    private fun environment(recordCount: Int): Map<String, String> =
        mapOf(
            "mode" to "topology-test-driver",
            "java_version" to System.getProperty("java.version", "unknown"),
            "kotlin_version" to KotlinVersion.CURRENT.toString(),
            "os" to System.getProperty("os.name", "unknown"),
            "os_arch" to System.getProperty("os.arch", "unknown"),
            "processors" to Runtime.getRuntime().availableProcessors().toString(),
            "seed" to SEED.toString(),
            "records" to recordCount.toString(),
            "broker" to "none",
        )

    private fun write(
        report: BenchmarkReport,
        outputPath: Path,
    ) {
        outputPath.parent?.createDirectories()
        val json = Json { prettyPrint = true }
        Files.writeString(outputPath, json.encodeToString(report) + System.lineSeparator())
    }
}

object Fixtures {
    private const val BASE_TIMESTAMP = 1_700_000_000_000L

    fun profiles(): List<CustomerProfile> =
        listOf(
            CustomerProfile("customer-000", "premium", "BR", 0.03),
            CustomerProfile("customer-001", "standard", "BR", 0.18),
            CustomerProfile("customer-002", "enterprise", "US", 0.04),
        )

    fun event(index: Int): PurchaseEvent {
        val customer = index % profiles().size
        return PurchaseEvent(
            eventId = "event-${index.toString().padStart(6, '0')}",
            customerId = "customer-${customer.toString().padStart(3, '0')}",
            sku = "sku-${(index % 17).toString().padStart(2, '0')}",
            quantity = 1 + (index % 4),
            amountCents = 1_000L + ((index * 137) % 10_000),
            occurredAtEpochMs = BASE_TIMESTAMP + index,
        )
    }
}
