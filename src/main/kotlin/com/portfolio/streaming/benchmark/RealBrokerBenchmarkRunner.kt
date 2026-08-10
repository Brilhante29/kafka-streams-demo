package com.portfolio.streaming.benchmark

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.CustomerSummary
import com.portfolio.streaming.domain.PurchaseEvent
import com.portfolio.streaming.infra.DomainJsonSerdes
import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private data class BrokerMeasurement(
    val recordsPerSecond: Double,
    val elapsedMs: Double,
    val enrichedOutputRecords: Int,
    val summaryOrdersBefore: Long,
    val summaryOrdersAfter: Long,
)

object RealBrokerBenchmarkRunner {
    private const val PARTITIONS = 3
    private val timeout = Duration.ofSeconds(60)

    fun run(
        bootstrapServers: String,
        recordCount: Int = 1_000,
        warmupIterations: Int = 1,
        measuredIterations: Int = 5,
        outputPath: Path,
    ): BenchmarkReportV2 {
        require(bootstrapServers.isNotBlank()) { "bootstrapServers must not be blank" }
        require(recordCount > 0) { "recordCount must be positive" }
        require(warmupIterations >= 0) { "warmupIterations must not be negative" }
        require(measuredIterations > 0) { "measuredIterations must be positive" }

        resetTopics(bootstrapServers)
        val applicationId = "kafka-streams-benchmark-${UUID.randomUUID()}"
        val streams =
            KafkaStreams(
                TopologyFactory.build(),
                TopologyFactory.properties(applicationId, bootstrapServers, realBroker = true),
            )
        val startupFinished = CountDownLatch(1)
        val currentState = AtomicReference(KafkaStreams.State.CREATED)
        val startupFailure = AtomicReference<Throwable?>()
        streams.setStateListener { newState, _ ->
            currentState.set(newState)
            if (newState == KafkaStreams.State.RUNNING || newState == KafkaStreams.State.ERROR) {
                startupFinished.countDown()
            }
        }
        streams.setUncaughtExceptionHandler { error ->
            startupFailure.compareAndSet(null, error)
            startupFinished.countDown()
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT
        }

        val producer = producer(bootstrapServers)
        val consumer = consumer(bootstrapServers)
        val profileSerializer = DomainJsonSerdes.customerProfile().serializer()
        val purchaseSerializer = DomainJsonSerdes.purchaseEvent().serializer()
        val startedAt = Instant.now()
        val executionStart = System.nanoTime()

        try {
            streams.start()
            check(startupFinished.await(timeout.toSeconds(), TimeUnit.SECONDS)) {
                "Kafka Streams did not finish startup within $timeout"
            }
            check(currentState.get() == KafkaStreams.State.RUNNING) {
                val cause = startupFailure.get()?.let { ": ${it::class.simpleName}: ${it.message}" }.orEmpty()
                "Kafka Streams entered ${currentState.get()} during startup$cause"
            }

            consumer.subscribe(listOf(TopologyFactory.ENRICHED_TOPIC))
            awaitAssignment(consumer)
            Fixtures.profiles().forEach { profile ->
                producer.send(
                    ProducerRecord(
                        TopologyFactory.PROFILES_TOPIC,
                        profile.customerId,
                        profileSerializer.serialize(TopologyFactory.PROFILES_TOPIC, profile),
                    ),
                )
            }
            producer.flush()
            awaitProfiles(streams)

            var nextEventIndex = 0
            repeat(warmupIterations) {
                measure(streams, producer, consumer, purchaseSerializer, nextEventIndex, recordCount)
                nextEventIndex += recordCount
            }

            val measurements =
                List(measuredIterations) {
                    val measurement =
                        measure(
                            streams,
                            producer,
                            consumer,
                            purchaseSerializer,
                            nextEventIndex,
                            recordCount,
                        )
                    nextEventIndex += recordCount
                    measurement
                }

            val throughputSamples = measurements.map { it.recordsPerSecond }
            val latencySamples = measurements.map { it.elapsedMs }
            val invariantSamples =
                measurements.map { measurement ->
                    val enrichedRatio = measurement.enrichedOutputRecords / recordCount.toDouble()
                    val summaryDelta = measurement.summaryOrdersAfter - measurement.summaryOrdersBefore
                    val summaryRatio = summaryDelta / recordCount.toDouble()
                    minOf(enrichedRatio, summaryRatio)
                }
            check(invariantSamples.all { it == 1.0 }) {
                "Broker output invariant failed: expected enriched output and aggregate delta for every input"
            }

            val report =
                BenchmarkEvidence.report(
                    benchmarkId = "real-broker-end-to-end",
                    recordCount = recordCount,
                    warmupIterations = warmupIterations,
                    measuredIterations = measuredIterations,
                    startedAt = startedAt,
                    elapsedNanos = System.nanoTime() - executionStart,
                    metrics =
                        listOf(
                            BenchmarkMetricV2(
                                name = "end_to_end_input_records_per_second",
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
                                    ),
                            ),
                            BenchmarkMetricV2(
                                name = "end_to_end_batch_latency_ms",
                                value = BenchmarkEvidence.median(latencySamples),
                                unit = "ms",
                                direction = "lower_is_better",
                                samples = latencySamples,
                                failures = 0,
                                summary =
                                    mapOf(
                                        "minimum" to latencySamples.min(),
                                        "maximum" to latencySamples.max(),
                                        "p95" to BenchmarkEvidence.percentile(latencySamples, 0.95),
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
                            "mode" to "real-broker",
                            "broker" to (System.getenv("KAFKA_BROKER_IMAGE") ?: "kafka-compatible"),
                            "processing_guarantee" to "exactly_once_v2",
                            "partitions" to PARTITIONS.toString(),
                            "records" to recordCount.toString(),
                            "seed" to "42",
                        ),
                )
            BenchmarkEvidence.write(report, outputPath)
            return report
        } finally {
            consumer.close()
            producer.close(Duration.ofSeconds(5))
            streams.close(Duration.ofSeconds(10))
        }
    }

    private fun measure(
        streams: KafkaStreams,
        producer: KafkaProducer<String, ByteArray>,
        consumer: KafkaConsumer<String, ByteArray>,
        serializer: org.apache.kafka.common.serialization.Serializer<PurchaseEvent>,
        startIndex: Int,
        recordCount: Int,
    ): BrokerMeasurement {
        val summaryBefore = summaryOrderCount(streams)
        val start = System.nanoTime()
        repeat(recordCount) { offset ->
            val event = Fixtures.event(startIndex + offset)
            producer.send(
                ProducerRecord(
                    TopologyFactory.PURCHASES_TOPIC,
                    event.customerId,
                    serializer.serialize(TopologyFactory.PURCHASES_TOPIC, event),
                ),
            )
        }
        producer.flush()
        val enrichedCount = awaitEnriched(consumer, recordCount)
        val expectedSummary = summaryBefore + recordCount
        val summaryAfter = awaitSummaryOrderCount(streams, expectedSummary)
        val elapsedNanos = System.nanoTime() - start

        return BrokerMeasurement(
            recordsPerSecond = recordCount / (elapsedNanos / 1_000_000_000.0),
            elapsedMs = elapsedNanos / 1_000_000.0,
            enrichedOutputRecords = enrichedCount,
            summaryOrdersBefore = summaryBefore,
            summaryOrdersAfter = summaryAfter,
        )
    }

    private fun resetTopics(bootstrapServers: String) {
        val externalTopics =
            listOf(
                TopologyFactory.PURCHASES_TOPIC,
                TopologyFactory.PROFILES_TOPIC,
                TopologyFactory.ENRICHED_TOPIC,
                TopologyFactory.SUMMARY_TOPIC,
            )
        AdminClient.create(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)).use { admin ->
            val existing = admin.listTopics().names().get(timeout.toSeconds(), TimeUnit.SECONDS)
            val stale = externalTopics.filter(existing::contains)
            if (stale.isNotEmpty()) {
                admin.deleteTopics(stale).all().get(timeout.toSeconds(), TimeUnit.SECONDS)
            }
            val topics =
                externalTopics.map { name ->
                    NewTopic(name, Optional.of(PARTITIONS), Optional.of(1.toShort())).apply {
                        if (name == TopologyFactory.PROFILES_TOPIC) {
                            configs(mapOf(TopicConfig.CLEANUP_POLICY_CONFIG to TopicConfig.CLEANUP_POLICY_COMPACT))
                        }
                    }
                }
            admin.createTopics(topics).all().get(timeout.toSeconds(), TimeUnit.SECONDS)
        }
    }

    private fun producer(bootstrapServers: String): KafkaProducer<String, ByteArray> =
        KafkaProducer(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.ACKS_CONFIG to "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
                ProducerConfig.LINGER_MS_CONFIG to 0,
            ),
            StringSerializer(),
            ByteArraySerializer(),
        )

    private fun consumer(bootstrapServers: String): KafkaConsumer<String, ByteArray> =
        KafkaConsumer(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "benchmark-output-${UUID.randomUUID()}",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
                ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
            ),
            StringDeserializer(),
            ByteArrayDeserializer(),
        )

    private fun awaitAssignment(consumer: KafkaConsumer<String, ByteArray>) {
        val deadline = System.nanoTime() + timeout.toNanos()
        while (consumer.assignment().isEmpty() && System.nanoTime() < deadline) {
            consumer.poll(Duration.ofMillis(100))
        }
        check(consumer.assignment().isNotEmpty()) { "Output consumer received no partition assignment" }
    }

    private fun awaitProfiles(streams: KafkaStreams) {
        val deadline = System.nanoTime() + timeout.toNanos()
        while (System.nanoTime() < deadline) {
            val store = runCatching { profileStore(streams) }.getOrNull()
            if (store != null && Fixtures.profiles().all { store.get(it.customerId) == it }) {
                return
            }
            Thread.sleep(50)
        }
        error("Customer profiles were not materialized within $timeout")
    }

    private fun awaitEnriched(
        consumer: KafkaConsumer<String, ByteArray>,
        expected: Int,
    ): Int {
        val deadline = System.nanoTime() + timeout.toNanos()
        var count = 0
        while (count < expected && System.nanoTime() < deadline) {
            count += consumer.poll(Duration.ofMillis(100)).count()
        }
        check(count == expected) { "Expected $expected enriched outputs, received $count" }
        return count
    }

    private fun awaitSummaryOrderCount(
        streams: KafkaStreams,
        expected: Long,
    ): Long {
        val deadline = System.nanoTime() + timeout.toNanos()
        while (System.nanoTime() < deadline) {
            val actual = summaryOrderCount(streams)
            if (actual == expected) {
                return actual
            }
            check(actual < expected) { "Summary order count exceeded expected value: $actual > $expected" }
            Thread.sleep(25)
        }
        error("Summary order count did not reach $expected within $timeout")
    }

    private fun summaryOrderCount(streams: KafkaStreams): Long {
        val store = waitForStore<CustomerSummary>(streams, TopologyFactory.SUMMARY_STORE)
        return Fixtures.profiles().sumOf { profile -> store.get(profile.customerId)?.totalOrders ?: 0L }
    }

    private fun profileStore(streams: KafkaStreams): ReadOnlyKeyValueStore<String, CustomerProfile> =
        waitForStore(streams, TopologyFactory.PROFILE_STORE)

    private fun <V> waitForStore(
        streams: KafkaStreams,
        name: String,
    ): ReadOnlyKeyValueStore<String, V> =
        try {
            streams.store(
                StoreQueryParameters.fromNameAndType(
                    name,
                    QueryableStoreTypes.keyValueStore(),
                ),
            )
        } catch (error: InvalidStateStoreException) {
            throw error
        }
}
