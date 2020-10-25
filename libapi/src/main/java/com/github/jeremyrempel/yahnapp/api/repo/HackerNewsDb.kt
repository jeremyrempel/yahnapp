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
import timber.log.Timber

class HackerNewsDb(
    context: Application
) {

    // first access will perform upgrade on open
    private val driver by lazy {
        AndroidSqliteDriver(Database.Schema, context, "yahn.db")
    }
    private val database by lazy {
        Database(driver)
    }

    suspend fun storePost(post: Post) = coroutineScope {
        val postDb = database.postQueries.selectPostById(post.id).executeAsOneOrNull()

        if (postDb != null) {
            // only update post if something we care about has changed
            if (postDb.points != post.points || postDb.commentsCnt != post.commentsCnt) {
                database.postQueries.update(
                    post.points,
                    post.commentsCnt,
                    post.id
                )
            }
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

    suspend fun selectCommentsByPost(id: Long) = coroutineScope {
        database.commentQueries.selectCommentsByPost(id).asFlow().mapToList()
    }

    suspend fun selectCommentsByParent(id: Long) = coroutineScope {
        database.commentQueries.selectCommentsByParent(id).asFlow().mapToList()
    }

    suspend fun storePost(
        id: Long,
        username: String,
        unixTime: Long,
        content: String,
        postId: Long,
        parent: Long?,
        childrenCnt: Long,
        order: Long
    ) = coroutineScope {
        withContext(Dispatchers.Default) {

            val commentDb = database.commentQueries.selectById(id).executeAsOneOrNull()

            if (commentDb != null) {
                // incremental update
                if (childrenCnt != commentDb.childrenCnt || content != commentDb.content) {
                    Timber.v("Cache miss, updating comment: $id, $username, $content, $childrenCnt")
                    database.commentQueries.updateChildrenContent(
                        content,
                        childrenCnt,
                        id
                    )
                } else {
                    Timber.v("Cache hit, not updating comment: $id")
                }
            } else {
                Timber.v("Cache miss, inserting new comment: $id, $username, $content")
                database.commentQueries.insert(
                    id = id,
                    username = username,
                    unixTime = unixTime,
                    content = content,
                    postid = postId,
                    parent = parent,
                    childrenCnt = childrenCnt,
                    sortorder = order
                )
            }
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

    fun close() {
        driver.close()
    }
}
