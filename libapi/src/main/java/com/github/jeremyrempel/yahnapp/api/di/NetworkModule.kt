package com.github.jeremyrempel.yahnapp.api.di

import android.app.Application
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE = 1024L * 1024L * 10L

    @Provides
    @InternalApi
    fun provideCache(ctx: Application): Cache {
        return Cache(ctx.cacheDir, CACHE_SIZE)
    }

    @Provides
    @InternalApi
    fun providesClient(@InternalApi cache: Cache): HttpClient {
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

    @Provides
    @Singleton
    fun providesApi(@InternalApi client: HttpClient): HackerNewsApi {
        return HackerNewsApi(client = client)
    }
}
