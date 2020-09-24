package com.github.jeremyrempel.yanhnapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

@Composable
fun SingleComment(comment: Comment, modifier: Modifier) {
    Column(
        modifier = modifier.padding(5.dp)
    ) {
        Row(
            modifier = modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.subtitle1,
                modifier = modifier.padding(end = 10.dp)
            )
            Text(
                text = "${comment.ageHours} hours ago", // todo make string resource
                style = MaterialTheme.typography.subtitle1
            )
        }
        Text(
            text = comment.content,
            style = MaterialTheme.typography.body1
        )

        Row(
            modifier = modifier.align(Alignment.End)
        ) {
            if (comment.hasMore > 0) {
                Text(
                    text = comment.hasMore.toString(),
                    color = Color.Gray,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = modifier.align(Alignment.CenterVertically)
                )

                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_expand_more_24),
                    colorFilter = ColorFilter.tint(Color.Gray)
                )
            }
        }
    }
}

@ExperimentalLayout
@Composable
fun MultipleComments(comments: List<Comment>, modifier: Modifier = Modifier) {
    ScrollableColumn {
        comments.forEach { comment ->
            CommentAndLevel(level = comment.level, comment, modifier)
        }
    }
}

@ExperimentalLayout
@Composable
fun CommentAndLevel(level: Int, comment: Comment, modifier: Modifier) {
    Row(modifier = Modifier.preferredHeight(IntrinsicSize.Min)) {
        CommentLevelDivider(level = level, modifier = modifier)
        SingleComment(comment = comment, modifier)
    }
}

@Composable
fun CommentLevelDivider(level: Int, modifier: Modifier) {
    for (i in 0 until level) {
        Spacer(modifier = modifier.preferredWidth(10.dp))
        Divider(
            color = MaterialTheme.colors.secondary,
            modifier = modifier.fillMaxHeight().preferredWidth(3.dp)
        )
    }
    Spacer(modifier = modifier.preferredWidth(10.dp))
}

@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun CommentPreview() {
    YetAnotherHNAppTheme {
        MultipleComments(
            comments = listOf(
                Comment(
                    "En",
                    6,
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    8,
                    0
                ),
                Comment(
                    "En",
                    6,
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    1,
                    1
                ),
                Comment(
                    "Joe",
                    4,
                    "I'm a short one liner",
                    0,
                    2
                ),
            )
        )
    }
}

@Immutable
data class Comment(
    val userName: String,
    val ageHours: Int,
    val content: String,
    val hasMore: Int,
    val level: Int
)
