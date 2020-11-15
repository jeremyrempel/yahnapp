package com.github.jeremyrempel.yahnapp.api.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int,
    val type: ItemType,
    val by: String? = null,
    val time: Long,
    val text: String? = null,
    val kids: List<Long>? = null,
    val parts: List<Long>? = null,
    val parent: Int? = null,
    val url: String? = null,
    val score: Int? = null,
    val title: String? = null,
    val descendants: Int? = null,
    val deleted: Boolean = false,
    val dead: Boolean = false
)

@Keep
enum class ItemType {
    job, story, comment, poll, pollopt
}
