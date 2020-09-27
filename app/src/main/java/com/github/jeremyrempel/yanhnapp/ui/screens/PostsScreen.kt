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
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.models.getSample
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import java.util.Date

@Composable
fun PostsList(data: List<Post>, callback: (post: Post) -> Unit) {
    LazyColumnFor(
        items = data,
        modifier = Modifier.padding(5.dp)
    ) { row ->
        PostRow(row, callback)
    }
}

@Composable
fun PostRow(post: Post, callback: (Post) -> Unit) {

    Column(modifier = Modifier.fillMaxWidth()) {
        Row {

            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .padding(end = 10.dp)
                    .clickable(onClick = { callback(post) }),
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
                modifier = Modifier.weight(0.1f).align(Alignment.CenterVertically)
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

        Divider(modifier = Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 15.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PostsRowPreview() {
    YetAnotherHNAppTheme(darkTheme = false) {
        PostsList(data = getSample(), callback = {})
    }
}
