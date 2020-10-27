package com.github.jeremyrempel.yanhnapp.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.jeremyrempel.yahnapp.api.HackerNewsApi
import com.github.jeremyrempel.yahnapp.api.di.NetworkModule
import com.github.jeremyrempel.yahnapp.api.repo.HackerNewsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MyVm(application: Application) : AndroidViewModel(application) {

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    // todo daggerify and extract out
    val db by lazy { HackerNewsDb(application) }
    val api: HackerNewsApi by lazy { NetworkModule.providesApi(application) }

    fun markPostViewed(id: Long) {
        viewModelScope.launch {
            async(Dispatchers.IO) {
                db.markPostAsRead(id)
            }
        }
    }

    @Suppress("DeferredResultUnused")
    fun markPostCommentViewed(id: Long) {
        viewModelScope.launch {
            async(Dispatchers.IO) {
                db.markCommentRead(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        db.close()
    }
}
