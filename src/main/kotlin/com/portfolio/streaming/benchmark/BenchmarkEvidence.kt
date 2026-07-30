package com.portfolio.streaming.benchmark

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.math.ceil

@Serializable
data class BenchmarkMetricV2(
    val name: String,
    val value: Double,
    val unit: String,
    val direction: String,
    val samples: List<Double>,
    val failures: Int,
    val summary: Map<String, Double>,
)

@Serializable
data class BenchmarkExecutionV2(
    val command: String,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("duration_seconds")
    val durationSeconds: Double,
    @SerialName("exit_code")
    val exitCode: Int,
    val repeat: Int,
)

@Serializable
data class BenchmarkProvenanceV2(
    @SerialName("source_commit")
    val sourceCommit: String,
    @SerialName("clean_tree")
    val cleanTree: Boolean,
    @SerialName("image_ref")
    val imageRef: String,
    @SerialName("image_digest")
    val imageDigest: String,
    @SerialName("dependency_lock_digest")
    val dependencyLockDigest: String,
    val producer: String,
    @SerialName("artifact_digest")
    val artifactDigest: String,
    @SerialName("ci_run_url")
    val ciRunUrl: String? = null,
)

@Serializable
data class BenchmarkWorkloadV2(
    val version: String,
    @SerialName("fixture_digest")
    val fixtureDigest: String,
    @SerialName("config_digest")
    val configDigest: String,
    @SerialName("warmup_iterations")
    val warmupIterations: Int,
    @SerialName("measured_iterations")
    val measuredIterations: Int,
    val concurrency: Int,
)

@Serializable
data class BenchmarkReportV2(
    @SerialName("schema_version")
    val schemaVersion: Int = 2,
    @SerialName("run_id")
    val runId: String,
    val project: String,
    @SerialName("benchmark_id")
    val benchmarkId: String,
    val workload: BenchmarkWorkloadV2,
    val metrics: List<BenchmarkMetricV2>,
    val execution: BenchmarkExecutionV2,
    val environment: Map<String, String>,
    val provenance: BenchmarkProvenanceV2,
    @SerialName("comparability_key")
    val comparabilityKey: String,
)

object BenchmarkEvidence {
    private val json =
        Json {
            prettyPrint = true
            explicitNulls = false
            encodeDefaults = true
        }

    fun report(
        benchmarkId: String,
        recordCount: Int,
        warmupIterations: Int,
        measuredIterations: Int,
        startedAt: Instant,
        elapsedNanos: Long,
        metrics: List<BenchmarkMetricV2>,
        environment: Map<String, String>,
    ): BenchmarkReportV2 {
        val config =
            listOf(
                benchmarkId,
                recordCount,
                warmupIterations,
                measuredIterations,
                environment.toSortedMap(),
            ).joinToString("|")

        return BenchmarkReportV2(
            runId = UUID.randomUUID().toString(),
            project = "kafka-streams-demo",
            benchmarkId = benchmarkId,
            workload =
                BenchmarkWorkloadV2(
                    version = "1",
                    fixtureDigest = sha256(Fixtures.fixtureDescriptor(recordCount)),
                    configDigest = sha256(config),
                    warmupIterations = warmupIterations,
                    measuredIterations = measuredIterations,
                    concurrency = 1,
                ),
            metrics = metrics,
            execution =
                BenchmarkExecutionV2(
                    command = System.getProperty("sun.java.command", benchmarkId),
                    startedAt = startedAt.toString(),
                    durationSeconds = elapsedNanos / 1_000_000_000.0,
                    exitCode = 0,
                    repeat = measuredIterations,
                ),
            environment =
                mapOf(
                    "runtime" to "Java ${System.getProperty("java.version", "unknown")} / Kotlin ${KotlinVersion.CURRENT}",
                    "architecture" to System.getProperty("os.arch", "unknown"),
                    "hardware_class" to (
                        System.getenv("HARDWARE_CLASS")
                            ?: "${Runtime.getRuntime().availableProcessors()}cpu"
                    ),
                    "os" to System.getProperty("os.name", "unknown"),
                ) + environment,
            provenance = provenance(benchmarkId, config),
            comparabilityKey = "kafka-streams-demo:$benchmarkId:v1:jvm21",
        )
    }

    fun write(
        report: BenchmarkReportV2,
        outputPath: Path,
    ) {
        outputPath.parent?.createDirectories()
        Files.writeString(outputPath, json.encodeToString(report) + System.lineSeparator())
    }

    fun percentile(
        values: List<Double>,
        quantile: Double,
    ): Double {
        require(values.isNotEmpty()) { "percentile requires at least one value" }
        val sorted = values.sorted()
        val index = ceil(quantile * sorted.size).toInt().coerceAtLeast(1) - 1
        return sorted[index.coerceAtMost(sorted.lastIndex)]
    }

    fun median(values: List<Double>): Double = percentile(values, 0.5)

    fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return "sha256:" + digest.joinToString("") { "%02x".format(it) }
    }

    private fun provenance(
        benchmarkId: String,
        config: String,
    ): BenchmarkProvenanceV2 {
        val imageRef = System.getenv("EVIDENCE_IMAGE_REF") ?: "local-jvm"
        val sourceCommit =
            System
                .getenv("SOURCE_COMMIT")
                ?.lowercase()
                ?.takeIf { it.matches(Regex("[0-9a-f]{40}")) }
                ?: git("rev-parse", "HEAD")?.lowercase()?.takeIf { it.matches(Regex("[0-9a-f]{40}")) }
                ?: "0000000000000000000000000000000000000000"
        return BenchmarkProvenanceV2(
            sourceCommit = sourceCommit,
            cleanTree =
                System.getenv("EVIDENCE_CLEAN_TREE")?.toBooleanStrictOrNull()
                    ?: (git("status", "--porcelain")?.isEmpty() == true),
            imageRef = imageRef,
            imageDigest = System.getenv("EVIDENCE_IMAGE_DIGEST") ?: sha256(imageRef),
            dependencyLockDigest =
                System.getenv("DEPENDENCY_LOCK_DIGEST")
                    ?: runCatching {
                        sha256(Files.readString(Path.of("gradle.lockfile")))
                    }.getOrElse { sha256("gradle.lockfile-unavailable") },
            producer = System.getenv("EVIDENCE_PRODUCER") ?: "local",
            artifactDigest = sha256("$benchmarkId|$config"),
            ciRunUrl = System.getenv("CI_RUN_URL"),
        )
    }

    private fun git(vararg arguments: String): String? =
        runCatching {
            val process = ProcessBuilder(listOf("git", *arguments)).redirectErrorStream(true).start()
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                null
            } else if (process.exitValue() == 0) {
                process.inputStream
                    .bufferedReader()
                    .readText()
                    .trim()
            } else {
                null
            }
        }.getOrNull()
}
