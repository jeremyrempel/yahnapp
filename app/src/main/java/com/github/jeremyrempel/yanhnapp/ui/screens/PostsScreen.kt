package com.github.jeremyrempel.yanhnapp.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.Item
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.io.IOException
import java.net.URL
import java.time.Instant

// todo move into a repository

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

private suspend fun fetchAndStore(api: HackerNewsApi, db: HackerNewsDb) = coroutineScope {
    // fetch from network and store
    try {
        val now = Instant.now().epochSecond
        val expiration = now - (60 * 5) // 5 minutes ago

        val lastFetch = db.getPref("lastfetch")?.valueInt ?: 0
        if (lastFetch < expiration) {
            Timber.d("Last fetch more older than 5m, fetching again")
            db.savePref("lastfetch", now)

            Timber.d("fetching top items")
            val topItems = api.fetchTopItems().map { it.toLong() }
            db.replaceTopPosts(topItems)

            topItems.map { id ->
                // query db async
                val job = async { db.selectPostById(id) }
                Pair(id, job)
            }.map { p ->
                Pair(p.first, p.second.await())
            }.map { postDb ->
                // fetch from db if not already stored
                async(Dispatchers.IO) {
                    if (postDb.second == null) {
                        db.store(api.fetchItem(postDb.first).toPost())
                    }
                }
            }.map { it.await() }
        } else {
            Timber.d("Last fetch within last 5m, skipping fetch")
        }
    } catch (e: IOException) {
        Timber.e(e)
    }
}

@Composable
fun ListContent(
    api: HackerNewsApi,
    db: HackerNewsDb,
    scrollState: LazyListState,
    navigateTo: (Screen) -> Unit
) {
    val result = remember { mutableStateOf<Lce<List<Post>>>(Lce.Loading()) }

    if (result.value is Lce.Loading) {

        launchInComposition {

            fetchAndStore(api, db)

            // query from db
            try {
                val data = db.selectAllPostsByRank()
                result.value = Lce.Content(data)
            } catch (e: Exception) {
                Timber.e(e)
                result.value = Lce.Error(e)
            }
        }
    }

    when (result.value) {
        is Lce.Loading -> Loading()
        is Lce.Error -> {
            val errorMsg = (result.value as Lce.Error).error.message ?: "Unknown Error"
            Text(errorMsg)
        }
        else -> {
            val context = ContextAmbient.current
            val data = (result.value as Lce.Content).data

            PostsList(
                data = data,
                scrollState,
                onSelectPost = { post ->

                    val url = post.url
                    if (url != null) {
                        launchBrowser(url, context)
                    } else {
                        navigateTo(Screen.ViewOne(post))
                    }
                },
                onSelectPostComment = { post ->
                    navigateTo(Screen.ViewComments(post))
                }
            )
        }
    }
}

@Composable
fun PostsList(
    data: List<Post>,
    scrollState: LazyListState,
    onSelectPost: (Post) -> Unit,
    onSelectPostComment: (Post) -> Unit
) {
    LazyColumnFor(
        items = data,
        state = scrollState
    ) { row ->
        PostRow(row, onSelectPost, onSelectPostComment)
    }
}

@Composable
fun PostRow(post: Post, onSelectPost: (Post) -> Unit, onSelectPostComment: (Post) -> Unit) {

    val relativeDate =
        remember(post) {
            DateUtils.getRelativeTimeSpanString(
                post.unixTime * 1000,
                Instant.now().toEpochMilli(),
                0
            ).toString()
        }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 5.dp, end = 5.dp)

    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .padding(end = 10.dp)
                    .clickable(onClick = { onSelectPost(post) }),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.h6
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    if (post.domain != null) {
                        Text(
                            text = post.domain!!,
                            style = MaterialTheme.typography.body2
                        )
                    }

                    Text(
                        text = relativeDate,
                        style = MaterialTheme.typography.body2,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.1f)
                    .align(Alignment.CenterVertically)
                    .clickable(
                        onClick = { onSelectPostComment(post) }
                    ),
            ) {
                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_comment_24),
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = post.commentsCnt.toString(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Divider(modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PostsRowPreview() {
    YetAnotherHNAppTheme(darkTheme = false) {
        PostsList(
            data = SampleData.posts,
            rememberLazyListState(), onSelectPost = {},
            onSelectPostComment = {})
    }
}
