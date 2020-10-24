package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.viewModel
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler
import com.github.jeremyrempel.yanhnapp.ui.vm.MyVm

sealed class Screen {
    data class List(val isLoading: Boolean = false) : Screen()
    data class ViewOne(val post: Post) : Screen()
    data class ViewComments(val post: Post) : Screen()
}

@ExperimentalLazyDsl
@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun MainScreen() {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.List()) }
    val scrollState = rememberLazyListState()

    // todo combine the scaffolds and animate the topbar
    Crossfade(currentScreen.value) { screen ->
        when (screen) {
            is Screen.List -> {
                ScaffoldWithContent(
                    content = {
                        ListContent(scrollState) { newScreen ->
                            currentScreen.value = newScreen
                        }
                    },
                    showUp = false,
                    title = R.string.top_stories_title,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
            is Screen.ViewComments -> {

                // todo pass in
                val vm = viewModel<MyVm>()
                val commentUseCase = CommentsUseCase(
                    db = vm.db,
                    api = vm.api
                )

                ScaffoldWithContent(
                    content = { CommentsScreen(post = screen.post, commentUseCase) },
                    showUp = true,
                    title = R.string.comments_title,
                    onUpaction = { currentScreen.value = Screen.List() }
                )
            }
            is Screen.ViewOne -> {
                ScaffoldWithContent(
                    content = { ViewOne((currentScreen.value as Screen.ViewOne).post) },
                    showUp = true,
                    title = R.string.app_name,
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
