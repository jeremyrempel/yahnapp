package com.github.jeremyrempel.yanhnapp.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jeremyrempel.yahn.Comment
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.HtmlText
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.util.launchBrowser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExperimentalAnimationApi
@Composable
fun CommentsScreen(
    post: Post,
    getCommentsForPost: (postId: Long) -> Flow<List<Comment>>,
    requestAndStoreComments: suspend (postId: Long, (Float) -> Unit) -> Unit,
    getCommentsForParent: suspend (Long) -> Flow<List<Comment>>
) {

    val data by getCommentsForPost(post.id).collectAsState(initial = emptyList())
    var loadProgress by remember { mutableStateOf(0.0f) }

    var errorMsgVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(post.id) {
        try {
            requestAndStoreComments(post.id) {
                loadProgress = it
            }
        } catch (e: Exception) {
            Timber.e(e)
            error = e.localizedMessage
            errorMsgVisible = true
        }
    }

    Column {
        if (error != null && errorMsgVisible) {
            val errorTxt = error!!
            Text(errorTxt)
            TextButton(onClick = { errorMsgVisible = false }) {
                Text("Dismiss")
            }
        }

        if (loadProgress < 100) {
            LinearProgressIndicator(
                loadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        CommentList(comments = data, post, getCommentsForParent)
    }
}

@ExperimentalAnimationApi
@Composable
fun CommentList(
    comments: List<Comment>,
    post: Post,
    getComments: suspend (Long) -> Flow<List<Comment>>
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState(), enabled = true)
    ) {
        CommentHeader(
            title = post.title,
            domain = post.domain,
            date = post.unixTime,
            content = post.text
        )

        comments.forEach { comment ->
            CommentTree(level = 0, comment = comment, getComments = getComments)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun CommentTree(
    level: Int,
    comment: Comment,
    getComments: suspend (Long) -> Flow<List<Comment>>
) {

    var showChildren by remember(comment.id) { mutableStateOf(true) }
    var children by remember(comment.id) { mutableStateOf(emptyList<Comment>()) }

    rememberCoroutineScope().launch {
        getComments(comment.id).collectLatest {
            children = it
        }
    }

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        CommentLevelDivider(level = level)

        Column {
            Spacer(
                modifier = Modifier
                    .height(15.dp)
                    .fillMaxWidth()
            )

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
            animationSpec = TweenSpec(),
        ) + expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = TweenSpec(),
        ) + fadeIn(
            initialAlpha = 0.3f,
            animationSpec = TweenSpec(),
        ),
        exit = slideOutVertically(
            targetOffsetY = { -40 },
            animationSpec = TweenSpec(),
        ) + shrinkVertically(
            animationSpec = TweenSpec()
        ) + fadeOut(
            animationSpec = TweenSpec()
        )
    ) {
        Column {
            children.forEach { c ->
                CommentTree(level = level + 1, comment = c, getComments = getComments)
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
        val context = LocalContext.current
        HtmlText(html = comment.content) { url ->
            launchBrowser(url, context)
        }
    }
}

@Composable
private fun CommentHeader(title: String, domain: String?, date: Long, content: String?) {
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
            Modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (domain != null) {
                Text(text = domain, style = MaterialTheme.typography.body2)
            }
            Text(text = relativeDate, style = MaterialTheme.typography.body2)
        }

        if (content != null) {
            val context = LocalContext.current
            HtmlText(html = content) { url ->
                launchBrowser(url, context)
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp)
        )
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

                val arrowDirection: Float by animateFloatAsState(if (isExpanded) -1f else 1f)

                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_expand_more_24),
                    contentDescription = stringResource(id = R.string.show_more),
                    // app crashes on alternate
                    modifier = Modifier.graphicsLayer(
                        scaleY = arrowDirection
                    ),
                    colorFilter = ColorFilter.tint(Color.Gray)
                )
            }
        }
    }
}

@Composable
fun CommentLevelDivider(level: Int, modifier: Modifier = Modifier) {
    for (i in 0 until level) {
        Spacer(modifier = modifier.width(10.dp))
        Divider(
            color = MaterialTheme.colors.secondary,
            modifier = modifier
                .fillMaxHeight()
                .width(3.dp)
        )
    }
    Spacer(modifier = modifier.width(15.dp))
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
            Instant.now().minus(1, ChronoUnit.HOURS).epochSecond,
            "This is my content from a ASK HN or something"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleCommentPreview() {
    SingleComment(comment = SampleData.commentList.first())
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun CommentPreview() {
    val getComments: suspend (Long) -> Flow<List<Comment>> = {
        flow {
            emit(SampleData.commentList)
        }
    }

    YetAnotherHNAppTheme {
        CommentList(SampleData.commentList, SampleData.posts.first(), getComments)
    }
}
