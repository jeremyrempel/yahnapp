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
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.HtmlText
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExperimentalLazyDsl
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentsScreen(post: Post, useCase: CommentsUseCase) {

    var state by remember { mutableStateOf<Lce<List<Comment>>>(Lce.Loading()) }

    LaunchedTask(post.id) {
        async(Dispatchers.IO) {
            try {
                useCase.requestAndStoreComments(post.id)
            } catch (e: Exception) {
                state = Lce.Error(e.localizedMessage ?: "")
            }
        }

        useCase.getCommentsForPost(post.id).collectLatest {
            state = Lce.Content(it)
        }
    }

    when (state) {
        is Lce.Loading -> Loading()
        is Lce.Error -> Text("Error: $state")
        is Lce.Content -> {
            val content = (state as Lce.Content<List<Comment>>)
            CommentList(comments = content.data, post, useCase)
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentList(
    comments: List<Comment>,
    post: Post,
    useCase: CommentsUseCase
) {

    ScrollableColumn {
        CommentHeader(title = post.title, domain = post.domain, date = post.unixTime)

        comments.forEach { comment ->
            CommentTree(level = 0, comment = comment, useCase)
        }
    }
}

@ExperimentalLayout
@ExperimentalAnimationApi
@Composable
fun CommentTree(level: Int, comment: Comment, useCase: CommentsUseCase) {

    var showChildren by remember(comment.id) { mutableStateOf(true) }
    var children by remember(comment.id) { mutableStateOf(emptyList<Comment>()) }

    LaunchedTask(comment.id) {
        launch {
            useCase.getCommentsForParent(comment.id).collectLatest {
                children = it
            }
        }
    }

    Row(modifier = Modifier.preferredHeight(IntrinsicSize.Min)) {
        CommentLevelDivider(level = level)

        Column {
            Spacer(modifier = Modifier.preferredHeight(15.dp).fillMaxWidth())

            SingleComment(comment = comment)
            CommentHasMore(
                count = comment.childrenCnt,
                isExpanded = showChildren
            ) {
                showChildren = !showChildren
            }
        }
    }

    AnimatedVisibility(
        visible = showChildren,
        enter = slideInVertically(
            initialOffsetY = { -40 },
            animSpec = TweenSpec(),
        ) + expandVertically(
            expandFrom = Alignment.Top,
            animSpec = TweenSpec(),
        ) + fadeIn(
            initialAlpha = 0.3f,
            animSpec = TweenSpec(),
        ),
        exit = slideOutVertically(
            targetOffsetY = { -40 },
            animSpec = TweenSpec(),
        ) + shrinkVertically(
            animSpec = TweenSpec()
        ) + fadeOut(
            animSpec = TweenSpec()
        )
    ) {
        if (showChildren) {
            Column {
                children.forEach { c ->
                    CommentTree(level = level + 1, comment = c, useCase = useCase)
                }
            }
        }
    }
}

@Composable
fun SingleComment(comment: Comment, modifier: Modifier = Modifier) {

    val timeRelative = remember(comment.unixTime) {
        DateUtils
            .getRelativeTimeSpanString(comment.unixTime, Instant.now().epochSecond, 0)
            .toString()
    }

    Column(
        modifier = modifier.padding(end = 10.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = comment.username,
                style = MaterialTheme.typography.subtitle1,
                modifier = modifier.padding(end = 10.dp)
            )
            Text(
                text = timeRelative,
                style = MaterialTheme.typography.subtitle1
            )
        }
        val context = ContextAmbient.current
        HtmlText(html = comment.content) { url ->
            launchBrowser(url, context)
        }
    }
}

@Composable
private fun CommentHeader(title: String, domain: String?, date: Long) {
    val relativeDate =
        remember(date) {
            DateUtils.getRelativeTimeSpanString(
                date * 1000,
                Instant.now().toEpochMilli(),
                0
            ).toString()
        }

    Column(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6
        )

        Row(
            Modifier.fillMaxWidth().padding(end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (domain != null) {
                Text(text = domain, style = MaterialTheme.typography.body2)
            }
            Text(text = relativeDate, style = MaterialTheme.typography.body2)
        }

        Divider(modifier = Modifier.fillMaxWidth().padding(top = 15.dp))
    }
}

@Composable
fun CommentHasMore(count: Long, isExpanded: Boolean, onClick: () -> Unit) {

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
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Image(
                    asset = vectorResource(id = R.drawable.ic_baseline_expand_more_24),
                    colorFilter = ColorFilter.tint(Color.Gray),
                    // app crashes on alternate
                    modifier = Modifier.drawLayer(
                        scaleY = animate(
                            target = if (isExpanded) -1f else 1f,
                            animSpec = TweenSpec()
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun CommentLevelDivider(level: Int, modifier: Modifier = Modifier) {
    for (i in 0 until level) {
        Spacer(modifier = modifier.preferredWidth(10.dp))
        Divider(
            color = MaterialTheme.colors.secondary,
            modifier = modifier.fillMaxHeight().preferredWidth(3.dp)
        )
    }
    Spacer(modifier = modifier.preferredWidth(15.dp))
}

@Preview(showBackground = true)
@Composable
fun CommentHasMorePreview() {
    var expanded by remember { mutableStateOf(true) }

    CommentHasMore(count = 1, isExpanded = expanded, onClick = { expanded = !expanded })
}

@Preview(showBackground = true)
@Composable
fun CommentHeaderPreview() {
    YetAnotherHNAppTheme {
        CommentHeader(
            "My Cool Post",
            "cnn.com",
            Instant.now().minus(1, ChronoUnit.HOURS).epochSecond
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleCommentPreview() {
    SingleComment(comment = SampleData.commentList.first())
}

@ExperimentalLazyDsl
@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun CommentPreview() {
    YetAnotherHNAppTheme {
        // CommentList(comments = SampleData.commentList, SampleData.posts.first())
    }
}
