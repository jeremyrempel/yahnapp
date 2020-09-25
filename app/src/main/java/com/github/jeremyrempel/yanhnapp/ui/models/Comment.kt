package com.github.jeremyrempel.yanhnapp.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class Comment(
    val userName: String,
    val ageHours: Int,
    val content: String,
    val children: List<Comment> = emptyList()
)
