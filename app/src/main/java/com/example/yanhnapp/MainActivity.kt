package com.example.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.example.yanhnapp.ui.YetAnotherHNAppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YetAnotherHNAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                }
            }
        }
    }
}

@Composable
fun MainScreen(data: List<Post>) {
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
//        bottomBar = {
//            BottomNavigationAlwaysShowLabelComponent(currentContent.value) {
//                currentContent.value = it
//            }
//        },
        bodyContent = {
            PostsList(data = data)
        }
    )
}

@Composable
fun PostsList(data: List<Post>) {
    LazyColumnFor(items = data, modifier = Modifier.padding(5.dp)) { row ->
        PostRow(post = row)
    }
}

@Composable
fun PostRow(post: Post) {
    Row {
        Text(String.format("%d.", post.rank))
        Column(modifier = Modifier.padding(horizontal = 5.dp)) {
            Text(
                text = post.title,
                style = TextStyle(
                    fontSize = TextUnit.Sp(15)
                )
            )

            Row {
                Text(
                    text = String.format(
                        "%s | %d points %d hours ago | %d comments",
                        post.domain, post.points, post.ageHours, post.commentsCnt
                    ),
                    color = Color.Gray,
                    style = TextStyle(
                        fontSize = TextUnit.Sp(10)
                    )
                )
            }

            Spacer(modifier = Modifier.preferredHeight(3.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    YetAnotherHNAppTheme(false) {
        MainScreen(
            data = listOf(
                Post(
                    1,
                    "Jetpack Compose 1.0 released",
                    "developer.android.com",
                    96,
                    2,
                    9
                ),
                Post(
                    2,
                    "First Man on Mars. This is a super long title that should go over the maximum line length.",
                    "nasa.gov",
                    1000,
                    5,
                    1000
                ),
                Post(
                    3,
                    "KMM 1.0.0 released",
                    "kotlinlang.org",
                    100,
                    1,
                    50
                ),
                Post(
                    4,
                    "Jetpack Compose is Awesome",
                    "medium.com",
                    50,
                    1,
                    50
                ),
                Post(
                    5,
                    "Linus Torvalids announces presidential candidacy",
                    "cnn.com",
                    125,
                    10,
                    100
                ),
            )
        )
    }
}

data class Post(
    val rank: Int,
    val title: String,
    val domain: String,
    val points: Int,
    val ageHours: Int,
    val commentsCnt: Int
)
