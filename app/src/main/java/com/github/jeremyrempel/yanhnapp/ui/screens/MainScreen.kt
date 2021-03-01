package com.github.jeremyrempel.yanhnapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.jeremyrempel.yahn.Post
import com.github.jeremyrempel.yahnapp.api.interactor.CommentsUseCase
import com.github.jeremyrempel.yahnapp.api.interactor.PostsUseCase
import com.github.jeremyrempel.yanhnapp.R
import kotlinx.coroutines.launch

sealed class Screen {
    object List : Screen()
    data class ViewComments(val post: Post) : Screen()
    object About : Screen()
}

@ExperimentalAnimationApi
@Composable
fun MainScreen(
    commentsUseCase: CommentsUseCase,
    postsUseCase: PostsUseCase
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    val scrollState = rememberLazyListState()

    // todo combine the scaffolds and animate the topbar
    Crossfade(currentScreen) { screen ->
        when (screen) {
            is Screen.List -> {
                ScaffoldWithContent(
                    content = {
                        ListContent(
                            scrollState,
                            postsUseCase::selectAllPostsByRank,
                            postsUseCase::requestAndStorePosts,
                            postsUseCase::markPostViewed,
                            commentsUseCase::markPostCommentViewed
                        ) { newScreen ->
                            currentScreen = newScreen
                        }
                    },
                    showUp = false,
                    title = R.string.top_stories_title,
                    navigateTo = { currentScreen = it },
                    currentScreen = currentScreen,
                    onUpaction = { currentScreen = Screen.List }
                )
            }
            is Screen.ViewComments -> {
                ScaffoldWithContent(
                    content = {
                        CommentsScreen(
                            post = screen.post,
                            commentsUseCase::getCommentsForPost,
                            commentsUseCase::requestAndStoreComments,
                            commentsUseCase::getCommentsForParent
                        )
                    },
                    showUp = true,
                    title = R.string.comments_title,
                    navigateTo = { currentScreen = it },
                    currentScreen = currentScreen,
                    onUpaction = { currentScreen = Screen.List }
                )
            }
            Screen.About -> {
                ScaffoldWithContent(
                    content = { AboutScreen() },
                    showUp = false,
                    title = R.string.about_title,
                    navigateTo = { currentScreen = it },
                    currentScreen = currentScreen,
                    onUpaction = { currentScreen = Screen.List }
                )
            }
        }
    }
}

@Composable
fun ScaffoldWithContent(
    showUp: Boolean,
    @StringRes title: Int,
    navigateTo: (Screen) -> Unit,
    currentScreen: Screen,
    onUpaction: () -> Unit,
    content: @Composable () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    if (showUp) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(title)) },
                    navigationIcon = {
                        IconButton(onClick = { onUpaction() }) {
                            Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.back))
                        }
                    },
                )
            },
            content = { content() }
        )

        if (showUp) {
            BackHandler(onBack = { onUpaction() })
        }
    } else {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(title)) },
                    navigationIcon = {
                        // todo fix this
                        IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, stringResource(id = R.string.menu))
                        }
                    }
                )
            },
            drawerContent = {
                DrawerContent(navigateTo = navigateTo, currentScreen = currentScreen)
            },
            content = { content() }
        )
    }
}

@Composable
fun DrawerContent(
    navigateTo: (Screen) -> Unit,
    currentScreen: Screen
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {

        YahnLogo(modifier = Modifier.padding(16.dp))
        Spacer(Modifier.height(16.dp))
        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = .2f))
        Spacer(Modifier.height(16.dp))

        DrawerButton(
            icon = painterResource(id = R.drawable.ic_baseline_trending_up_24),
            label = "Top Stories",
            isSelected = currentScreen is Screen.List,
            action = {
                navigateTo(Screen.List)
            }
        )

        DrawerButton(
            icon = rememberVectorPainter(image = Icons.Filled.Person),
            label = "About",
            isSelected = currentScreen is Screen.About,
            action = {
                navigateTo(Screen.About)
            }
        )
    }
}

@Composable
private fun YahnLogo(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(id = R.string.logo),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
            modifier = modifier.size(80.dp)
        )
        Text(stringResource(R.string.app_name_full))
    }
}

@Composable
private fun DrawerButton(
    icon: Painter,
    label: String,
    isSelected: Boolean,
    action: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colors
    val imageAlpha = if (isSelected) {
        1f
    } else {
        0.6f
    }
    val textIconColor = if (isSelected) {
        colors.primary
    } else {
        colors.onSurface.copy(alpha = 0.6f)
    }
    val backgroundColor = if (isSelected) {
        colors.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    val surfaceModifier = modifier
        .padding(start = 8.dp, top = 8.dp, end = 8.dp)
        .fillMaxWidth()
    Surface(
        modifier = surfaceModifier,
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = action,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = icon,
                    contentDescription = stringResource(id = R.string.drawer),
                    colorFilter = ColorFilter.tint(textIconColor),
                    alpha = imageAlpha
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.body2,
                    color = textIconColor
                )
            }
        }
    }
}
