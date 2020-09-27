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

fun getSample() = listOf(
    Post(
        1,
        "Jetpack Compose 1.0 released",
        "developer.android.com",
        "https://developer.android.com",
        null,
        96,
        2,
        9
    ),
    Post(
        2,
        "First Man on Mars. This is a super long title that should go over the maximum line length.",
        "nasa.gov",
        "https://nasa.gov",
        null,
        1000,
        5,
        1000
    ),
    Post(
        3,
        "KMM 1.0.0 released",
        "kotlinlang.org",
        "https://kotlinlang.org",
        null,
        100,
        1,
        50
    ),
    Post(
        4,
        "Jetpack Compose is Awesome",
        "medium.com",
        "https://medium.com",
        null,
        50,
        1,
        50
    ),
    Post(
        5,
        "Linus Torvalids announces presidential candidacy",
        "cnn.com",
        "https://cnn.com",
        null,
        125,
        10,
        100
    ),
    Post(
        6,
        "Ask HN: How can I learn to code?",
        null,
        null,
        "<p>How can I learn to code. This is a long description with html</p>",
        200,
        5,
        25
    )
)
