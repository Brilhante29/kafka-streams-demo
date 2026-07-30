package com.portfolio.streaming.benchmark

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.PurchaseEvent
import com.portfolio.streaming.infra.DomainJsonSerdes
import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import java.nio.file.Path
import java.time.Instant

private data class TopologyMeasurement(
    val recordsPerSecond: Double,
    val elapsedMs: Double,
    val latenciesMs: List<Double>,
    val enrichedOutputRecords: Int,
    val summaryOutputRecords: Int,
)

object TopologyBenchmarkRunner {
    private const val DEFAULT_RECORDS = 1_000
    private const val SEED = 42

    fun run(
        recordCount: Int = DEFAULT_RECORDS,
        warmupIterations: Int = 1,
        measuredIterations: Int = 5,
        outputPath: Path,
    ): BenchmarkReportV2 {
        require(recordCount > 0) { "recordCount must be positive" }
        require(warmupIterations >= 0) { "warmupIterations must not be negative" }
        require(measuredIterations > 0) { "measuredIterations must be positive" }

        val startedAt = Instant.now()
        val executionStart = System.nanoTime()

        repeat(warmupIterations) { iteration ->
            measure(recordCount, "warmup-$iteration")
        }

        val measurements =
            List(measuredIterations) { iteration ->
                measure(recordCount, "measured-$iteration")
            }

        val throughputSamples = measurements.map { it.recordsPerSecond }
        val latencySamples = measurements.flatMap { it.latenciesMs }
        val invariantSamples =
            measurements.map {
                val actual = it.enrichedOutputRecords + it.summaryOutputRecords
                actual / (recordCount * 2.0)
            }
        check(invariantSamples.all { it == 1.0 }) {
            "Topology output invariant failed: expected $recordCount records in each output topic"
        }

        val report =
            BenchmarkEvidence.report(
                benchmarkId = "topology-test-driver",
                recordCount = recordCount,
                warmupIterations = warmupIterations,
                measuredIterations = measuredIterations,
                startedAt = startedAt,
                elapsedNanos = System.nanoTime() - executionStart,
                metrics =
                    listOf(
                        BenchmarkMetricV2(
                            name = "topology_input_records_per_second",
                            value = BenchmarkEvidence.median(throughputSamples),
                            unit = "records/s",
                            direction = "higher_is_better",
                            samples = throughputSamples,
                            failures = 0,
                            summary =
                                mapOf(
                                    "minimum" to throughputSamples.min(),
                                    "maximum" to throughputSamples.max(),
                                    "p95" to BenchmarkEvidence.percentile(throughputSamples, 0.95),
                                    "record_count" to recordCount.toDouble(),
                                    "elapsed_ms_median" to
                                        BenchmarkEvidence.median(measurements.map { it.elapsedMs }),
                                ),
                        ),
                        BenchmarkMetricV2(
                            name = "topology_input_latency_p95_ms",
                            value = BenchmarkEvidence.percentile(latencySamples, 0.95),
                            unit = "ms",
                            direction = "lower_is_better",
                            samples = latencySamples,
                            failures = 0,
                            summary =
                                mapOf(
                                    "average" to latencySamples.average(),
                                    "p50" to BenchmarkEvidence.percentile(latencySamples, 0.50),
                                    "p99" to BenchmarkEvidence.percentile(latencySamples, 0.99),
                                ),
                        ),
                        BenchmarkMetricV2(
                            name = "output_invariant_ratio",
                            value = invariantSamples.average(),
                            unit = "ratio",
                            direction = "target",
                            samples = invariantSamples,
                            failures = 0,
                            summary = mapOf("target" to 1.0),
                        ),
                    ),
                environment =
                    mapOf(
                        "mode" to "topology-test-driver",
                        "broker" to "none",
                        "seed" to SEED.toString(),
                        "records" to recordCount.toString(),
                    ),
            )
        BenchmarkEvidence.write(report, outputPath)
        return report
    }

    private fun measure(
        recordCount: Int,
        iterationId: String,
    ): TopologyMeasurement {
        val purchaseSerde = DomainJsonSerdes.purchaseEvent()
        val profileSerde = DomainJsonSerdes.customerProfile()
        val enrichedSerde = DomainJsonSerdes.enrichedPurchase()
        val summarySerde = DomainJsonSerdes.customerSummary()
        val driver =
            TopologyTestDriver(
                TopologyFactory.build(),
                TopologyFactory.properties("topology-benchmark-$SEED-$iterationId"),
            )

        return driver.use {
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
            val elapsedNanos = System.nanoTime() - batchStart

            val enrichedCount = enrichedOutput.readKeyValuesToList().size
            val summaryCount = summaryOutput.readKeyValuesToList().size
            TopologyMeasurement(
                recordsPerSecond = recordCount / (elapsedNanos / 1_000_000_000.0),
                elapsedMs = elapsedNanos / 1_000_000.0,
                latenciesMs = timings.map { it / 1_000_000.0 },
                enrichedOutputRecords = enrichedCount,
                summaryOutputRecords = summaryCount,
            )
        }
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
            eventId = "event-${index.toString().padStart(8, '0')}",
            customerId = "customer-${customer.toString().padStart(3, '0')}",
            sku = "sku-${(index % 17).toString().padStart(2, '0')}",
            quantity = 1 + (index % 4),
            amountCents = 1_000L + ((index * 137L) % 10_000L),
            occurredAtEpochMs = BASE_TIMESTAMP + index,
        )
    }

    fun fixtureDescriptor(recordCount: Int): String =
        buildString {
            append("seed=42;records=")
            append(recordCount)
            append(";profiles=")
            profiles().forEach { append(it).append(';') }
            append("events=")
            repeat(recordCount.coerceAtMost(1_000)) { append(event(it)).append(';') }
        }
}
