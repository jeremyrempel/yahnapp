package com.github.jeremyrempel.yanhnapp.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.Item
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.YahnApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import java.time.Instant

class MyVm(application: Application) : AndroidViewModel(application) {

    val posts = MutableLiveData<List<Post>>()
    private val db = lazy { HackerNewsDb(application) }

    fun start() {
        viewModelScope.launch {
            // fetchAndStore(api, db)
            fetchAndStoreNew()

            db.value.selectAllPostsByRank()
                .collectLatest {
                    // don't update ui on every update
                    delay(100)
                    posts.postValue(it)
                }
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

    private suspend fun fetchAndStoreNew() = viewModelScope.launch {
        val application = getApplication<YahnApplication>()

        try {
            withContext(Dispatchers.IO) {
                val api = HackerNewsApi(context = application, networkDebug = Timber::d)

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
