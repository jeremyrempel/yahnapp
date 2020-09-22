package com.github.jeremyrempel.yanhnapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.wrapContentWidth
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
fun SingleComment(comment: Comment) {
    Column(
        modifier = Modifier.padding(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = "${comment.ageHours} hours ago",
                style = MaterialTheme.typography.subtitle1
            )

            if (comment.hasMore) {
                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_arrow_downward_24),
                    colorFilter = ColorFilter.tint(Color.Gray),
                    alignment = Alignment.TopEnd
                )
            }
        }
        Text(
            text = comment.content,
            style = MaterialTheme.typography.body1
        )
    }
}

@ExperimentalLayout
@Composable
fun MultipleComments(comments: List<Comment>) {
    Row(modifier = Modifier.preferredHeight(IntrinsicSize.Min)) {

        Spacer(modifier = Modifier.preferredWidth(5.dp))

        Divider(color = Color.Red, modifier = Modifier.fillMaxHeight().preferredWidth(5.dp))

        Column(
            modifier = Modifier.weight(1f)
                .padding(5.dp)
                .wrapContentWidth(Alignment.End)
        ) {
            comments.forEach { SingleComment(comment = it) }

            Text(
                text = "world"
            )
        }
    }
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
                    true
                ),
                Comment(
                    "En",
                    6,
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    true
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
    val hasMore: Boolean
)
