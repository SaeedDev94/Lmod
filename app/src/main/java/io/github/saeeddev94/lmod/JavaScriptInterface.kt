package io.github.saeeddev94.lmod

import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.EditText
import androidx.annotation.Keep

@Keep
class JavaScriptInterface(
    urlBarLayout: ViewGroup,
) {

    private val secure: View
    private val urlBar: EditText

    init {
        val root = urlBarLayout.getChildAt(0) as ViewGroup
        val card = root.getChildAt(1) as ViewGroup
        val layout = card.getChildAt(0) as ViewGroup
        secure = layout.getChildAt(1) as View
        urlBar = layout.getChildAt(2) as EditText
    }

    @JavascriptInterface
    fun onPopState(url: String) {
        updateUrl(url)
    }

    @JavascriptInterface
    fun onPushState(url: String) {
        updateUrl(url)
    }

    @JavascriptInterface
    fun onReplaceState(url: String) {
        updateUrl(url)
    }

    private fun updateUrl(url: String) {
        secure.visibility = if (url.startsWith("https://")) View.VISIBLE else View.GONE
        urlBar.setText(url)
    }

    companion object {
        const val JS_INTERFACE = "Jelly"

        const val SYNC_URL_JS = """
            (() => {
                if (!window.registeredPopState) {
                    window.registeredPopState = true;
                    window.addEventListener('popstate', () => {
                        $JS_INTERFACE.onPopState(window.location.href);
                    });
                }
                if (!window.originalPushState) {
                    window.originalPushState = window.history.pushState;
                }
                if (!window.originalReplaceState) {
                    window.originalReplaceState = window.history.replaceState;
                }

                window.history.pushState = function (state, title, url) {
                    window.originalPushState.apply(window.history, [state, title, url]);
                    $JS_INTERFACE.onPushState(window.location.href);
                };

                window.history.replaceState = function (state, title, url) {
                    window.originalReplaceState.apply(window.history, [state, title, url]);
                    $JS_INTERFACE.onReplaceState(window.location.href);
                };
            })();
        """
    }
}
