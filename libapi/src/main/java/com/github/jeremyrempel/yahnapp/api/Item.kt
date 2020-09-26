package com.github.jeremyrempel.yahnapp.api

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int,
    val type: ItemType,
    val by: String,
    val time: Long,
    val text: String? = null,
    val kids: List<Int>? = null,
    val parent: Int? = null,
    val url: String? = null,
    val score: Int? = null,
    val title: String? = null,
    val descendants: Int? = null,
)

enum class ItemType {
    job, story, comment, poll, pollopt
}
