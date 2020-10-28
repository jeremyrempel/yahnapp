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
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onActive
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yahnapp.api.interactor.PostsUseCase
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.time.Instant

@ExperimentalCoroutinesApi
@Composable
fun ListContent(
    scrollState: LazyListState,
    useCase: PostsUseCase,
    commentsUseCase: CommentsUseCase,
    navigateTo: (Screen) -> Unit
) {
    val posts by useCase.selectAllPostsByRank().collectAsState(initial = emptyList())
    var loadProgress by remember { mutableStateOf(0.0f) }

    var errorMsgVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedTask {
        try {
            useCase.requestAndStorePosts {
                loadProgress = it
            }
        } catch (e: Exception) {
            Timber.e(e)
            error = e.localizedMessage
            errorMsgVisible = true
        }
    }

    Column {
        if (loadProgress < 1 && error == null) {
            LinearProgressIndicator(
                loadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (error != null && errorMsgVisible) {
            val errorTxt = error!!
            Text(errorTxt)
            TextButton(onClick = { errorMsgVisible = false }) {
                Text("Dismiss")
            }
        }

        val context = ContextAmbient.current
        PostsList(
            data = posts,
            scrollState,
            onSelectPost = { post ->
                useCase.markPostViewed(post.id)

                val url = post.url
                if (url != null) {
                    launchBrowser(url, context)
                } else {
                    commentsUseCase.markPostCommentViewed(post.id)
                    navigateTo(Screen.ViewComments(post))
                }
            },
            onSelectPostComment = { post ->
                commentsUseCase.markPostCommentViewed(post.id)
                navigateTo(Screen.ViewComments(post))
            }
        )
    }
}

@Composable
fun PostsList(
    data: List<Post>,
    scrollState: LazyListState,
    onSelectPost: (Post) -> Unit,
    onSelectPostComment: (Post) -> Unit
) {
    LazyColumnForIndexed(
        items = data,
        state = scrollState
    ) { index, row ->
        PostRow(row, onSelectPost, onSelectPostComment)

        if (data.size - 1 == index) {
            onActive {
                // todo implement load more
                Timber.d("reached end. load more")
            }
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
fun PostRow(post: Post, onSelectPost: (Post) -> Unit, onSelectPostComment: (Post) -> Unit) {

    val readOpacity = 0.4f

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
                    style = MaterialTheme.typography.h6,
                    modifier = if (post.hasViewedPost == 1L) Modifier.drawOpacity(readOpacity) else Modifier
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
                        onClick = {
                            onSelectPostComment(post)
                        }
                    ),
            ) {

                val imgCommentMod = if (post.hasViewedComments == 1L) {
                    Modifier.align(Alignment.CenterHorizontally).drawOpacity(readOpacity)
                } else {
                    Modifier.align(Alignment.CenterHorizontally)
                }

                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_comment_24),
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
                    modifier = imgCommentMod
                )
                Text(
                    text = post.commentsCnt.toString(),
                    modifier = imgCommentMod
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
            onSelectPostComment = {}
        )
    }
}
