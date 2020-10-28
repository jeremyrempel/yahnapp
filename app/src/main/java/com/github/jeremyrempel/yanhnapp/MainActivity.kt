package com.github.jeremyrempel.yanhnapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.ui.platform.setContent
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yahnapp.api.interactor.PostsUseCase
import com.github.jeremyrempel.yanhnapp.ui.screens.MainScreen
import com.github.jeremyrempel.yanhnapp.ui.theme.YetAnotherHNAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var commentUseCase: CommentsUseCase

    @Inject
    lateinit var postsUseCase: PostsUseCase

    @ExperimentalLazyDsl
    @ExperimentalLayout
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YetAnotherHNAppTheme {
                MainScreen(
                    commentsUseCase = commentUseCase,
                    postsUseCase = postsUseCase
                )
            }
        }
    }
}
