package com.portfolio.streaming

import com.portfolio.streaming.benchmark.Fixtures
import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.CustomerSummary
import com.portfolio.streaming.domain.EnrichedPurchase
import com.portfolio.streaming.domain.PurchaseEvent
import com.portfolio.streaming.infra.JsonSerde
import com.portfolio.streaming.infra.TopologyFactory
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TopologyTest {
    @Test
    fun `joins profiles and emits running customer aggregate without a broker`() {
        val driver = TopologyTestDriver(TopologyFactory.build(), TopologyFactory.properties("test-join-aggregate"))
        val profileSerde = JsonSerde(CustomerProfile.serializer())
        val purchaseSerde = JsonSerde(PurchaseEvent.serializer())
        val enrichedSerde = JsonSerde(EnrichedPurchase.serializer())
        val summarySerde = JsonSerde(CustomerSummary.serializer())

        try {
            val profiles =
                driver.createInputTopic(
                    TopologyFactory.PROFILES_TOPIC,
                    Serdes.String().serializer(),
                    profileSerde.serializer(),
                )
            val purchases =
                driver.createInputTopic(
                    TopologyFactory.PURCHASES_TOPIC,
                    Serdes.String().serializer(),
                    purchaseSerde.serializer(),
                )
            val enriched =
                driver.createOutputTopic(
                    TopologyFactory.ENRICHED_TOPIC,
                    Serdes.String().deserializer(),
                    enrichedSerde.deserializer(),
                )
            val summaries =
                driver.createOutputTopic(
                    TopologyFactory.SUMMARY_TOPIC,
                    Serdes.String().deserializer(),
                    summarySerde.deserializer(),
                )

            profiles.pipeInput("customer-000", Fixtures.profiles().first())
            purchases.pipeInput("customer-000", Fixtures.event(0))
            purchases.pipeInput("customer-000", Fixtures.event(3))

            val enrichedRecords = enriched.readKeyValuesToList()
            val summaryRecords = summaries.readKeyValuesToList()

            assertThat(enrichedRecords).hasSize(2)
            assertThat(enrichedRecords.map { it.value.segment }).containsOnly("premium")
            assertThat(summaryRecords).hasSize(2)
            assertThat(summaryRecords.last().value).isEqualTo(
                CustomerSummary(
                    customerId = "customer-000",
                    segment = "premium",
                    country = "BR",
                    totalOrders = 2,
                    totalUnits = 5,
                    totalAmountCents = Fixtures.event(0).amountCents + Fixtures.event(3).amountCents,
                ),
            )
        } finally {
            driver.close()
        }
    }

    @Test
    fun `does not emit an event for an unknown profile`() {
        val driver = TopologyTestDriver(TopologyFactory.build(), TopologyFactory.properties("test-unknown-profile"))
        val purchaseSerde = JsonSerde(PurchaseEvent.serializer())
        val enrichedSerde = JsonSerde(EnrichedPurchase.serializer())

        try {
            val purchases =
                driver.createInputTopic(
                    TopologyFactory.PURCHASES_TOPIC,
                    Serdes.String().serializer(),
                    purchaseSerde.serializer(),
                )
            val enriched =
                driver.createOutputTopic(
                    TopologyFactory.ENRICHED_TOPIC,
                    Serdes.String().deserializer(),
                    enrichedSerde.deserializer(),
                )

            purchases.pipeInput("customer-404", Fixtures.event(0).copy(customerId = "customer-404"))

            assertThat(enriched.isEmpty).isTrue()
        } finally {
            driver.close()
        }
    }
}
