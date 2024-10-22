package io.github.saeeddev94.lmod

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class Clipboard {
    companion object {
        fun copy(context: Context, text: String) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(null, text)
            clipboardManager.setPrimaryClip(clipData)
        }
    }
}
