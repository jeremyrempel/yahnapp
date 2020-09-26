package com.github.jeremyrempel.yahnapp.api

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class HackerNewsApi(
    private val scheme: String = "https",
    private val host: String = "hacker-news.firebaseio.com",
    private val networkDebug: (String) -> Unit
) {
    private val client = HttpClient {
        install(JsonFeature) {
            val config = Json.Default
            serializer = KotlinxSerializer(config)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    networkDebug("Network: $message")
                }
            }

            level = LogLevel.INFO
        }
    }

    suspend fun fetchItem(id: Int): Item {
        return client.get(
            scheme = scheme,
            host = host,
            path = "/v0/item/${id}.json"
        )
    }

    suspend fun fetchTopItems(): List<Int> {
        return client.get(
            scheme = scheme,
            host = host,
            path = "/v0/topstories.json"
        )
    }
}