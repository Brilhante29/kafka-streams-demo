package com.portfolio.streaming.infra

import com.portfolio.streaming.domain.CustomerProfile
import com.portfolio.streaming.domain.CustomerSummary
import com.portfolio.streaming.domain.EnrichedPurchase
import com.portfolio.streaming.domain.PurchaseEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

object DomainJsonSerdes {
    fun purchaseEvent(): Serde<PurchaseEvent> =
        mappingSerde(
            PurchaseEventDocument.serializer(),
            { value ->
                PurchaseEventDocument(
                    value.eventId,
                    value.customerId,
                    value.sku,
                    value.quantity,
                    value.amountCents,
                    value.occurredAtEpochMs,
                )
            },
            { value ->
                PurchaseEvent(
                    value.eventId,
                    value.customerId,
                    value.sku,
                    value.quantity,
                    value.amountCents,
                    value.occurredAtEpochMs,
                )
            },
        )

    fun customerProfile(): Serde<CustomerProfile> =
        mappingSerde(
            CustomerProfileDocument.serializer(),
            { value ->
                CustomerProfileDocument(
                    value.customerId,
                    value.segment,
                    value.country,
                    value.riskScore,
                )
            },
            { value ->
                CustomerProfile(
                    value.customerId,
                    value.segment,
                    value.country,
                    value.riskScore,
                )
            },
        )

    fun enrichedPurchase(): Serde<EnrichedPurchase> =
        mappingSerde(
            EnrichedPurchaseDocument.serializer(),
            { value ->
                EnrichedPurchaseDocument(
                    value.eventId,
                    value.customerId,
                    value.sku,
                    value.segment,
                    value.country,
                    value.quantity,
                    value.amountCents,
                    value.occurredAtEpochMs,
                )
            },
            { value ->
                EnrichedPurchase(
                    value.eventId,
                    value.customerId,
                    value.sku,
                    value.segment,
                    value.country,
                    value.quantity,
                    value.amountCents,
                    value.occurredAtEpochMs,
                )
            },
        )

    fun customerSummary(): Serde<CustomerSummary> =
        mappingSerde(
            CustomerSummaryDocument.serializer(),
            { value ->
                CustomerSummaryDocument(
                    value.customerId,
                    value.segment,
                    value.country,
                    value.totalOrders,
                    value.totalUnits,
                    value.totalAmountCents,
                )
            },
            { value ->
                CustomerSummary(
                    value.customerId,
                    value.segment,
                    value.country,
                    value.totalOrders,
                    value.totalUnits,
                    value.totalAmountCents,
                )
            },
        )

    private fun <Domain, Document> mappingSerde(
        documentSerializer: KSerializer<Document>,
        toDocument: (Domain) -> Document,
        toDomain: (Document) -> Domain,
    ): Serde<Domain> = MappingJsonSerde(documentSerializer, toDocument, toDomain)
}

private class MappingJsonSerde<Domain, Document>(
    private val documentSerializer: KSerializer<Document>,
    private val toDocument: (Domain) -> Document,
    private val toDomain: (Document) -> Domain,
) : Serde<Domain> {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    override fun serializer(): Serializer<Domain> =
        Serializer { _, value ->
            value?.let {
                json
                    .encodeToString(documentSerializer, toDocument(it))
                    .toByteArray(Charsets.UTF_8)
            }
        }

    override fun deserializer(): Deserializer<Domain> =
        Deserializer { _, bytes ->
            bytes?.let {
                toDomain(json.decodeFromString(documentSerializer, it.toString(Charsets.UTF_8)))
            }
        }
}

@Serializable
private data class PurchaseEventDocument(
    val eventId: String,
    val customerId: String,
    val sku: String,
    val quantity: Int,
    val amountCents: Long,
    val occurredAtEpochMs: Long,
)

@Serializable
private data class CustomerProfileDocument(
    val customerId: String,
    val segment: String,
    val country: String,
    val riskScore: Double,
)

@Serializable
private data class EnrichedPurchaseDocument(
    val eventId: String,
    val customerId: String,
    val sku: String,
    val segment: String,
    val country: String,
    val quantity: Int,
    val amountCents: Long,
    val occurredAtEpochMs: Long,
)

@Serializable
private data class CustomerSummaryDocument(
    val customerId: String,
    val segment: String,
    val country: String,
    val totalOrders: Long,
    val totalUnits: Long,
    val totalAmountCents: Long,
)
