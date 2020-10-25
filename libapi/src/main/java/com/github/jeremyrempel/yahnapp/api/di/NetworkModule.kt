package com.github.jeremyrempel.yahnapp.api.di

import android.content.Context
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.compression.ContentEncoding
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.header
import kotlinx.serialization.json.Json
import okhttp3.Cache
import timber.log.Timber

// todo daggerify
object NetworkModule {

    private const val CACHE_SIZE = 1024L * 1024L * 10L

    private fun provideCache(ctx: Context): Cache {
        return Cache(ctx.cacheDir, CACHE_SIZE)
    }

    private fun providesClient(cache: Cache): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    retryOnConnectionFailure(true)
                    cache(cache)
                }
            }
            install(JsonFeature) {
                val config = Json.Default
                serializer = KotlinxSerializer(config)
            }
            ContentEncoding {
                gzip()
            }
            defaultRequest {
                header("Connection", "close")
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.v("network: $message")
                    }
                }

                level = LogLevel.INFO
            }
        }
    }

    fun providesApi(context: Context): HackerNewsApi {
        val cache = provideCache(context)

        val client = providesClient(cache)
        return HackerNewsApi(client = client)
    }
}
