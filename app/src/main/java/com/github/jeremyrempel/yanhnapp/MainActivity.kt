package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.ui.platform.setContent
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.ui.vm.MyVm
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    @ExperimentalLayout
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm: MyVm by viewModels()
        vm.start()

        val api = HackerNewsApi(context = application, networkDebug = Timber::d)

        setContent {
            YetAnotherHNAppTheme {
                MainScreen(api)
            }
        }
    }
}
