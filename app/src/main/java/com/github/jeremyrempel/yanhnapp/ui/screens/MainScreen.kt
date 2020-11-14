package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yahnapp.api.interactor.PostsUseCase
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler

sealed class Screen {
    data class List(val isLoading: Boolean = false) : Screen()
    data class ViewComments(val post: Post) : Screen()
}

@ExperimentalAnimationApi
@Composable
fun MainScreen(
    commentsUseCase: CommentsUseCase,
    postsUseCase: PostsUseCase
) {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.List()) }
    val scrollState = rememberLazyListState()

    // todo combine the scaffolds and animate the topbar
    Crossfade(currentScreen.value) { screen ->
        when (screen) {
            is Screen.List -> {
                ScaffoldWithContent(
                    content = {
                        ListContent(scrollState, postsUseCase, commentsUseCase) { newScreen ->
                            currentScreen.value = newScreen
                        }
                    },
                    showUp = false,
                    title = R.string.top_stories_title,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
            is Screen.ViewComments -> {
                ScaffoldWithContent(
                    content = { CommentsScreen(post = screen.post, commentsUseCase) },
                    showUp = true,
                    title = R.string.comments_title,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
        }
    }
}

@Composable
fun ScaffoldWithContent(
    content: @Composable () -> Unit,
    showUp: Boolean,
    @StringRes title: Int,
    onUpaction: () -> Unit
) {

    if (showUp) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(title)) },
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
                    title = { Text(stringResource(title)) },
                )
            },
            bodyContent = { content() }
        )
    }
}
