package com.github.jeremyrempel.yanhnapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
            if (comment.children.isNotEmpty()) {
                Text(
                    text = comment.children.size.toString(),
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

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentList(comments: List<Comment>, modifier: Modifier = Modifier) {
    LazyColumnFor(items = comments, modifier = modifier) { comment ->
        CommentTree(level = 0, comment = comment, modifier = modifier)
    }
}

@ExperimentalLayout
@ExperimentalAnimationApi
@Composable
fun CommentTree(level: Int, comment: Comment, modifier: Modifier) {

    val showChildren = remember { mutableStateOf(true) }

    Row(modifier = Modifier.preferredHeight(IntrinsicSize.Min)) {
        CommentLevelDivider(level = level, modifier = modifier)
        SingleComment(comment = comment, modifier)
    }

    AnimatedVisibility(visible = showChildren.value) {
        Column {
            comment.children.forEach {
                CommentTree(level = level + 1, comment = it, modifier = modifier)
            }
        }
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

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun CommentPreview() {
    YetAnotherHNAppTheme {
        CommentList(
            comments = listOf(
                Comment(
                    "En",
                    6,
                    "L1: Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                    listOf(
                        Comment(
                            "Kaiman",
                            6,
                            "L2: Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                            listOf(
                                Comment(
                                    "Nikido",
                                    4,
                                    "L3: I'm a short one liner reply",
                                ),
                                Comment(
                                    "Ebisu",
                                    1,
                                    "L3: I'm another reply",
                                )
                            )
                        )
                    )
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
    val children: List<Comment> = emptyList()
)
