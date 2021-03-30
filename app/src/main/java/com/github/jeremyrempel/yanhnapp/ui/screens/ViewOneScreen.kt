package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

const val TAB_CONTENT = 0
const val TAB_COMMENTS = 1

@ExperimentalAnimationApi
@Composable
fun ViewOne(post: Post) {
    val selectedTab = remember { mutableStateOf(TAB_CONTENT) }

    Column {
        TabRow(selectedTabIndex = selectedTab.value, modifier = Modifier.height(48.dp)) {
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
            ViewOneContent(post.text)
        } else {
            ViewOneComments()
        }
    }
}

@Composable
fun ViewOneContent(text: String?) {
    Text(text ?: "")
}

@ExperimentalAnimationApi
@Composable
fun ViewOneComments() {
    // CommentPreview()
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun ViewOnePreview() {
    YetAnotherHNAppTheme(false) {
        ViewOne(SampleData.posts.first())
    }
}
