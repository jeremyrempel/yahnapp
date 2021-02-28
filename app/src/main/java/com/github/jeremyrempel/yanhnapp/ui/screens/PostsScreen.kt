package com.github.jeremyrempel.yanhnapp.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant

@Composable
fun ListContent(
    scrollState: LazyListState,
    selectAllPostsByRank: () -> Flow<List<Post>>,
    requestAndStorePosts: suspend ((Float) -> Unit) -> Unit,
    markPostViewed: (Long) -> Unit,
    markCommentViewed: (postId: Long) -> Unit,
    navigateTo: (Screen) -> Unit
) {
    val posts by selectAllPostsByRank().collectAsState(initial = emptyList())
    var loadProgress by remember { mutableStateOf(0.0f) }

    var errorMsgVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(1) {
        try {
            requestAndStorePosts {
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

        if (posts.isNotEmpty()) {

            val context = LocalContext.current
            PostsList(
                data = posts,
                scrollState,
                onSelectPost = { post ->
                    markPostViewed(post.id)

                    val url = post.url
                    if (url != null) {
                        launchBrowser(url, context)
                    } else {
                        markCommentViewed(post.id)
                        navigateTo(Screen.ViewComments(post))
                    }
                },
                onSelectPostComment = { post ->
                    markCommentViewed(post.id)
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
    LazyColumn(
        state = scrollState
    ) {
        items(
            count = data.size,
            itemContent = { row ->
                PostRow(data[row], onSelectPost, onSelectPostComment)
            }
        )
    }
}

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 5.dp, end = 5.dp)

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
                    modifier = if (post.hasViewedPost == 1L) Modifier.alpha(readOpacity) else Modifier
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
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .alpha(readOpacity)
                } else {
                    Modifier.align(Alignment.CenterHorizontally)
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_comment_24),
                    contentDescription = stringResource(id = R.string.comments_title),
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
                    modifier = imgCommentMod
                )
                Text(
                    text = post.commentsCnt.toString(),
                    modifier = imgCommentMod
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
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
