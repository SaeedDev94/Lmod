package io.github.saeeddev94.lmod

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.webkit.WebView
import android.webkit.WebViewClient

class ChromeClient(
    private val activity: Context,
    private val incognito: Boolean,
) {
    fun onCreateWindow(
        view: WebView, isDialog: Boolean,
        isUserGesture: Boolean, resultMsg: Message
    ): Boolean {
        val transport = resultMsg.obj as WebView.WebViewTransport
        val tempWebView = WebView(view.context)
        tempWebView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView, url: String) {
                tempWebView.destroy()
                view.loadUrl(url)
                val intent = Intent().apply {
                    setClassName("org.lineageos.jelly", "org.lineageos.jelly.MainActivity")
                    data = Uri.parse(url)
                    flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    putExtra("extra_incognito", incognito)
                }
                activity.startActivity(intent)
            }
        }
        transport.webView = tempWebView
        resultMsg.sendToTarget()
        return true
    }
}
