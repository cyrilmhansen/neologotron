package com.neologotron.app.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.neologotron.app.theme.ThemeStyle

@RunWith(AndroidJUnit4::class)
class ThemedBackgroundPowerSaveInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun timeAdvances_whenPowerSaveOff() {
        composeRule.mainClock.autoAdvance = false
        var latest = 0f
        composeRule.setContent {
            ThemedBackground(
                enabled = true,
                style = ThemeStyle.RETRO80S,
                debugPowerSaveOverride = false,
                debugTimeListener = { latest = it },
            ) {}
        }
        composeRule.mainClock.advanceTimeBy(16L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()
        assertTrue(latest > 0f)
    }

    @Test
    fun timeStops_whenPowerSaveOn() {
        composeRule.mainClock.autoAdvance = false
        var latest = 0f
        composeRule.setContent {
            ThemedBackground(
                enabled = true,
                style = ThemeStyle.RETRO80S,
                debugPowerSaveOverride = true,
                debugTimeListener = { latest = it },
            ) {}
        }
        composeRule.mainClock.advanceTimeBy(16L)
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()
        assertEquals(0f, latest, 0.0001f)
    }
}
