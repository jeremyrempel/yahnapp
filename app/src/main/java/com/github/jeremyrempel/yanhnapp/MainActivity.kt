package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.ui.platform.setContent
import androidx.lifecycle.lifecycleScope
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.Lce
import com.github.jeremyrempel.yanhnapp.ui.models.Post
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.net.URL

class MainActivity : AppCompatActivity() {

    @ExperimentalAnimationApi
    @ExperimentalLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = HackerNewsApi(networkDebug = Timber::d)

        val dataFlow = flow {
            emit(Lce.Loading())

            try {
                val topList = api.fetchTopItems()
                    .take(50)
                    .map {
                        lifecycleScope.async(Dispatchers.IO) {
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
                            item.time * 1000, // seconds to ms
                            item.descendants ?: 0
                        )
                    }

                emit(Lce.Content(topList))
            } catch (e: Exception) {
                emit(Lce.Error(e))
                Timber.w(e)
            }
        }

        setContent {
            YetAnotherHNAppTheme {
                MainScreen(flow = dataFlow)
            }
        }
    }
}
