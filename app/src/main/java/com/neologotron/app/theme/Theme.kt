package com.neologotron.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.neologotron.app.ui.viewmodel.SettingsViewModel

enum class ThemeStyle { MINIMAL, RETRO80S, CYBERPUNK }

private fun minimalLight() = lightColorScheme(
    primary = Color(0xFF1F1F1F),
    secondary = Color(0xFF3A3A3A),
    tertiary = Color(0xFF5E5E5E),
)
private fun minimalDark() = darkColorScheme(
    primary = Color(0xFFE6E6E6),
    secondary = Color(0xFFBDBDBD),
    tertiary = Color(0xFF9E9E9E),
)

private fun retro80s() = darkColorScheme(
    primary = Color(0xFFFF6EC7), // neon pink
    secondary = Color(0xFF40E0D0), // turquoise
    tertiary = Color(0xFF7DF9FF), // electric blue
)

private fun cyberpunk() = darkColorScheme(
    primary = Color(0xFFFF00A6), // magenta
    secondary = Color(0xFF00FFF0), // cyan
    tertiary = Color(0xFFFFF000), // yellow-ish
)

private fun schemeFor(style: ThemeStyle, dark: Boolean): ColorScheme = when (style) {
    ThemeStyle.MINIMAL -> if (dark) minimalDark() else minimalLight()
    ThemeStyle.RETRO80S -> retro80s()
    ThemeStyle.CYBERPUNK -> cyberpunk()
}

@Composable
fun NeologotronTheme(
    style: ThemeStyle,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = schemeFor(style, darkTheme),
        content = content
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val vm: SettingsViewModel = hiltViewModel()
    val themeStyle by vm.theme.collectAsState()
    val dark by vm.darkTheme.collectAsState()
    NeologotronTheme(style = themeStyle, darkTheme = dark) { content() }
}
