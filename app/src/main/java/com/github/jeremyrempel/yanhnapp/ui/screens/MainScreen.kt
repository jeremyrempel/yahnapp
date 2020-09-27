package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.components.Loading
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed class Screen {
    object List : Screen()
    data class ViewOne(val post: Post) : Screen()
    data class ViewComments(val post: Post) : Screen()
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun MainScreen(flow: Flow<Lce<List<Post>>>) {
    val data: State<Lce<List<Post>>> = flow.collectAsState(initial = Lce.Loading())

    val currentScreen = remember { mutableStateOf<Screen>(Screen.List) }

    AnimatedVisibility(
        visible = currentScreen.value is Screen.ViewComments,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CommentsScreen(
            comments = SampleData.commentList,
            goUp = { currentScreen.value = Screen.List }
        )
    }

    when (currentScreen.value) {
        is Screen.ViewComments -> {
            val s = currentScreen.value as Screen.ViewComments
        }
        Screen.List -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(ContextAmbient.current.getString(R.string.app_name)) }
                    )
                },
                bodyContent = {
                    when (data.value) {
                        is Lce.Content -> {
                            val contentLce = data.value as Lce.Content<List<Post>>
                            PostsList(
                                data = contentLce.data,
                                onSelectPost = { currentScreen.value = Screen.ViewOne(it) },
                                onSelectPostComment = {
                                    currentScreen.value = Screen.ViewComments(it)
                                }
                            )
                        }
                        is Lce.Loading -> {
                            Loading()
                        }
                        is Lce.Error<*> -> {
                            val err = data.value as Lce.Error<List<Post>>
                            Text(err.error.message ?: "Error")
                        }
                    }
                }
            )
        }
        is Screen.ViewOne -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                        navigationIcon = {
                            IconButton(onClick = { currentScreen.value = Screen.List }) {
                                Icon(Icons.Filled.ArrowBack)
                            }
                        },
                    )
                },
                bodyContent = {
                    val screen = currentScreen.value as Screen.ViewOne
                    ViewOne(screen.post)
                }
            )

            BackButtonHandler {
                currentScreen.value = Screen.List
            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun MainListPreview() {
    YetAnotherHNAppTheme(false) {
        MainScreen(flowOf(Lce.Content(SampleData.posts)))
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true)
@Composable
fun MainListDarkPreview() {
    YetAnotherHNAppTheme(true) {
        MainScreen(flowOf(Lce.Content(SampleData.posts)))
    }
}
