package com.neologotron.app.ui

import android.graphics.Paint as AndroidPaint
import android.graphics.RuntimeShader
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
import androidx.compose.ui.platform.LocalDensity
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

        // Prepare shaders once in composable scope (not inside draw lambda)
        val retroShader: RuntimeShader? = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RuntimeShader(
                    """
                    uniform float2 iResolution;
                    uniform float iTime;
                    uniform float iIntensity;
                    uniform float uOpacity;
                    uniform float3 uPrimary;   // neon pink
                    uniform float3 uSecondary; // turquoise
                    uniform float3 uBgColor;   // material background

                    float fractf(float x) { return x - floor(x); }
                    float2 fract2(float2 x) { return x - floor(x); }
                    float hash21(float2 p) {
                        p = fract2(p * float2(123.34, 345.45));
                        p += dot(p, p + 34.345);
                        return fractf(p.x * p.y);
                    }

                    float noise(float2 p) {
                        float2 i = floor(p);
                        float2 f = p - i;
                        float a = hash21(i);
                        float b = hash21(i + float2(1.0, 0.0));
                        float c = hash21(i + float2(0.0, 1.0));
                        float d = hash21(i + float2(1.0, 1.0));
                        float2 u = f*f*(3.0-2.0*f);
                        return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
                    }

                    half4 main(float2 fragCoord) {
                        float2 uv = fragCoord / iResolution;
                        float t = iTime * 0.6;

                        // Scanlines (soft, non-distracting)
                        float scan = 0.90 + 0.10 * sin((uv.y * iResolution.y) * 3.14159 + t * 5.0);

                        // Two layers of noise for richer grain
                        float n1 = noise(uv * iResolution * 0.35 + float2(0.0, t * 24.0));
                        float n2 = noise(uv * iResolution * 1.0 + float2(t * 8.0, 0.0));
                        float grain = mix(n1, n2, 0.5);
                        grain = pow(grain, 1.25);

                        // Horizontal jitter, very subtle
                        float jitter = (noise(float2(t * 3.0, uv.y * 36.0)) - 0.5) * (0.002 + 0.002*iIntensity);
                        float x = clamp(uv.x + jitter, 0.0, 1.0);

                        // Luminance and tint
                        float luma = grain * scan;
                        float3 base = mix(uPrimary, uSecondary, luma);

                        // Slight chromatic shift
                        float r = mix(base.r, luma, 0.12);
                        float g = mix(base.g, luma, 0.08);
                        float b = mix(base.b, luma, 0.16);

                        float3 fx = float3(r, g, b);
                        // Blend with background using opacity
                        float3 outc = mix(uBgColor, fx, clamp(uOpacity, 0.0, 1.0));
                        return half4(outc, 1.0);
                    }
                    """.trimIndent()
                )
            } else null
        }

        val cyberShader: RuntimeShader? = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RuntimeShader(
                    """
                    uniform float2 iResolution;
                    uniform float iTime;
                    uniform float iIntensity;
                    uniform float uAspect;
                    uniform float2 uSize;
                    uniform float uSpeed;
                    uniform float uYSpread;
                    uniform float uBasePulse;
                    uniform float3 uColor1;
                    uniform float3 uColor2;
                    uniform float3 uBgColor;
                    uniform float uOpacity;

                    float fractf(float x) { return x - floor(x); }
                    float rand1(float x) { return fractf(sin(x) * 4358.5453123); }
                    float rand2f(float2 co) { return fractf(sin(dot(co.xy ,float2(12.9898,78.233))) * 43758.5357); }

                    float pulseColor(float t, float basePulse) {
                        float myPulse = basePulse + sin(t) * 0.1;
                        return myPulse < 1.0 ? myPulse : 1.0;
                    }

                    float boxSDF(float2 p, float2 b, float r) {
                      return length(max(abs(p)-b,0.0))-r;
                    }

                    half4 main(float2 fragCoord) {
                        float2 uv = fragCoord / iResolution - 0.5;
                        float3 color = float3(0.0);
                        float pulse = pulseColor(iTime, uBasePulse);
                        float3 baseColor = uv.x > 0.0 ? uColor1 : uColor2;
                        float colFactor = pulse * 0.5 * (0.9 - cos(uv.x * 8.0));
                        color += colFactor * baseColor;
                        uv.x *= uAspect;

                        // Fixed loop bound for AGSL
                        for (int i = 0; i < 70; i++) {
                            float z = 1.0 - 0.7 * rand1(float(i) * 1.4333);
                            float tickTime = iTime * z * uSpeed + float(i) * 1.23753;
                            float tick = floor(tickTime);

                            float sgnx = uv.x >= 0.0 ? 1.0 : -1.0;
                            float2 pos = float2(0.6 * uAspect * (rand1(tick) - 0.5), sgnx * uYSpread * (0.5 - fractf(tickTime)));
                            pos.x += 0.24 * (pos.x >= 0.0 ? 1.0 : -1.0);
                            if (abs(pos.x) < 0.1) pos.x += 1.0; // guard when sign is zero

                            float b = boxSDF(uv - pos, uSize, 0.01);
                            float dust  = z * smoothstep(0.22, 0.0, b) * pulse * 0.5 * iIntensity;
                            float block = 0.2 * z * smoothstep(0.002, 0.0, b);
                            float shine = 0.6 * z * pulse * smoothstep(-0.002, b, 0.007) * iIntensity;
                            color += dust * baseColor + block * z + shine;
                        }

                        // Fine film grain
                        color -= rand2f(uv) * 0.02;
                        // Gentle vignette to reduce center impact
                        float v = smoothstep(0.8, 0.2, length(uv));
                        color *= mix(0.82, 1.0, v);
                        float3 outc = mix(uBgColor, color, clamp(uOpacity, 0.0, 1.0));
                        return half4(outc, 1.0);
                    }
                    """.trimIndent()
                )
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
