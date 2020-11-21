package com.github.jeremyrempel.yahnapp.api.di

import android.app.Application
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun providesDriver(context: Application): AndroidSqliteDriver {
        // first access will upgrade
        checkMainThread()
        return AndroidSqliteDriver(Database.Schema, context, "yahn.db")
    }

    @Provides
    @Singleton
    fun providesDatabase(driver: AndroidSqliteDriver): Database {
        checkMainThread()
        return Database(driver)
    }

    @Provides
    fun providesHnDb(database: Provider<Database>): HackerNewsDb {
        return HackerNewsDb {
            withContext(Dispatchers.Default) {
                database.get()
            }
        }
    }
}
