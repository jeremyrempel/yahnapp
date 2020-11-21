package com.github.jeremyrempel.yahnapp.api.interactor

import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.PAGE_SIZE
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class CommentsUseCase @Inject constructor(
    private val db: HackerNewsDb,
    private val api: HackerNewsApi
) {

    fun getCommentsForPost(postId: Long): Flow<List<Comment>> {
        return db.selectCommentsByPost(postId)
    }

    suspend fun getCommentsForParent(parentCommentId: Long): Flow<List<Comment>> {
        return db.selectCommentsByParent(parentCommentId)
    }

    suspend fun requestAndStoreComments(postId: Long, progressUpdate: (Float) -> Unit) {
        coroutineScope {

            val kids = (api.fetchItem(postId).kids ?: emptyList())

            val total = kids.size.toFloat()
            var currentLoad = 1
            val fetcher = CommentsFetcher(db, api)

            fetcher.get(kids, postId) {
                currentLoad++
                val update = currentLoad / total
                progressUpdate(update)
            }

            progressUpdate(100f)
        }
    }

    private class CommentsFetcher(
        private val db: HackerNewsDb,
        private val api: HackerNewsApi,
        private var data: MutableList<Comment> = mutableListOf(),
        private val now: Long = Date().time
    ) {

        suspend fun get(
            commentIds: List<Long>,
            postId: Long,
            level: Int = 1,
            onBranchCompleted: () -> Unit
        ) {
            getCommentsByIds(commentIds, postId, level, onBranchCompleted)

            db.storeComments(data)
        }

        private suspend fun getCommentsByIds(
            commentIds: List<Long>,
            postId: Long,
            level: Int = 1,
            onBranchCompleted: () -> Unit,
        ) {
            coroutineScope {
                var cnt = 0

                val result = commentIds
                    .map {
                        async(Dispatchers.IO) {
                            api.fetchItem(it)
                        }
                    }
                    .awaitAll()
                    .forEach { item ->
                        val comment = Comment(
                            id = item.id.toLong(),
                            username = item.by ?: "n/a",
                            unixTime = item.time,
                            content = item.text ?: "",
                            postid = postId,
                            parent = if (item.parent?.toLong() != postId) item.parent?.toLong() else null,
                            childrenCnt = item.kids?.size?.toLong() ?: 0,
                            sortorder = cnt.toLong(),
                            created = now,
                            lastUpdated = now
                        )
                        data.add(comment)

                        // limit
                        if (data.size >= PAGE_SIZE) {
                            return@coroutineScope
                        }

                        cnt++

                        getCommentsByIds(
                            item.kids ?: listOf(),
                            postId,
                            level + 1,
                            onBranchCompleted
                        )

                        if (level == 1) {
                            onBranchCompleted()
                        }
                    }
            }
        }
    }

    fun markPostCommentViewed(id: Long) {
        // todo scope
        GlobalScope.launch {
            db.markCommentRead(id)
        }
    }

    // kotlin compiler bug
    // private suspend fun fetchAndStoreCommentsForPost(postId: Long) {
    //
    //     suspend fun getCommentsByIds(commentIds: List<Long>, level: Int = 1) {
    //         commentIds
    //             .forEach { commentId ->
    //                 // val kids = api.fetchItem(commentId).kids ?: listOf()
    //                 val kids = listOf(4L, 5L)
    //
    //                 // recurse
    //                 getCommentsByIds(kids, level + 1)
    //             }
    //     }
    //
    //     // val kids = (api.fetchItem(postId).kids ?: emptyList())
    //     val kids = listOf(1L,2L,3L)
    //
    //     // dfs start
    //     getCommentsByIds(kids)
    // }
}
