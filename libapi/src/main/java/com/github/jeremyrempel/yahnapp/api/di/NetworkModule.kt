package com.github.jeremyrempel.yahnapp.api.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.github.jeremyrempel.yahnapp.api.CACHE_TIME_MINUTES
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE = 1024L * 1024L * 10L

    @Provides
    @Singleton
    @InternalApi
    fun provideCache(ctx: Application): Cache {
        checkMainThread()
        return Cache(ctx.cacheDir, CACHE_SIZE)
    }

    @Provides
    @Singleton
    @InternalApi
    fun providesOkHttp(@InternalApi cache: Cache, ctx: Application): OkHttpClient {
        checkMainThread()

        val cacheInterceptor = Interceptor { chain ->

            val cacheControl = CacheControl.Builder()
                .maxAge(CACHE_TIME_MINUTES, TimeUnit.MINUTES)
                .build()

            val response = chain.proceed(chain.request()).newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", cacheControl.toString())
                .build()

            response
        }

        val chuckerInterceptor = ChuckerInterceptor.Builder(ctx)
            .collector(
                ChuckerCollector(
                    context = ctx,
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_HOUR
                )
            )
            .build()

        val loggingInterceptor = Interceptor { chain ->
            Timber.v("request: ${chain.request().url}")
            chain.proceed(chain.request())
        }

        val closeInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Connection", "close")
                .build()

            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .retryOnConnectionFailure(true)
            .addInterceptor(cacheInterceptor)
            .addInterceptor(closeInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @InternalApi
    fun providesClient(@InternalApi okHttpClient: OkHttpClient): HttpClient {
        checkMainThread()
        return HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }
            install(JsonFeature) {
                val config = Json.Default
                serializer = KotlinxSerializer(config)
            }
        }
    }

    @Provides
    fun providesApi(@InternalApi client: Provider<HttpClient>): HackerNewsApi {
        return HackerNewsApi {
            withContext(Dispatchers.Default) {
                client.get()
            }
        }
    }
}
