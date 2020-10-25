package com.github.jeremyrempel.yanhnapp.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.di.NetworkModule
import com.github.jeremyrempel.yahnapp.api.model.Item
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL
import java.time.Instant

@ExperimentalCoroutinesApi
class MyVm(application: Application) : AndroidViewModel(application) {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    // todo daggerify and extract out
    val db by lazy { HackerNewsDb(application) }
    val api: HackerNewsApi by lazy { NetworkModule.providesApi(application) }

    @Suppress("DeferredResultUnused")
    fun requestAndStorePosts() {
        viewModelScope.launch {

            async(Dispatchers.IO) {
                fetchAndStore()
            }

            // todo is there a better way to wire this together?
            async {
                db.selectAllPostsByRank()
                    .collectLatest {
                        // don't update ui on every update
                        delay(100)
                        if (it.size > 20) {
                            _posts.value = it
                        }
                    }
            }
        }
    }

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

    /**
     * Fetch and store
     */
    private suspend fun fetchAndStore() {
        _errorMsg.value = null

        try {
            api.fetchTopItems()
                .map { it.toLong() }
                .also { topItems ->
                    db.replaceTopPosts(topItems)
                    topItems.map { itemId ->
                        api
                            .fetchItem(itemId)
                            .toPost()
                            .also { p -> db.storePost(p) }
                    }
                }
        } catch (e: Exception) {
            _errorMsg.value = e.message
            Timber.e(e)
        }
    }

    @Suppress("DeferredResultUnused")
    fun markPostViewed(id: Long) {
        viewModelScope.launch {
            async(Dispatchers.IO) {
                db.markPostAsRead(id)
            }
        }
    }

    @Suppress("DeferredResultUnused")
    fun markPostCommentViewed(id: Long) {
        viewModelScope.launch {
            async(Dispatchers.IO) {
                db.markCommentRead(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        db.close()
    }
}
