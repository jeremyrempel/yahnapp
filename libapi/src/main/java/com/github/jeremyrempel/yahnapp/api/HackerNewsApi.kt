package com.github.jeremyrempel.yahnapp.api

import com.github.jeremyrempel.yahnapp.api.model.Item
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class HackerNewsApi(
    private val scheme: String = "https",
    private val host: String = "hacker-news.firebaseio.com",
    private val provideClient: suspend () -> HttpClient
) {

    suspend fun fetchItem(id: Long): Item {
        val client = provideClient()
        return client.get(
            scheme = scheme,
            host = host,
            path = "/v0/item/$id.json"
        )
    }

    suspend fun fetchTopItems(): List<Int> {
        val client = provideClient()
        return client.get(
            scheme = scheme,
            host = host,
            path = "/v0/topstories.json"
        )
    }
}
