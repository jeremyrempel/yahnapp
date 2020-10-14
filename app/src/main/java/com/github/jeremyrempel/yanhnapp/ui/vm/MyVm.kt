package com.github.jeremyrempel.yanhnapp.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.model.Comment
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
import java.io.IOException
import java.net.URL
import java.time.Instant

@ExperimentalCoroutinesApi
class MyVm(application: Application) : AndroidViewModel(application) {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    private var currentPost: Long = -1

    private val db = lazy { HackerNewsDb(application) }
    private val api = lazy { HackerNewsApi(networkDebug = Timber::d) }

    fun requestPosts() {
        viewModelScope.launch {

            async(Dispatchers.IO) {
                fetchAndStore()
            }

            // todo is there a better way to wire this together?
            async {
                db.value.selectAllPostsByRank()
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

    fun requestComments(id: Long) {
        if (currentPost != id) {
            currentPost = id
            _comments.value = emptyList()

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    _errorMsg.value = null
                    val result = fetchCommentsForPost(id)
                    _comments.value = result
                } catch (e: IOException) {
                    _errorMsg.value = e.message
                    Timber.e(e)
                }
            }
        }
    }

    /**
     * Given list of trees, fetch all leafs and metadata
     */
    private suspend fun fetchCommentsForPost(postId: Long): List<Comment> {
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
                _comments.value = commentList
            }

            return commentList
        }

        val kids = (api.fetchItem(postId).kids ?: emptyList())

        // dfs start
        return getCommentsByIds(kids)
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
     * Fetch and store
     */
    private suspend fun fetchAndStore() {
        _errorMsg.value = null

        try {
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
        } catch (e: Exception) {
            _errorMsg.value = e.message
            Timber.e(e)
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (db.isInitialized()) db.value.close()
    }
}
