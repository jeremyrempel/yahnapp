package com.github.jeremyrempel.yanhnapp.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.Post
import com.github.jeremyrempel.yanhnapp.getSample

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
    Row(modifier = Modifier.clickable(onClick = { callback(post) })) {
        Text(String.format("%d.", post.rank))
        Column(modifier = Modifier.padding(horizontal = 5.dp)) {
            Text(
                text = post.title,
                style = TextStyle(
                    fontSize = TextUnit.Sp(15)
                )
            )

            Row {
                Text(
                    text = String.format(
                        "%s | %d points %d hours ago | %d comments",
                        post.domain, post.points, post.ageHours, post.commentsCnt
                    ),
                    color = Color.Gray,
                    style = TextStyle(
                        fontSize = TextUnit.Sp(10)
                    )
                )
            }

            Spacer(modifier = Modifier.preferredHeight(3.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostsRow() {
    PostRow(post = getSample()[0], callback = {})
}
