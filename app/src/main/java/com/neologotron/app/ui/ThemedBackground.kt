package com.neologotron.app.ui

import android.graphics.Paint as AndroidPaint
import android.graphics.RuntimeShader
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neologotron.app.theme.ThemeStyle
import kotlinx.coroutines.android.awaitFrame

enum class AnimatedBackgroundIntensity { LOW, MEDIUM, HIGH }

@Composable
fun ThemedBackground(
    enabled: Boolean,
    style: ThemeStyle,
    intensity: AnimatedBackgroundIntensity = AnimatedBackgroundIntensity.MEDIUM,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        val scale = when (intensity) {
            AnimatedBackgroundIntensity.LOW -> 0.06f
            AnimatedBackgroundIntensity.MEDIUM -> 0.10f
            AnimatedBackgroundIntensity.HIGH -> 0.16f
        }
        var tSeconds by remember { mutableStateOf(0f) }
        // Drive time only when enabled and style needs it
        LaunchedEffect(enabled, style, reduceMotion) {
            if (!enabled || reduceMotion) return@LaunchedEffect
            while (true) {
                val frameTimeNanos = awaitFrame()
                tSeconds = frameTimeNanos / 1_000_000_000f
            }
        }

        // Prepare shaders once in composable scope (source from resources, with assets override)
        val context = LocalContext.current
        val retroShader: RuntimeShader? = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val src = loadShaderSource(context, com.neologotron.app.R.raw.retro80s_agsl, "shaders/retro80s_agsl.aglsl")
                RuntimeShader(src)
            } else null
        }

        val cyberShader: RuntimeShader? = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val src = loadShaderSource(context, com.neologotron.app.R.raw.cyberpunk_agsl, "shaders/cyberpunk_agsl.aglsl")
                RuntimeShader(src)
            } else null
        }

        val themeBg = MaterialTheme.colorScheme.background
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val colors: List<Color> = when (style) {
                ThemeStyle.MINIMAL -> listOf(
                    Color.Black.copy(alpha = 0f),
                    Color.Black.copy(alpha = scale)
                )
                ThemeStyle.RETRO80S -> listOf(
                    Color(0xFFFF6EC7).copy(alpha = 0f),
                    Color(0xFF40E0D0).copy(alpha = scale)
                )
                ThemeStyle.CYBERPUNK -> listOf(
                    Color(0xFFFF00A6).copy(alpha = 0f),
                    Color(0xFF00FFF0).copy(alpha = scale)
                )
            }
            val useRetroShader = enabled && style == ThemeStyle.RETRO80S && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            val useCyberShader = enabled && style == ThemeStyle.CYBERPUNK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

            if (!enabled) {
                // Dynamic background disabled: paint solid theme background for proper contrast
                drawRect(color = themeBg, size = Size(w, h))
            } else if (useRetroShader) {
                val agsl = retroShader!!
                agsl.setFloatUniform("iResolution", w, h)
                agsl.setFloatUniform("iTime", tSeconds)
                agsl.setFloatUniform(
                    "iIntensity",
                    when (intensity) {
                        AnimatedBackgroundIntensity.LOW -> 0.6f
                        AnimatedBackgroundIntensity.MEDIUM -> 1.0f
                        AnimatedBackgroundIntensity.HIGH -> 1.6f
                    }
                )
                agsl.setFloatUniform(
                    "uOpacity",
                    when (intensity) {
                        AnimatedBackgroundIntensity.LOW -> 0.18f
                        AnimatedBackgroundIntensity.MEDIUM -> 0.26f
                        AnimatedBackgroundIntensity.HIGH -> 0.34f
                    }
                )
                // Theme accent colors
                val p = Color(0xFFFF6EC7)
                val s = Color(0xFF40E0D0)
                agsl.setFloatUniform("uPrimary", p.red, p.green, p.blue)
                agsl.setFloatUniform("uSecondary", s.red, s.green, s.blue)
                agsl.setFloatUniform("uBgColor", themeBg.red, themeBg.green, themeBg.blue)

                val paint = AndroidPaint().apply { shader = agsl }
                drawContext.canvas.nativeCanvas.drawRect(0f, 0f, w, h, paint)
            } else if (useCyberShader) {
                val agsl = cyberShader!!
                agsl.setFloatUniform("iResolution", w, h)
                agsl.setFloatUniform("iTime", tSeconds)
                agsl.setFloatUniform(
                    "iIntensity",
                    when (intensity) {
                        AnimatedBackgroundIntensity.LOW -> 0.8f
                        AnimatedBackgroundIntensity.MEDIUM -> 1.0f
                        AnimatedBackgroundIntensity.HIGH -> 1.3f
                    }
                )
                val aspect = if (h > 0f) w / h else 1f
                agsl.setFloatUniform("uAspect", aspect)
                // Base size roughly similar to the reference; scale with density
                val sizeXY = 0.3f
                agsl.setFloatUniform("uSize", sizeXY, sizeXY)
                agsl.setFloatUniform("uSpeed", 0.7f)
                agsl.setFloatUniform("uYSpread", 1.6f)
                agsl.setFloatUniform("uBasePulse", 0.33f)
                // Magenta/Cyan accents for cyberpunk
                val c1 = Color(0xFF00FFF0) // cyan
                val c2 = Color(0xFFFF00A6) // magenta
                agsl.setFloatUniform("uColor1", c1.red, c1.green, c1.blue)
                agsl.setFloatUniform("uColor2", c2.red, c2.green, c2.blue)
                agsl.setFloatUniform("uBgColor", themeBg.red, themeBg.green, themeBg.blue)
                agsl.setFloatUniform(
                    "uOpacity",
                    when (intensity) {
                        AnimatedBackgroundIntensity.LOW -> 0.16f
                        AnimatedBackgroundIntensity.MEDIUM -> 0.24f
                        AnimatedBackgroundIntensity.HIGH -> 0.32f
                    }
                )

                val paint = AndroidPaint().apply { shader = agsl }
                drawContext.canvas.nativeCanvas.drawRect(0f, 0f, w, h, paint)
            } else {
                // Fallback (API < 33 or styles without shader): use a gentle gradient based on current style
                val brush = Brush.verticalGradient(
                    colors = listOf(colors.first(), colors.last()),
                    startY = 0f,
                    endY = h
                )
                drawRect(brush = brush, size = Size(w, h))
            }
        }
        content()
    }
}

private fun loadShaderSource(context: Context, @androidx.annotation.RawRes rawId: Int, assetName: String): String {
    return try {
        context.assets.open(assetName).bufferedReader().use { it.readText() }
    } catch (_: Throwable) {
        context.resources.openRawResource(rawId).bufferedReader().use { it.readText() }
    }
}
