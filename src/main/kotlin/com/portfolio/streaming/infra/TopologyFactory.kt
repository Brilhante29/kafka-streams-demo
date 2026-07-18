package com.portfolio.streaming.infra

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.CustomerSummary
import com.portfolio.streaming.domain.EnrichedPurchase
import com.portfolio.streaming.domain.EnrichmentPolicy
import com.portfolio.streaming.domain.PurchaseEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore
import java.util.Properties

object TopologyFactory {
    const val PURCHASES_TOPIC = "purchases"
    const val PROFILES_TOPIC = "customer-profiles"
    const val ENRICHED_TOPIC = "enriched-purchases"
    const val SUMMARY_TOPIC = "customer-summaries"
    const val SUMMARY_STORE = "customer-summary-store"

    fun build(): Topology {
        val builder = StreamsBuilder()
        val purchaseSerde = JsonSerde(PurchaseEvent.serializer())
        val profileSerde = JsonSerde(CustomerProfile.serializer())
        val enrichedSerde = JsonSerde(EnrichedPurchase.serializer())
        val summarySerde = JsonSerde(CustomerSummary.serializer())

        val profiles =
            builder.table(
                PROFILES_TOPIC,
                Consumed.with(Serdes.String(), profileSerde),
            )
        val purchases =
            builder.stream(
                PURCHASES_TOPIC,
                Consumed.with(Serdes.String(), purchaseSerde),
            )

        val enriched =
            purchases.join(
                profiles,
                { event, profile -> EnrichmentPolicy.enrich(event, profile) },
                Joined.with(Serdes.String(), purchaseSerde, profileSerde),
            )

        enriched.to(ENRICHED_TOPIC, Produced.with(Serdes.String(), enrichedSerde))

        enriched
            .selectKey { _, event -> event.customerId }
            .groupByKey(Grouped.with(Serdes.String(), enrichedSerde))
            .aggregate(
                { CustomerSummary.empty() },
                { _, event, current -> current.add(event) },
                Materialized
                    .`as`<String, CustomerSummary, KeyValueStore<Bytes, ByteArray>>(SUMMARY_STORE)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(summarySerde),
            ).toStream()
            .to(SUMMARY_TOPIC, Produced.with(Serdes.String(), summarySerde))

        return builder.build()
    }

    fun properties(
        applicationId: String = "kafka-streams-demo",
        bootstrapServers: String = "dummy:9092",
    ): Properties =
        Properties().apply {
            put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java.name)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray()::class.java.name)
            put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0)
            put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0)
            put(StreamsConfig.STATE_DIR_CONFIG, "build/streams-state")
        }
}
