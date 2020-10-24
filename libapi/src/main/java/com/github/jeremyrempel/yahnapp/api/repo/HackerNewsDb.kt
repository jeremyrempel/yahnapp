package com.github.jeremyrempel.yahnapp.api.repo

import android.app.Application
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahn.Pref
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HackerNewsDb(
    context: Application
) {

    private val driver = AndroidSqliteDriver(Database.Schema, context, "yahn.db")
    private val database = Database(driver)

    fun close() {
        driver.close()
    }

    suspend fun store(post: Post) = coroutineScope {
        val postDb = database.postQueries.selectPostById(post.id).executeAsOneOrNull()

        if (postDb != null) {
            database.postQueries.update(
                post.points,
                post.commentsCnt,
                post.id
            )
        } else {
            database.postQueries.insert(
                post.id,
                post.title,
                post.text,
                post.domain,
                post.url,
                post.points,
                post.unixTime,
                post.commentsCnt
            )
        }
    }

    suspend fun selectAllPostsByRank(): Flow<List<Post>> = coroutineScope {
        database.topPostsQueries.selectPostsByRank().asFlow().mapToList()
    }

    suspend fun replaceTopPosts(topPosts: List<Long>) = coroutineScope {
        database.topPostsQueries.truncateTopPosts()
        topPosts.forEachIndexed { rank, postId ->
            database.topPostsQueries.insertTopPost(postId, rank.toLong())
        }
    }

    suspend fun markPostAsRead(id: Long) = coroutineScope {
        database.postQueries.markPostAsViewed(id)
    }

    suspend fun markCommentRead(id: Long) = coroutineScope {
        database.postQueries.markPostCommentAsViewed(id)
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
