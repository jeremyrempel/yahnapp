package com.github.jeremyrempel.yahnapp.api.di

import android.app.Application
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @InternalApi
    fun providesDriver(context: Application): AndroidSqliteDriver {
        // first access will upgrade
        return AndroidSqliteDriver(Database.Schema, context, "yahn.db")
    }

    @Provides
    @InternalApi
    fun providesDatabase(driver: AndroidSqliteDriver): Database {
        return Database(driver)
    }

    @Provides
    @Singleton
    fun providesHnDb(database: Database): HackerNewsDb {
        return HackerNewsDb(database)
    }
}
