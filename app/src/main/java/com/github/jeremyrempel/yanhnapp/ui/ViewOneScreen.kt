package com.github.jeremyrempel.yanhnapp.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

const val TAB_CONTENT = 0
const val TAB_COMMENTS = 1

@Composable
fun ViewOne() {
    val selectedTab = remember { mutableStateOf(TAB_CONTENT) }

    Column {
        TabRow(selectedTabIndex = selectedTab.value, modifier = Modifier.preferredHeight(48.dp)) {
            Tab(
                selected = selectedTab.value == TAB_CONTENT,
                onClick = { selectedTab.value = TAB_CONTENT }
            ) {
                Text(getString(R.string.content_title))
            }
            Tab(
                selected = selectedTab.value == TAB_COMMENTS,
                onClick = { selectedTab.value = TAB_COMMENTS }
            ) {
                Text(getString(R.string.comments_title))
            }
        }

        if (selectedTab.value == TAB_CONTENT) {
            ViewOneContent()
        } else {
            ViewOneComments()
        }
    }
}

@Composable
fun ViewOneContent() {
    AndroidView(
        viewBlock = { ctx ->
            val webView = WebView(ctx)
            val webViewClient = WebViewClient()
            webView.webViewClient = webViewClient
            webView.loadUrl("https://google.com")

            webView
        }
    )
}

@Composable
fun ViewOneComments() {
    Text("Comments")
}

@Preview(showBackground = true)
@Composable
fun ViewOnePreview() {
    YetAnotherHNAppTheme(false) {
        ViewOne()
    }
}
