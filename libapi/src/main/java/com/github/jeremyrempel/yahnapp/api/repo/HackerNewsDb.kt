package com.github.jeremyrempel.yahnapp.api.repo

import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahn.Pref
import com.github.jeremyrempel.yanhnapp.lib.Database
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HackerNewsDb(
    private val provideDatabase: suspend () -> Database
) {

    fun selectAllPostsByRank(): Flow<List<Post>> {
        return flow {
            val db = provideDatabase()
            emitAll(db.topPostsQueries.selectPostsByRank().asFlow().mapToList())
        }.flowOn(Dispatchers.Default)
    }

    fun selectCommentsByPost(id: Long): Flow<List<Comment>> {
        return flow {
            val db = provideDatabase()
            emitAll(db.commentQueries.selectCommentsByPost(id).asFlow().mapToList())
        }.flowOn(Dispatchers.Default)
    }

    suspend fun storePosts(posts: List<Post>) = coroutineScope {
        launch(Dispatchers.Default) {
            val database = provideDatabase()

            database.postQueries.transaction {
                posts.forEach { post ->
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
            }
        }
    }

    suspend fun replaceTopPosts(topPosts: List<Long>) = coroutineScope {
        launch(Dispatchers.Default) {
            val database = provideDatabase()

            database.topPostsQueries.transaction {
                database.topPostsQueries.truncateTopPosts()
                topPosts.forEachIndexed { rank, postId ->
                    database.topPostsQueries.insertTopPost(postId, rank.toLong())
                }
            }
        }
    }

    suspend fun markPostAsRead(id: Long) = coroutineScope {
        provideDatabase().postQueries.markPostAsViewed(id)
    }

    suspend fun markCommentRead(id: Long) = coroutineScope {
        provideDatabase().postQueries.markPostCommentAsViewed(id)
    }

    suspend fun selectCommentsByParent(id: Long) = coroutineScope {
        val database = provideDatabase()
        database.commentQueries.selectCommentsByParent(id).asFlow().mapToList()
    }

    suspend fun storeComments(comments: List<Comment>) = coroutineScope {
        launch(Dispatchers.Default) {
            val database = provideDatabase()
            database.commentQueries.transaction {
                comments.forEach {
                    storeComment(
                        database,
                        it.id,
                        it.username,
                        it.unixTime,
                        it.content,
                        it.postid,
                        it.parent,
                        it.childrenCnt,
                        it.sortorder
                    )
                }
            }
        }
    }

    private fun storeComment(
        database: Database,
        id: Long,
        username: String,
        unixTime: Long,
        content: String,
        postId: Long,
        parent: Long?,
        childrenCnt: Long,
        order: Long
    ) {
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

    suspend fun getPref(key: String): Pref? = coroutineScope {
        withContext(Dispatchers.Default) {
            provideDatabase().prefsQueries.get(key).executeAsOneOrNull()
        }
    }

    suspend fun savePref(key: String, value: Long) {
        withContext(Dispatchers.Default) {
            val database = provideDatabase()
            if (database.prefsQueries.get(key).executeAsOneOrNull() != null) {
                database.prefsQueries.updateInt(value, key)
            } else {
                database.prefsQueries.insertInt(key, value)
            }
        }
    }
}
