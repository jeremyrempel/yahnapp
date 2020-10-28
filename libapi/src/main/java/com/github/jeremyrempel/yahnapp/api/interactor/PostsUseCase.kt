package com.github.jeremyrempel.yahnapp.api.interactor

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.model.Item
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import java.time.Instant
import javax.inject.Inject

class PostsUseCase @Inject constructor(
    private val db: HackerNewsDb,
    private val api: HackerNewsApi
) {

    companion object {
        const val PAGE_SIZE = 100
        const val FETCH_TIME_SECONDS = 600L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun requestAndStorePosts(progressUpdate: (Float) -> Unit) = coroutineScope {
        val lastFetch = Instant.ofEpochSecond(db.getPref("lastfetch")?.valueInt ?: 0L)
        val fetchLimit = Instant.now().minusSeconds(FETCH_TIME_SECONDS)

        Timber.d("last fetch: $lastFetch, fetchLimit: $fetchLimit")

        if (lastFetch.isBefore(fetchLimit)) {
            withContext(Dispatchers.IO) {
                api.fetchTopItems()
            }
                .take(PAGE_SIZE)
                .map { it.toLong() }
                .also { topItems ->
                    db.replaceTopPosts(topItems)

                    topItems.map { itemId ->
                        async(Dispatchers.IO) {
                            api
                                .fetchItem(itemId)
                        }
                    }.forEachIndexed { idx, result ->
                        val post = result.await().toPost()

                        withContext(Dispatchers.Default) {
                            db.storePost(post)
                        }

                        val progress = idx.toFloat() / topItems.size.toFloat()
                        progressUpdate(progress)
                    }
                }
            db.savePref("lastfetch", Instant.now().epochSecond)
        }

        progressUpdate(1.0f)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Item.toPost(): Post {
        val item = this

        val domain = if (item.url != null) {

            // malformed url. such as https:///mydomain.com just print it as is
            if (URL(item.url).toURI().authority == null) {
                item.url
            } else {
                URL(item.url).toURI().authority.replaceFirst("www.", "")
            }
        } else {
            null
        }

        val now = Instant.now().epochSecond
        return Post(
            id = item.id.toLong(),
            title = item.title ?: "",
            text = item.text,
            domain = domain,
            url = item.url,
            points = 0,
            unixTime = item.time,
            commentsCnt = item.descendants?.toLong() ?: 0,
            now,
            now,
            Instant.now().epochSecond,
            Instant.now().epochSecond
        )
    }

    fun selectAllPostsByRank(): Flow<List<Post>> {
        return db
            .selectAllPostsByRank()
            .flowOn(Dispatchers.Default)
    }

    fun markPostViewed(id: Long) {
        // todo scope
        GlobalScope.launch(Dispatchers.IO) {
            db.markPostAsRead(id)
        }
    }
}
