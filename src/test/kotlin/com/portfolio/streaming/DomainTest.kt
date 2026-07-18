package com.portfolio.streaming

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.EnrichmentPolicy
import com.portfolio.streaming.domain.PurchaseEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DomainTest {
    @Test
    fun `enrichment policy keeps event facts and adds profile facts`() {
        val event = PurchaseEvent("event-1", "customer-1", "sku-1", 2, 2500, 42)
        val profile = CustomerProfile("customer-1", "premium", "BR", 0.02)

        val enriched = EnrichmentPolicy.enrich(event, profile)

        assertThat(enriched.eventId).isEqualTo("event-1")
        assertThat(enriched.customerId).isEqualTo("customer-1")
        assertThat(enriched.segment).isEqualTo("premium")
        assertThat(enriched.country).isEqualTo("BR")
        assertThat(enriched.amountCents).isEqualTo(2500)
    }
}
