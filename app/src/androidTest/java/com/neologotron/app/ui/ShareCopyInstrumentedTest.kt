package com.neologotron.app.ui

import android.app.Instrumentation
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareCopyInstrumentedTest {
    @Test
    fun shareWord_sendsChooserIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Intents.init()
        val expected = hasAction(Intent.ACTION_CHOOSER)
        intending(expected).respondWith(Instrumentation.ActivityResult(0, null))
        shareWord(context, "mot", "définition")
        intended(expected)
        Intents.release()
    }

    @Test
    fun copyToClipboard_setsPrimaryClip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        copyToClipboard(context, "label", "copie")
        assertEquals("copie", clipboard.primaryClip?.getItemAt(0)?.text)
    }
}
