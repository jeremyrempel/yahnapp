package com.github.jeremyrempel.yahnapp.api.repo

import android.content.Context
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HackerNewsDb(
    context: Context
) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "mydb.db")
    private val database = Database(driver)

    suspend fun store(post: Post) {
        withContext(Dispatchers.IO) {
            database.postQueries.insert(post)
        }
    }
}
