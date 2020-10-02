package com.github.jeremyrempel.yahnapp.api.model

data class Post(
    val id: Int,
    val title: String,
    val domain: String?, // ask hn have no urls
    val url: String?,
    val text: String?,
    val points: Int,
    val unixTimeMs: Long,
    val commentsCnt: Int
)
