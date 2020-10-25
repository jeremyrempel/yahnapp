package com.github.jeremyrempel.yahnapp.api.interactor

import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class CommentsUseCase(
    private val db: HackerNewsDb,
    private val api: HackerNewsApi
) {

    @Suppress("DeferredResultUnused")
    suspend fun requestAndStoreComments(postId: Long) = coroutineScope {
        val kids = (api.fetchItem(postId).kids ?: emptyList())
        getCommentsByIds(kids, postId)
    }

    @Suppress("DeferredResultUnused")
    suspend fun getCommentsForPost(postId: Long): Flow<List<Comment>> {
        return db.selectCommentsByPost(postId)
    }

    suspend fun getCommentsForParent(parentCommentId: Long): Flow<List<Comment>> {
        return db.selectCommentsByParent(parentCommentId)
    }

    private suspend fun getCommentsByIds(commentIds: List<Long>, postId: Long, level: Int = 1) {
        coroutineScope {
            var cnt = 0

            commentIds
                .map {
                    async(Dispatchers.IO) {
                        api.fetchItem(it)
                    }
                }
                .map {
                    it.await()
                }
                .forEach { item ->
                    db.storePost(
                        id = item.id.toLong(),
                        username = item.by ?: "n/a",
                        unixTime = item.time,
                        content = item.text ?: "",
                        postId = postId,
                        parent = if (item.parent?.toLong() != postId) item.parent?.toLong() else null,
                        childrenCnt = item.kids?.size?.toLong() ?: 0,
                        order = cnt.toLong()
                    )

                    cnt++

                    getCommentsByIds(item.kids ?: listOf(), postId, level + 1)
                }
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
