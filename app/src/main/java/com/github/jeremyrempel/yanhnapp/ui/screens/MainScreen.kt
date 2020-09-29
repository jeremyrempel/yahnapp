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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.BackButtonHandler
import com.github.jeremyrempel.yanhnapp.ui.SampleData
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed class Screen {
    data class List(val isLoading: Boolean = false) : Screen()
    data class ViewOne(val post: Post) : Screen()
    data class ViewComments(val post: Post) : Screen()
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun MainScreen(flow: Flow<Lce<List<Post>>>) {
    val data = flow.collectAsState(initial = Lce.Loading())
    val currentScreen = remember { mutableStateOf<Screen>(Screen.List()) }

    AnimatedVisibility(
        visible = currentScreen.value is Screen.ViewComments,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CommentsScreen(
            comments = SampleData.commentList,
            goUp = { currentScreen.value = Screen.List() }
        )
    }

    AnimatedVisibility(
        visible = currentScreen.value is Screen.List,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) }
                )
            },
            bodyContent = {
                ListContent(data.value) { newScreen ->
                    currentScreen.value = newScreen
                }
            }
        )
    }

    AnimatedVisibility(
        visible = currentScreen.value is Screen.ViewOne,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen.value = Screen.List() }) {
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
            currentScreen.value = Screen.List()
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
