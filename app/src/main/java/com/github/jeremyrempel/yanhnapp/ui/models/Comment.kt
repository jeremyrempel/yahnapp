package com.github.jeremyrempel.yanhnapp.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class Comment(
    val userName: String,
    val unixTimeMs: Long,
    val content: String,
    val children: List<Comment> = emptyList()
)
