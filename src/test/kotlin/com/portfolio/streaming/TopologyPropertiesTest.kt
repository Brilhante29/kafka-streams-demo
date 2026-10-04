package com.portfolio.streaming

import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.streams.StreamsConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TopologyPropertiesTest {
    @Test
    fun `runtime properties keep Kafka Streams defaults for commit interval, cache, and state dir`() {
        val properties = TopologyFactory.runtimeProperties("orders-app", "broker:9092")

        assertThat(properties[StreamsConfig.APPLICATION_ID_CONFIG]).isEqualTo("orders-app")
        assertThat(properties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG]).isEqualTo("broker:9092")
        assertThat(properties).doesNotContainKeys(
            StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
            StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG,
            StreamsConfig.STATE_DIR_CONFIG,
        )
    }

    @Test
    fun `test-driver properties commit every record with the cache disabled`() {
        val properties = TopologyFactory.properties("deterministic-test")

        assertThat(properties[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG]).isEqualTo(0)
        assertThat(properties[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG]).isEqualTo(0)
        assertThat(properties[StreamsConfig.STATE_DIR_CONFIG]).isEqualTo("build/streams-state")
    }
}
