package com.portfolio.streaming.infra

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class JsonSerde<T>(
    private val valueSerializer: KSerializer<T>,
) : Serde<T> {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    override fun serializer(): Serializer<T> =
        Serializer { _, value ->
            value?.let { json.encodeToString(valueSerializer, it).toByteArray(Charsets.UTF_8) }
        }

    override fun deserializer(): Deserializer<T> =
        Deserializer { _, bytes ->
            bytes?.let { json.decodeFromString(valueSerializer, it.toString(Charsets.UTF_8)) }
        }
}
