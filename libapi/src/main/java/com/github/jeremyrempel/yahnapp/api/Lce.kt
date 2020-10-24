package com.github.jeremyrempel.yahnapp.api

sealed class Lce<T> {
    class Loading<T> : Lce<T>()
    data class Content<T>(val data: T) : Lce<T>()
    data class Error<T>(val error: String) : Lce<T>()
}
