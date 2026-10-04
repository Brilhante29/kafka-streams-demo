package com.portfolio.streaming

import com.portfolio.streaming.benchmark.RealBrokerBenchmarkRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.nio.file.Path

private const val KAFKA_IMAGE =
    "apache/kafka-native:4.3.1@" +
        "sha256:2885898ba17065023f1bd605f3a81efcfa986014f062b73b91ef5462485f9060"

class RealBrokerBenchmarkTest {
    @Test
    fun `processes every record through a real broker and preserves aggregate state`() {
        val evidencePath = System.getenv("REAL_BROKER_EVIDENCE_PATH")
        val output =
            if (evidencePath == null) {
                Files.createTempFile("kafka-broker-evidence-", ".json")
            } else {
                Path.of(evidencePath)
            }
        val kafka =
            KafkaContainer(
                DockerImageName.parse(KAFKA_IMAGE).asCompatibleSubstituteFor("apache/kafka"),
            )

        kafka.start()
        try {
            val report =
                RealBrokerBenchmarkRunner.run(
                    bootstrapServers = kafka.bootstrapServers,
                    recordCount = 30,
                    warmupIterations = 1,
                    measuredIterations = 2,
                    outputPath = output,
                )

            assertThat(report.schemaVersion).isEqualTo(2)
            assertThat(report.benchmarkId).isEqualTo("real-broker-end-to-end")
            assertThat(report.metrics.first().value).isPositive()
            assertThat(report.metrics.first().samples).hasSize(2)
            assertThat(report.metrics.last().value).isEqualTo(1.0)
            assertThat(output).exists()
        } finally {
            kafka.stop()
        }
    }
}
