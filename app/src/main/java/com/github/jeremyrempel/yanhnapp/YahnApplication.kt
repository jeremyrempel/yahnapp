package com.github.jeremyrempel.yanhnapp

import android.app.Application
import timber.log.Timber

class YahnApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
