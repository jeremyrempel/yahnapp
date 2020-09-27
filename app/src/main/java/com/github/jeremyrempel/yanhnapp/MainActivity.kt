package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.setContent
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.net.URL

class MainActivity : AppCompatActivity() {

    val job = Job()

    @ExperimentalAnimationApi
    @ExperimentalLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = HackerNewsApi(networkDebug = Timber::d)

        val scope = CoroutineScope(job)

        val dataFlow = flow {
            try {
                val topList = api.fetchTopItems()
                    .take(20)
                    .map {
                        scope.async(Dispatchers.IO) {
                            api.fetchItem(it)
                        }
                    }.map {
                        it.await()
                    }.mapIndexed { i, item ->
                        Post(
                            i + 1,
                            item.title ?: "",
                            if (item.url != null) URL(item.url).toURI().authority else null,
                            item.url,
                            text = item.text,
                            item.score ?: 0,
                            1,
                            item.descendants ?: 0
                        )
                    }

                emit(topList)
            } catch (e: Exception) {
                // todo show user an err
                Timber.w(e)
            }
        }

        setContent {
            YetAnotherHNAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(flow = dataFlow)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
