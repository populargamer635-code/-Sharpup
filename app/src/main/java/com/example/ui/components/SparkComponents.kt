package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CrueltyFree
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AvatarOption
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkCoral
import com.example.ui.theme.SparkMint
import com.example.ui.theme.SparkViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

fun avatarVisual(avatar: AvatarOption): Pair<ImageVector, Color> = when (avatar) {
    AvatarOption.OWL -> Icons.Rounded.AutoAwesome to SparkCobalt
    AvatarOption.FOX -> Icons.Rounded.Pets to SparkCoral
    AvatarOption.PANDA -> Icons.Rounded.Face to SparkMint
    AvatarOption.ROCKET -> Icons.Rounded.RocketLaunch to SparkViolet
    AvatarOption.STAR -> Icons.Rounded.Star to SparkAmber
    AvatarOption.DINO -> Icons.Rounded.CrueltyFree to Color(0xFF0EA5E9)
}

@Composable
fun MascotAvatarBadge(
    avatar: AvatarOption,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val (icon, color) = avatarVisual(avatar)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(color, color.copy(alpha = 0.72f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = avatar.label,
            tint = Color.White,
            modifier = Modifier.size(size * 0.56f)
        )
    }
}

@Composable
fun StatBadgeChip(
    icon: ImageVector,
    value: String,
    tint: Color,
    containerColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountingBlocksVisual(
    leftCount: Int,
    rightCount: Int,
    operator: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (operator == "−") {
                Text(
                    text = "Start with $leftCount blocks, cross out $rightCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val remaining = (leftCount - rightCount).coerceAtLeast(0)
                    repeat(leftCount) { index ->
                        val isCrossed = index >= remaining
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    if (isCrossed) SparkCoral.copy(alpha = 0.25f) else SparkCobalt
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCrossed) {
                                Text(
                                    text = "×",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SparkCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "$leftCount blue blocks + $rightCount green blocks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(leftCount) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SparkCobalt)
                            )
                        }
                    }
                    Text(
                        text = "  +  ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(rightCount) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SparkMint)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrainRadarChart(
    accuracy: Int,
    speed: Int,
    logic: Int,
    memory: Int,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val fillBrush = Brush.radialGradient(
        colors = listOf(
            SparkCobalt.copy(alpha = 0.45f),
            SparkMint.copy(alpha = 0.30f)
        )
    )
    val strokeColor = SparkCobalt

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(210.dp)
                .padding(18.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Concentric diamond rings
            listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { scale ->
                val r = radius * scale
                val ringPath = Path().apply {
                    moveTo(center.x, center.y - r)
                    lineTo(center.x + r, center.y)
                    lineTo(center.x, center.y + r)
                    lineTo(center.x - r, center.y)
                    close()
                }
                drawPath(ringPath, color = gridColor, style = Stroke(width = 2f))
            }

            // Cross axes
            drawLine(gridColor, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = 2f)
            drawLine(gridColor, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 2f)

            val topNorm = (accuracy.coerceIn(10, 100)) / 100f
            val rightNorm = (speed.coerceIn(10, 100)) / 100f
            val bottomNorm = (logic.coerceIn(10, 100)) / 100f
            val leftNorm = (memory.coerceIn(10, 100)) / 100f

            val topPt = Offset(center.x, center.y - radius * topNorm)
            val rightPt = Offset(center.x + radius * rightNorm, center.y)
            val bottomPt = Offset(center.x, center.y + radius * bottomNorm)
            val leftPt = Offset(center.x - radius * leftNorm, center.y)

            val statPath = Path().apply {
                moveTo(topPt.x, topPt.y)
                lineTo(rightPt.x, rightPt.y)
                lineTo(bottomPt.x, bottomPt.y)
                lineTo(leftPt.x, leftPt.y)
                close()
            }
            drawPath(statPath, brush = fillBrush)
            drawPath(statPath, color = strokeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

            listOf(
                topPt to SparkCobalt,
                rightPt to SparkCoral,
                bottomPt to SparkMint,
                leftPt to SparkViolet
            ).forEach { (pt, dotColor) ->
                drawCircle(color = Color.White, radius = 9f, center = pt)
                drawCircle(color = dotColor, radius = 6f, center = pt)
            }
        }

        // Axis labels around the diamond
        Text(
            text = "Accuracy",
            style = MaterialTheme.typography.labelMedium,
            color = SparkCobalt,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "Speed",
            style = MaterialTheme.typography.labelMedium,
            color = SparkCoral,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Text(
            text = "Logic",
            style = MaterialTheme.typography.labelMedium,
            color = SparkMint,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Text(
            text = "Memory",
            style = MaterialTheme.typography.labelMedium,
            color = SparkViolet,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

private data class ConfettiParticle(
    val xFrac: Float,
    val speed: Float,
    val size: Float,
    val phase: Float,
    val color: Color
)

@Composable
fun CelebrationConfettiOverlay(modifier: Modifier = Modifier) {
    val particles = remember {
        val colors = listOf(SparkCobalt, SparkAmber, SparkMint, SparkCoral, SparkViolet)
        List(32) { idx ->
            ConfettiParticle(
                xFrac = Random.nextFloat(),
                speed = 0.6f + Random.nextFloat() * 0.9f,
                size = 7f + Random.nextFloat() * 9f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                color = colors[idx % colors.size]
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 2800, easing = LinearEasing))
    }

    val infinite = rememberInfiniteTransition(label = "confetti_sway")
    val sway by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "sway"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        if (t < 1f) {
            particles.forEach { p ->
                val y = (t * size.height * 1.25f * p.speed) % (size.height + 40f)
                val x = (p.xFrac * size.width) + sin(t * 10f + p.phase) * 24f + sway * 8f
                val alpha = (1f - t).coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun HeaderMetricsRow(
    stars: Long,
    streakDays: Long,
    brainScore: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatBadgeChip(
            icon = Icons.Rounded.Star,
            value = stars.toString(),
            tint = SparkAmber,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentDescription = "Total Stars"
        )
        StatBadgeChip(
            icon = Icons.Rounded.LocalFireDepartment,
            value = "${streakDays}d",
            tint = SparkCoral,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentDescription = "Day Streak"
        )
        StatBadgeChip(
            icon = Icons.Rounded.Psychology,
            value = brainScore.toString(),
            tint = SparkCobalt,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentDescription = "Brain Score"
        )
    }
}
