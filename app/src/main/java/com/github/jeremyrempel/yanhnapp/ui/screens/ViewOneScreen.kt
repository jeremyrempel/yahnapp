package com.github.jeremyrempel.yanhnapp.ui.screens

import android.webkit.WebViewClient
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yahnapp.api.model.Post
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

const val TAB_CONTENT = 0
const val TAB_COMMENTS = 1

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun ViewOne(post: Post) {
    val selectedTab = remember { mutableStateOf(TAB_CONTENT) }

    Column {
        TabRow(selectedTabIndex = selectedTab.value, modifier = Modifier.preferredHeight(48.dp)) {
            Tab(
                selected = selectedTab.value == TAB_CONTENT,
                onClick = { selectedTab.value = TAB_CONTENT }
            ) {
                Text(stringResource(R.string.content_title))
            }
            Tab(
                selected = selectedTab.value == TAB_COMMENTS,
                onClick = { selectedTab.value = TAB_COMMENTS }
            ) {
                Text(stringResource(R.string.comments_title))
            }
        }

        if (selectedTab.value == TAB_CONTENT) {
            ViewOneContent(post.url, post.text)
        } else {
            ViewOneComments()
        }
    }
}

@Composable
fun ViewOneContent(url: String?, text: String?) {
    val ctx = remember { WebContext() }
    val client = remember { WebViewClient() }

    if (text != null) {
        Text(text = text)
    }

    if (url != null) {
        WebComponent(url = url, webViewClient = client, webContext = ctx)
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun ViewOneComments() {
    CommentPreview()
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun ViewOnePreview() {
    YetAnotherHNAppTheme(false) {
        ViewOne(SampleData.posts.first())
    }
}
