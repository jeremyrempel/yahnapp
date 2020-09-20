package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.ui.tooling.preview.Preview
import com.github.jeremyrempel.yanhnapp.ui.PostsList
import com.github.jeremyrempel.yanhnapp.ui.ViewOne
import com.github.jeremyrempel.yanhnapp.ui.YetAnotherHNAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YetAnotherHNAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(data = getSample())
                }
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun MainListPreview() {
    YetAnotherHNAppTheme(false) {
        MainScreen(data = getSample())
    }
}

@Preview(showBackground = true)
@Composable
fun MainListDarkPreview() {
    YetAnotherHNAppTheme(true) {
        MainScreen(data = getSample())
    }
}
