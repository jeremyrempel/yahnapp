package com.github.jeremyrempel.yanhnapp.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class Post(
    val rank: Int,
    val title: String,
    val domain: String?, // ask hn have no urls
    val url: String?,
    val text: String?,
    val points: Int,
    val unixTimeMs: Long,
    val commentsCnt: Int
)
