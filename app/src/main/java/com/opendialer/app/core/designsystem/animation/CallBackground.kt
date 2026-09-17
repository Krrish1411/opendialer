package com.opendialer.app.core.designsystem.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.random.Random

enum class CallAnimationStyle {
    AURA_PARTICLES,
    FLOWING_WAVE,
    COSMIC_GRADIENT,
    MINIMAL_DARK
}

private data class Particle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

@Composable
fun CallBackground(
    style: CallAnimationStyle = CallAnimationStyle.AURA_PARTICLES,
    modifier: Modifier = Modifier
) {
    when (style) {
        CallAnimationStyle.AURA_PARTICLES -> AuraParticlesBackground(modifier)
        CallAnimationStyle.FLOWING_WAVE -> FlowingWaveBackground(modifier)
        CallAnimationStyle.COSMIC_GRADIENT -> CosmicGradientBackground(modifier)
        CallAnimationStyle.MINIMAL_DARK -> MinimalDarkBackground(modifier)
    }
}

@Composable
fun AuraParticlesBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember {
        List(25) {
            Particle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = Random.nextFloat() * 18f + 8f,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                color = if (it % 2 == 0) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF06B6D4).copy(alpha = 0.35f)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            particles.forEach { p ->
                val currentY = ((p.yRatio + phase * p.speed) % 1f) * h
                val currentX = (p.xRatio * w) + sin((phase + p.xRatio) * 6.28f) * 30f
                drawCircle(
                    color = p.color,
                    radius = p.radius,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

@Composable
fun FlowingWaveBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070B14), Color(0xFF10172A), Color(0xFF090D1A))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val path1 = Path()
            val path2 = Path()

            path1.moveTo(0f, h * 0.65f)
            path2.moveTo(0f, h * 0.75f)

            for (x in 0..w.toInt() step 10) {
                val y1 = h * 0.65f + sin((x / w * 4f) + waveOffset) * 45f
                val y2 = h * 0.75f + sin((x / w * 3f) - waveOffset) * 35f
                path1.lineTo(x.toFloat(), y1)
                path2.lineTo(x.toFloat(), y2)
            }

            drawPath(
                path = path1,
                color = Color(0xFF8B5CF6).copy(alpha = 0.4f),
                style = Stroke(width = 4f)
            )
            drawPath(
                path = path2,
                color = Color(0xFF06B6D4).copy(alpha = 0.35f),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun CosmicGradientBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3B0764),
                        Color(0xFF0C162D),
                        Color(0xFF030712)
                    ),
                    center = Offset(400f + animOffset, 600f - animOffset),
                    radius = 900f
                )
            )
    )
}

@Composable
fun MinimalDarkBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    )
}
