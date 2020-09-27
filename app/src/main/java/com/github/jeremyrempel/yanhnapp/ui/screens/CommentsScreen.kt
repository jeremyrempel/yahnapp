package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animate
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope.align
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.models.Comment
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

const val animationTime = 300

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentsScreen(comments: List<Comment>, goUp: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { goUp() }) {
                        Icon(Icons.Filled.ArrowBack)
                    }
                },
            )
        },
        bodyContent = {
            CommentList(comments = comments)
        }
    )

    BackButtonHandler {
        goUp()
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

@Composable
fun SingleComment(comment: Comment, modifier: Modifier) {
    Column(
        modifier = modifier.padding(start = 5.dp, end = 5.dp).fillMaxWidth()
    ) {
        Row {
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
    }
}

@Composable
fun CommentHasMore(count: Int, isExpanded: Boolean, modifier: Modifier, onClick: () -> Unit) {

    Row(
        modifier = modifier.align(Alignment.End).padding(end = 10.dp)
    ) {
        if (count > 0) {
            TextButton(
                onClick = { onClick() }
            ) {

                Text(
                    text = count.toString(),
                    color = Color.Gray,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = modifier.align(Alignment.CenterVertically)
                )

                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_expand_more_24),
                    colorFilter = ColorFilter.tint(Color.Gray),
                    modifier = modifier.drawLayer(
                        scaleY = animate(
                            target = if (isExpanded) -1f else 1f,
                            animSpec = TweenSpec(animationTime)
                        )
                    )
                )
            }
        }
    }
}

@ExperimentalLayout
@ExperimentalAnimationApi
@Composable
fun CommentTree(level: Int, comment: Comment, modifier: Modifier) {

    val showChildren = remember { mutableStateOf(true) }

    Row(modifier = Modifier.preferredHeight(IntrinsicSize.Min)) {
        CommentLevelDivider(level = level, modifier = modifier)

        Column {
            SingleComment(comment = comment, modifier)
            CommentHasMore(
                count = comment.children.size,
                isExpanded = showChildren.value,
                modifier
            ) {
                showChildren.value = !showChildren.value
            }
        }
    }

    AnimatedVisibility(
        visible = showChildren.value,
        enter = slideInVertically(
            initialOffsetY = { -40 },
            animSpec = TweenSpec(animationTime),
        ) + expandVertically(
            expandFrom = Alignment.Top,
            animSpec = TweenSpec(animationTime),
        ) + fadeIn(
            initialAlpha = 0.3f,
            animSpec = TweenSpec(animationTime),
        ),
        exit = slideOutVertically(
            targetOffsetY = { -40 },
            animSpec = TweenSpec(animationTime),
        ) + shrinkVertically(
            animSpec = TweenSpec(animationTime)
        ) + fadeOut(
            animSpec = TweenSpec(animationTime)
        )
    ) {
        LazyColumnFor(items = comment.children, modifier = modifier) { c ->
            CommentTree(level = level + 1, comment = c, modifier = modifier)
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
        CommentList(comments = SampleData.commentList)
    }
}
