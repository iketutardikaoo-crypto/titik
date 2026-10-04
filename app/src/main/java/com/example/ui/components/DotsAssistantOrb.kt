package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DotCoral
import com.example.ui.theme.DotCyan
import com.example.ui.theme.DotGold
import com.example.ui.theme.DotMint
import com.example.ui.theme.DotPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DotsAssistantOrb(
    isThinking: Boolean = false,
    isSpeaking: Boolean = false,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsOrb")

    // Rotation angle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 2000 else if (isSpeaking) 3500 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbRotation"
    )

    // Breathing pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isSpeaking) 1.25f else if (isThinking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 300 else if (isThinking) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    // Glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isThinking || isSpeaking) 0.8f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val baseRadius = (this.size.minDimension / 2) * 0.7f * pulse

            // 1. Central Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = glowAlpha),
                        DotCyan.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.4f
                ),
                radius = baseRadius * 1.4f,
                center = center
            )

            // 2. Core Orb Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = baseRadius * 0.75f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Orbiting Colorful Dots
            val dotColors = listOf(DotCoral, DotCyan, DotGold, DotMint, DotPurple)
            val dotCount = dotColors.size
            val rotRad = Math.toRadians(rotation.toDouble())

            for (i in 0 until dotCount) {
                val angle = rotRad + (2 * Math.PI * i / dotCount)
                val dotX = (center.x + baseRadius * cos(angle)).toFloat()
                val dotY = (center.y + baseRadius * sin(angle)).toFloat()
                val dotSize = 6.5.dp.toPx() * (if (isSpeaking || isThinking) 1.2f else 1.0f)

                // Glow behind orbiting dot
                drawCircle(
                    color = dotColors[i].copy(alpha = 0.5f),
                    radius = dotSize * 1.8f,
                    center = Offset(dotX, dotY)
                )

                // Main orbiting dot
                drawCircle(
                    color = dotColors[i],
                    radius = dotSize,
                    center = Offset(dotX, dotY)
                )
            }

            // 4. Center Pulsing Dot
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx() * (if (isThinking) pulse else 1f),
                center = center
            )
        }
    }
}
