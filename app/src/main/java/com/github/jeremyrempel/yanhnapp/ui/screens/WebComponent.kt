package com.github.jeremyrempel.yanhnapp.ui.screens

import android.print.PrintDocumentAdapter
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber

// todo replace with androidx.compose.androidview
class WebContext {

    companion object {
        val debug = true
    }

    fun createPrintDocumentAdapter(documentName: String): PrintDocumentAdapter {
        validateWebView()
        return webView!!.createPrintDocumentAdapter(documentName)
    }

    fun goForward() {
        validateWebView()
        webView!!.goForward()
    }

    fun goBack() {
        validateWebView()
        webView!!.goBack()
    }

    fun canGoBack(): Boolean {
        validateWebView()
        return webView!!.canGoBack()
    }

    private fun validateWebView() {
        if (webView == null) {
            throw IllegalStateException("The WebView is not initialized yet.")
        }
    }

    internal var webView: WebView? = null
}

private fun WebView.setRef(ref: (WebView) -> Unit) {
    ref(this)
}

private fun WebView.setUrl(url: String) {
    if (originalUrl != url) {
        if (WebContext.debug) {
            Timber.d("WebComponent load url")
        }
        loadUrl(url)
    }
}

@Composable
fun WebComponent(
    url: String,
    webViewClient: WebViewClient = WebViewClient(),
    webContext: WebContext
) {
    if (WebContext.debug) {
        Timber.d("WebComponent compose %s", url)
    }

    AndroidView(::WebView) {
        it.setRef { view -> webContext.webView = view }
        it.setUrl(url)
        it.webViewClient = webViewClient
    }
}
