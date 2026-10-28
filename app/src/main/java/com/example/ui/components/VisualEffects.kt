package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NexusCardBg
import kotlin.math.sin

/**
 * Dynamic Neon Glow Border:
 * Wraps content in a 24.dp rounded container with a multi-color fluid gradient
 * neon border that shifts between cyan, blue, purple, and pink, pulsing actively
 * in sync with voice frequency or system processing.
 */
@Composable
fun DynamicNeonGlowBorder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 2.dp,
    frequencyLevel: Float = 0f,
    isProcessing: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_flow")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 1500 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeIntensity = (frequencyLevel * 1.5f).coerceAtLeast(if (isProcessing) pulseScale else 1f)

    val gradientColors = listOf(
        NeonCyan,
        NeonBlue,
        NeonPurple,
        NeonPink,
        NeonCyan
    )

    val brush = Brush.sweepGradient(
        colors = gradientColors
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .drawBehind {
                val glowAlpha = (0.25f * activeIntensity).coerceIn(0.1f, 0.7f)
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension / 1.5f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            }
            .border(
                width = borderWidth * (1f + frequencyLevel * 0.5f),
                brush = brush,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Three-Dot Waving Loading Animation:
 * Triggered during web search and data research states.
 */
@Composable
fun ThreeDotWavingLoader(
    modifier: Modifier = Modifier,
    statusText: String = "ওয়েব অনুসন্ধান ও গবেষণা চলছে..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_wave")

    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .testTag("three_dot_loader")
            .clip(RoundedCornerShape(16.dp))
            .background(NexusCardBg.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-6 * anim1).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NeonCyan)
        )
        Box(
            modifier = Modifier
                .offset(y = (-6 * anim2).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NeonPurple)
        )
        Box(
            modifier = Modifier
                .offset(y = (-6 * anim3).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(NeonPink)
        )

        Text(
            text = statusText,
            color = NeonCyan,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/**
 * Wavy Image Generation Border:
 * While generating an image, displays an undulating neon wavy glow.
 */
@Composable
fun WavyImagePlaceholder(
    modifier: Modifier = Modifier,
    prompt: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_border")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(NexusCardBg)
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                val waveAmplitude = 5.dp.toPx()

                // Draw undulating fluid glow
                val path = androidx.compose.ui.graphics.Path()
                val w = size.width
                val h = size.height

                // Draw glowing outline with sinusoidal distortion
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan,
                            NeonPurple,
                            NeonPink,
                            NeonCyan
                        )
                    ),
                    style = Stroke(width = strokeWidth + sin(wavePhase) * waveAmplitude),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThreeDotWavingLoader(statusText = "ইমেজ প্রস্তুত হচ্ছে: \"$prompt\"")
        }
    }
}

/**
 * Audio Waveform Visualizer:
 * Visual frequency bars that dance in sync with user speech or TTS Bengali playback.
 */
@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    frequencyLevel: Float = 0f,
    isSpeaking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 60, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, delayMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    val multiplier = if (isSpeaking) 1.2f else if (frequencyLevel > 0.05f) frequencyLevel * 1.5f else 0.2f

    Row(
        modifier = modifier
            .testTag("audio_waveform_visualizer")
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(bar1, bar2, bar3, bar4, bar2, bar1).forEachIndexed { index, barHeight ->
            val color = when (index % 3) {
                0 -> NeonCyan
                1 -> NeonPurple
                else -> NeonPink
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((barHeight * multiplier).coerceIn(4f, 38f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
