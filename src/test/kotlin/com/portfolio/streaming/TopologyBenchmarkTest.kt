package com.portfolio.streaming

import com.portfolio.streaming.benchmark.BenchmarkEvidence
import com.portfolio.streaming.benchmark.TopologyBenchmarkRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class TopologyBenchmarkTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `emits versioned evidence with repeated samples and output invariants`() {
        val output = tempDir.resolve("topology.json")

        val report =
            TopologyBenchmarkRunner.run(
                recordCount = 6,
                warmupIterations = 0,
                measuredIterations = 2,
                outputPath = output,
            )

        assertThat(report.schemaVersion).isEqualTo(2)
        assertThat(report.benchmarkId).isEqualTo("topology-test-driver")
        assertThat(report.metrics.first().samples).hasSize(2)
        assertThat(report.metrics.last().value).isEqualTo(1.0)
        assertThat(BenchmarkEvidence.verifyArtifactDigest(report)).isTrue()
        val tampered =
            report.copy(
                metrics =
                    listOf(report.metrics.first().copy(value = report.metrics.first().value + 1.0)) +
                        report.metrics.drop(1),
            )
        assertThat(BenchmarkEvidence.verifyArtifactDigest(tampered)).isFalse()
        assertThat(output).exists()
    }
}
