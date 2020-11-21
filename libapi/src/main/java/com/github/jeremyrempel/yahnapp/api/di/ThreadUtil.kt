package com.github.jeremyrempel.yahnapp.api.di

import android.os.Looper

fun checkMainThread() {
    require(Looper.getMainLooper() != Looper.myLooper()) {
        "Main thread check failed"
    }
}
