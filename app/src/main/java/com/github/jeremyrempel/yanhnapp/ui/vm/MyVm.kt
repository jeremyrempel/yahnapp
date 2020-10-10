package com.github.jeremyrempel.yanhnapp.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.Item
import com.github.jeremyrempel.yahnapp.api.model.Comment
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import java.time.Instant

class MyVm(application: Application) : AndroidViewModel(application) {

    val posts = MutableLiveData<List<Post>>()
    val comments = MutableLiveData<List<Comment>>()
    private var currentPost: Long = -1

    private val db = lazy { HackerNewsDb(application) }
    private val api = lazy { HackerNewsApi(context = application, networkDebug = Timber::d) }

    fun requestPosts() {
        viewModelScope.launch {
            // fetchAndStore(api, db)
            fetchAndStore()

            db.value.selectAllPostsByRank()
                .collectLatest {
                    // don't update ui on every update
                    delay(100)
                    posts.postValue(it)
                }
        }
    }

    fun requestComments(id: Long) {
        if (currentPost != id) {
            currentPost = id
            comments.value = emptyList()

            viewModelScope.launch {
                val result = fetchCommentsForPost(id)
                comments.postValue(result)
            }
        }
    }

    /**
     * Given list of trees, fetch all leafs and metadata
     */
    private suspend fun fetchCommentsForPost(postId: Long) = coroutineScope {
        val api = api.value

        // dfs
        suspend fun getCommentsByIds(commentIds: List<Long>, level: Int = 1): List<Comment> {
            val commentList = commentIds
                .map {
                    val item = api.fetchItem(it)

                    // recurse
                    val commentChildren = getCommentsByIds(item.kids ?: listOf(), level + 1)

                    Comment(
                        item.by ?: "",
                        item.time * 1000,
                        item.text ?: "",
                        commentChildren
                    )
                }

            if (level == 1) {
                comments.postValue(commentList)
            }

            return commentList
        }

        val kids = (api.fetchItem(postId).kids ?: emptyList())

        // dfs start
        withContext(Dispatchers.IO) {
            getCommentsByIds(kids)
        }
    }

    private fun Item.toPost(): Post {
        val item = this

        val domain = if (item.url != null) {
            URL(item.url).toURI().authority.replaceFirst("www.", "")
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
            now
        )
    }

    /**
     * Fetch and store. If api failures error will be captured
     */
    private suspend fun fetchAndStore() = viewModelScope.launch {
        try {
            withContext(Dispatchers.IO) {
                val api = api.value
                api.fetchTopItems()
                    .map { it.toLong() }
                    .also { topItems ->
                        db.value.replaceTopPosts(topItems)

                        topItems.map { itemId ->
                            api
                                .fetchItem(itemId)
                                .toPost()
                                .also { p -> db.value.store(p) }
                        }
                    }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (db.isInitialized()) db.value.close()
    }
}
