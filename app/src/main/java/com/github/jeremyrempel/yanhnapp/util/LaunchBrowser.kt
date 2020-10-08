package com.github.jeremyrempel.yanhnapp.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun launchBrowser(url: String, context: Context) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        .setUrlBarHidingEnabled(true)
        .build()
        .launchUrl(context, Uri.parse(url))
}
