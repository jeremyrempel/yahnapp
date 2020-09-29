package com.github.jeremyrempel.yanhnapp.ui.screens

import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import java.util.Date

@Composable
fun ListContent(
    lce: Lce<List<Post>>,
    navigateTo: (Screen) -> Unit
) {
    when (lce) {
        is Lce.Content -> {
            val context = ContextAmbient.current
            PostsList(
                data = lce.data,
                onSelectPost = { post ->
                    if (post.url != null) {
                        launchBrowser(post.url, context)
                    } else {
                        navigateTo(Screen.ViewOne(post))
                    }
                },
                onSelectPostComment = { post ->
                    navigateTo(Screen.ViewComments(post))
                }
            )
        }
        is Lce.Loading -> {
            Loading()
        }
        is Lce.Error<*> -> {
            val err = lce as Lce.Error<List<Post>>
            Text(err.error.message ?: "Error")
        }
    }
}

fun launchBrowser(url: String, context: Context) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setDefaultShareMenuItemEnabled(true)
        .setUrlBarHidingEnabled(true)
        .build()
        .launchUrl(context, Uri.parse(url))
}

@Composable
fun PostsList(data: List<Post>, onSelectPost: (Post) -> Unit, onSelectPostComment: (Post) -> Unit) {
    LazyColumnFor(
        items = data
    ) { row ->
        PostRow(row, onSelectPost, onSelectPostComment)
    }
}

@Composable
fun PostRow(post: Post, onSelectPost: (Post) -> Unit, onSelectPostComment: (Post) -> Unit) {

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
                            text = post.domain,
                            style = MaterialTheme.typography.body2
                        )
                    }

                    val relativeDate =
                        DateUtils.getRelativeTimeSpanString(post.unixTimeMs, Date().time, 0)
                            .toString()
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
        PostsList(data = SampleData.posts, onSelectPost = {}, onSelectPostComment = {})
    }
}
