package com.github.jeremyrempel.yahnapp.api.model

data class Comment(
    val userName: String,
    val unixTimeMs: Long,
    val content: String,
    val children: List<Comment> = emptyList()
)
