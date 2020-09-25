package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.R
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.models.getSample
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme

@ExperimentalAnimationApi
@ExperimentalLayout
@Composable
fun MainScreen(data: List<Post>) {
    val currentScreen = remember { mutableStateOf("list") }

    if (currentScreen.value == "list") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Settings)
                        }
                    }
                )
            },
            bodyContent = {
                PostsList(data = data) {
                    currentScreen.value = "viewone"
                }
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ContextAmbient.current.getString(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { currentScreen.value = "list" }) {
                            Icon(Icons.Filled.ArrowBack)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Settings)
                        }
                    }
                )
            },
            bodyContent = {
                ViewOne()
            }
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true, showDecoration = true)
@Composable
fun MainListPreview() {
    YetAnotherHNAppTheme(false) {
        MainScreen(data = getSample())
    }
}

@ExperimentalAnimationApi
@ExperimentalLayout
@Preview(showBackground = true, showDecoration = true)
@Composable
fun MainListDarkPreview() {
    YetAnotherHNAppTheme(true) {
        MainScreen(data = getSample())
    }
}
