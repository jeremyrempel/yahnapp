package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.ui.platform.setContent
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import com.github.jeremyrempel.yanhnapp.ui.vm.MyVm

class MainActivity : AppCompatActivity() {

    private val vm: MyVm by viewModels()

    @ExperimentalLazyDsl
    @ExperimentalLayout
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.requestAndStorePosts()

        setContent {
            YetAnotherHNAppTheme {
                MainScreen()
            }
        }
    }
}
