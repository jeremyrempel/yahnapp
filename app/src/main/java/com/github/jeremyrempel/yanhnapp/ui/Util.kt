package com.github.jeremyrempel.yanhnapp.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ContextAmbient

@Composable
fun getString(@StringRes stringRes: Int): String {
    return ContextAmbient.current.getString(stringRes)
}
