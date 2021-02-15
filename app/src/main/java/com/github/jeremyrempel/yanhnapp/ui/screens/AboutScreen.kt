package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.jeremyrempel.yanhnapp.ui.components.HtmlText
import com.github.jeremyrempel.yanhnapp.util.launchBrowser

@Composable
fun AboutScreen() {

    val aboutText = """
     <p>
        Yet Another Hacker News (YAHN) is developed using Jetpack Compose. YAHN is licenced under GPL.
     </p>

     <p>
        See <a href="https://github.com/jeremyrempel/yahnapp">Github</a> for source and more info
     </p>
    """.trimIndent()

    val context = LocalContext.current

    Box(modifier = Modifier.padding(10.dp)) {
        HtmlText(html = aboutText, handleLink = { launchBrowser(url = it, context = context) })
    }
}
