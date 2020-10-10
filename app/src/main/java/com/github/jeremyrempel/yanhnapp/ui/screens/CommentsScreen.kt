package com.github.jeremyrempel.yanhnapp.ui.screens

import android.text.format.DateUtils
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
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
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.model.Comment
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.HtmlText
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.ui.vm.MyVm
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import java.util.Date

const val animationTime = 300

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentsScreen(post: Post) {

    val vm = viewModel<MyVm>()
    vm.requestComments(post.id)

    val data = vm.comments.observeAsState(initial = emptyList())
    val error = vm.errorMsg.observeAsState()

    when {
        !error.value.isNullOrEmpty() -> {
            Text("Error: ${error.value}")
        }
        data.value.isNotEmpty() -> {
            CommentList(comments = data.value)
        }
        else -> {
            Loading()
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentList(comments: List<Comment>, modifier: Modifier = Modifier) {
    ScrollableColumn {
        comments.forEach { comment ->
            CommentTree(level = 0, comment = comment, modifier = modifier)
        }
    }
}

@Composable
fun SingleComment(comment: Comment, modifier: Modifier) {

    val timeRelative = remember(comment.unixTimeMs) {
        DateUtils
            .getRelativeTimeSpanString(comment.unixTimeMs, Date().time, 0)
            .toString()
    }

    val context = ContextAmbient.current

    Column(
        modifier = modifier.padding(end = 10.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.subtitle1,
                modifier = modifier.padding(end = 10.dp)
            )
            Text(
                text = timeRelative,
                style = MaterialTheme.typography.subtitle1
            )
        }
        HtmlText(html = comment.content) { url ->
            launchBrowser(url, context)
        }
    }
}

@Composable
fun CommentHasMore(count: Int, isExpanded: Boolean, modifier: Modifier, onClick: () -> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        if (count > 0) {
            TextButton(
                onClick = onClick
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
                    // app crashes on alternate
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
            Spacer(modifier = Modifier.preferredHeight(15.dp).fillMaxWidth())

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
        Column {
            comment.children.forEach { c ->
                CommentTree(level = level + 1, comment = c, modifier = modifier)
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
    Spacer(modifier = modifier.preferredWidth(15.dp))
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
