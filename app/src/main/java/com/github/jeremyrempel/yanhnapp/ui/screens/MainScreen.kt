package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler

sealed class Screen {
    data class List(val isLoading: Boolean = false) : Screen()
    data class ViewOne(val post: Post) : Screen()
    data class ViewComments(val post: Post) : Screen()
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun MainScreen(
    api: HackerNewsApi,
    db: HackerNewsDb
) {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.List()) }

    // todo combine the scaffolds and animate the topbar
    Crossfade(currentScreen.value) { screen ->
        when (screen) {
            is Screen.List -> {
                ScaffoldWithContent(
                    content = {
                        ListContent(api, db) { newScreen ->
                            currentScreen.value = newScreen
                        }
                    },
                    showUp = false,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
            is Screen.ViewComments -> {
                ScaffoldWithContent(
                    content = { CommentsScreen(api = api, post = screen.post) },
                    showUp = true,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
            is Screen.ViewOne -> {
                ScaffoldWithContent(
                    content = { ViewOne((currentScreen.value as Screen.ViewOne).post) },
                    showUp = true,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
        }
    }
}

@Composable
fun ScaffoldWithContent(content: @Composable () -> Unit, showUp: Boolean, onUpaction: () -> Unit) {

    if (showUp) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { onUpaction() }) {
                            Icon(Icons.Filled.ArrowBack)
                        }
                    },
                )
            },
            bodyContent = { content() }
        )

        if (showUp) {
            BackButtonHandler(onBackPressed = { onUpaction() })
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                )
            },
            bodyContent = { content() }
        )
    }
}
