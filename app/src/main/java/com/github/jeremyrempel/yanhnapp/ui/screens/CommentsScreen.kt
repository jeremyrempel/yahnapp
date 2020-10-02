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
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yahnapp.api.model.Comment
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.apache.commons.text.StringEscapeUtils
import timber.log.Timber
import java.util.Date

const val animationTime = 300

/**
 * Given list of trees, fetch all leafs and metadata
 */
suspend fun getCommentsForPost(postId: Long, api: HackerNewsApi) = coroutineScope {

    // dfs
    suspend fun getCommentsByIds(commentIds: List<Long>): List<Comment> {
        return commentIds
            .map {
                async(Dispatchers.IO) {
                    api.fetchItem(it)
                }
            }.map { it.await() }
            .map { item ->
                // recurse
                val commentChildren = getCommentsByIds(item.kids ?: listOf())

                Comment(
                    item.by ?: "",
                    item.time * 1000,
                    item.text ?: "",
                    commentChildren
                )
            }
    }

    val kids = (api.fetchItem(postId).kids ?: emptyList())

    // dfs start
    getCommentsByIds(kids)
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun CommentsScreen(api: HackerNewsApi, post: Post) {

    val result = remember(post.id) { mutableStateOf<Lce<List<Comment>>>(Lce.Loading()) }

    launchInComposition {
        try {
            result.value = Lce.Content(getCommentsForPost(post.id, api))
        } catch (e: Exception) {
            Timber.e(e)
            result.value = Lce.Error(e)
        }
    }

    when (result.value) {
        is Lce.Loading -> Loading()
        is Lce.Error -> {
            val errorMsg = (result.value as Lce.Error).error.message ?: "Unknown Error"
            Text(errorMsg)
        }
        is Lce.Content -> {
            val contentResult = result.value as Lce.Content
            CommentList(comments = contentResult.data)
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

    val contentEscape = remember(comment.content) {
        StringEscapeUtils.unescapeHtml4(comment.content)
            .replace("<p>", "\n")
            .replace("</p>", "")
    }

    val timeRelative = remember(comment.unixTimeMs) {
        DateUtils
            .getRelativeTimeSpanString(comment.unixTimeMs, Date().time, 0)
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
                text = comment.userName,
                style = MaterialTheme.typography.subtitle1,
                modifier = modifier.padding(end = 10.dp)
            )
            Text(
                text = timeRelative,
                style = MaterialTheme.typography.subtitle1
            )
        }
        Text(
            text = contentEscape,
            style = MaterialTheme.typography.body1
        )
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
            SingleComment(comment = comment, modifier)
            CommentHasMore(
                count = comment.children.size,
                isExpanded = showChildren.value,
                modifier
            ) {
                showChildren.value = !showChildren.value
            }
            Spacer(modifier = Modifier.preferredHeight(10.dp).fillMaxWidth())
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
