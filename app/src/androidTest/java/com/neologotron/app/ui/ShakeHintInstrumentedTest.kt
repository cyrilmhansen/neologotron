package com.neologotron.app.ui

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.neologotron.app.data.repo.SettingsRepository
import com.neologotron.app.ui.viewmodel.SettingsViewModel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShakeHintInstrumentedTest {
    private lateinit var context: Context
    private lateinit var repo: SettingsRepository

    @Before
    fun setup() =
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            repo = SettingsRepository(context)
            // Ensure a known starting state
            repo.setShakeHintShown(false)
        }

    @After
    fun tearDown() =
        runBlocking {
            // Reset to default to avoid polluting other tests/sessions
            repo.setShakeHintShown(false)
        }

    @Test
    fun shakeHint_isShownOnlyOnce_afterMarking() =
        runBlocking {
            // Initially should be false
            val initial = repo.shakeHintShown.first()
            assertFalse(initial)

            val vm = SettingsViewModel(repo)
            vm.markShakeHintShown()
            val afterMark = repo.shakeHintShown.first()
            assertTrue(afterMark)

            // Marking again keeps it true
            vm.markShakeHintShown()
            val afterSecondMark = repo.shakeHintShown.first()
            assertTrue(afterSecondMark)
        }
}
