package com.github.jeremyrempel.yahnapp.api

import com.github.jeremyrempel.yahnapp.api.model.Item
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HackerNewsApi(
    private val scheme: String = "https",
    private val host: String = "hacker-news.firebaseio.com",
    private val provideClient: suspend () -> HttpClient
) {

    suspend fun fetchItem(id: Long): Item {
        return withContext(Dispatchers.Default) {
            val client = provideClient()
            client.get(
                scheme = scheme,
                host = host,
                path = "/v0/item/$id.json"
            )
        }
    }

    suspend fun fetchTopItems(): List<Int> {
        return withContext(Dispatchers.Default) {
            val client = provideClient()
            client.get(
                scheme = scheme,
                host = host,
                path = "/v0/topstories.json"
            )
        }
    }
}
