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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.ui.vm.MyVm
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Instant

@ExperimentalCoroutinesApi
@Composable
fun ListContent(
    scrollState: LazyListState,
    navigateTo: (Screen) -> Unit
) {
    val vm: MyVm = viewModel()
    val posts by vm.posts.collectAsState()
    val error by vm.errorMsg.collectAsState()

    if (posts.isEmpty()) {
        if (!error.isNullOrEmpty()) {
            Text("Error: $error")
        } else {
            Loading()
        }
    } else {
        val context = ContextAmbient.current
        PostsList(
            data = posts,
            scrollState,
            onSelectPost = { post ->
                vm.markPostViewed(post.id)

                val url = post.url
                if (url != null) {
                    launchBrowser(url, context)
                } else {
                    navigateTo(Screen.ViewOne(post))
                }
            },
            onSelectPostComment = { post ->
                vm.markPostCommentViewed(post.id)
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
                    style = MaterialTheme.typography.h6,
                    color = if (post.hasViewedPost == 1L) Color.Gray else MaterialTheme.colors.onBackground
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

                val imgCommentMod = if (post.hasViewedComments == 1L) {
                    Modifier.align(Alignment.CenterHorizontally).drawOpacity(0.5f)
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
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (post.hasViewedComments == 1L) Color.Gray else MaterialTheme.colors.onBackground
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
