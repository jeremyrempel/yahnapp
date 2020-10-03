package com.github.jeremyrempel.yahnapp.api.repo

import android.content.Context
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahn.Pref
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HackerNewsDb(
    context: Context
) {
    private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "yahn.db")
    private val database = Database(driver)

    suspend fun store(post: Post) = coroutineScope {
        launch(Dispatchers.IO) {
            database.postQueries.insert(post)
        }
    }

    suspend fun selectAll(): List<Post> = coroutineScope {
        withContext(Dispatchers.IO) {
            database.postQueries.selectPosts().executeAsList()
        }
    }

    suspend fun getPref(key: String): Pref? = coroutineScope {
        withContext(Dispatchers.IO) {
            database.prefsQueries.get(key).executeAsOneOrNull()
        }
    }

    suspend fun savePref(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            if (database.prefsQueries.get(key).executeAsOneOrNull() != null) {
                database.prefsQueries.updateInt(value, key)
            } else {
                database.prefsQueries.insertInt(key, value)
            }
        }
    }
}
