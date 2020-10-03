package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.ui.platform.setContent
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    @ExperimentalLayout
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = HackerNewsApi(networkDebug = Timber::d)
        val db = HackerNewsDb(applicationContext)

        setContent {
            YetAnotherHNAppTheme {
                MainScreen(api, db)
            }
        }
    }
}
