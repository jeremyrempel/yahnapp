package com.github.jeremyrempel.yanhnapp.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class Post(
    val rank: Int,
    val title: String,
    val domain: String,
    val url: String,
    val points: Int,
    val ageHours: Int,
    val commentsCnt: Int
)

fun getSample() = listOf(
    Post(
        1,
        "Jetpack Compose 1.0 released",
        "developer.android.com",
        "https://developer.android.com",
        96,
        2,
        9
    ),
    Post(
        2,
        "First Man on Mars. This is a super long title that should go over the maximum line length.",
        "nasa.gov",
        "https://nasa.gov",
        1000,
        5,
        1000
    ),
    Post(
        3,
        "KMM 1.0.0 released",
        "kotlinlang.org",
        "https://kotlinlang.org",
        100,
        1,
        50
    ),
    Post(
        4,
        "Jetpack Compose is Awesome",
        "medium.com",
        "https://medium.com",
        50,
        1,
        50
    ),
    Post(
        5,
        "Linus Torvalids announces presidential candidacy",
        "cnn.com",
        "https://cnn.com",
        125,
        10,
        100
    ),
)
