package com.neologotron.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.app.ShareCompat

fun shareWord(context: Context, word: String, definition: String) {
    val shareText = "$word — $definition"
    ShareCompat.IntentBuilder(context)
        .setType("text/plain")
        .setText(shareText)
        .startChooser()
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
