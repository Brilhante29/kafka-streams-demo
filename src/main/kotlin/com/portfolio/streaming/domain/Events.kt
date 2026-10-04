package com.portfolio.streaming.domain

data class PurchaseEvent(
    val eventId: String,
    val customerId: String,
    val sku: String,
    val quantity: Int,
    val amountCents: Long,
    val occurredAtEpochMs: Long,
)

data class CustomerProfile(
    val customerId: String,
    val segment: String,
    val country: String,
    val riskScore: Double,
)

data class EnrichedPurchase(
    val eventId: String,
    val customerId: String,
    val sku: String,
    val segment: String,
    val country: String,
    val quantity: Int,
    val amountCents: Long,
    val occurredAtEpochMs: Long,
)

data class CustomerSummary(
    val customerId: String,
    val segment: String,
    val country: String,
    val totalOrders: Long,
    val totalUnits: Long,
    val totalAmountCents: Long,
) {
    fun add(event: EnrichedPurchase): CustomerSummary =
        if (totalOrders == 0L) {
            copy(
                customerId = event.customerId,
                segment = event.segment,
                country = event.country,
                totalOrders = 1,
                totalUnits = event.quantity.toLong(),
                totalAmountCents = event.amountCents,
            )
        } else {
            copy(
                totalOrders = totalOrders + 1,
                totalUnits = totalUnits + event.quantity,
                totalAmountCents = totalAmountCents + event.amountCents,
            )
        }

    companion object {
        fun empty(): CustomerSummary = CustomerSummary("", "", "", 0, 0, 0)
    }
}

object EnrichmentPolicy {
    fun enrich(
        event: PurchaseEvent,
        profile: CustomerProfile,
    ): EnrichedPurchase =
        EnrichedPurchase(
            eventId = event.eventId,
            customerId = event.customerId,
            sku = event.sku,
            segment = profile.segment,
            country = profile.country,
            quantity = event.quantity,
            amountCents = event.amountCents,
            occurredAtEpochMs = event.occurredAtEpochMs,
        )
}
