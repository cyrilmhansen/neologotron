package com.neologotron.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import com.neologotron.app.theme.AppTheme
import com.neologotron.app.ui.WordTheatreHost
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.neologotron.app.ui.ThemedBackground
import com.neologotron.app.ui.viewmodel.SettingsViewModel
import android.animation.ValueAnimator
import com.neologotron.app.ui.AnimatedBackgroundIntensity
import com.neologotron.app.theme.ThemeStyle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val vm: SettingsViewModel = hiltViewModel()
                val style by vm.theme.collectAsState()
                val animated by vm.animatedBackgroundsEnabled.collectAsState()
                val intensity by vm.animatedBackgroundsIntensity.collectAsState()
                val reduceMotion = !ValueAnimator.areAnimatorsEnabled()

                ThemedBackground(
                    enabled = animated,
                    style = style,
                    intensity = intensity,
                    reduceMotion = reduceMotion,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        WordTheatreHost()
                    }
                }
            }
        }
    }
}
