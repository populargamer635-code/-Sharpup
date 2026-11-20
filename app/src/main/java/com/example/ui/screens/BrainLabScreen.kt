package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChildProfile
import com.example.model.GameMode
import com.example.model.GameSession
import com.example.ui.components.BrainRadarChart
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkCoral
import com.example.ui.theme.SparkMint
import com.example.ui.theme.SparkViolet
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.viewmodel.UiState

@Composable
fun BrainLabScreen(
    profile: ChildProfile,
    sessionsState: UiState<List<GameSession>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val brainTitle = when {
        profile.brainScore >= 750 -> "Super Brain Master"
        profile.brainScore >= 500 -> "Math Whiz Explorer"
        profile.brainScore >= 250 -> "Sharp Thinker"
        else -> "Rising Spark"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.brain_sharpness_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SparkCobalt, Color(0xFF3B82F6), SparkViolet)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = brainTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${profile.brainScore} / 999",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${profile.overallAccuracyPercent}% overall accuracy across ${profile.sessionsCompleted} adventures",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Psychology,
                                    contentDescription = "Brain Power",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "4-Pillar Brain Map",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        BrainRadarChart(
                            accuracy = profile.arithmeticSkill.toInt(),
                            speed = profile.speedSkill.toInt(),
                            logic = profile.logicSkill.toInt(),
                            memory = profile.memorySkill.toInt(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SkillProgressRow(
                                label = "Accuracy (Number Quest)",
                                value = profile.arithmeticSkill.toInt(),
                                color = SparkCobalt
                            )
                            SkillProgressRow(
                                label = "Speed (Lightning Sprint)",
                                value = profile.speedSkill.toInt(),
                                color = SparkCoral
                            )
                            SkillProgressRow(
                                label = "Logic (Pattern Detective)",
                                value = profile.logicSkill.toInt(),
                                color = SparkMint
                            )
                            SkillProgressRow(
                                label = "Memory (Brain Chain & Scale)",
                                value = profile.memorySkill.toInt(),
                                color = SparkViolet
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.recent_adventures),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when (sessionsState) {
                is UiState.Loading -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Loading your adventures...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sessionsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    val sessions = sessionsState.data
                    if (sessions.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = SparkCobalt,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = stringResource(R.string.no_sessions_yet),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(sessions, key = { it.id.ifBlank { "${it.mode}_${it.score}_${it.durationSeconds}" } }) { session ->
                            SessionHistoryCard(session = session)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SkillProgressRow(
    label: String,
    value: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value / 100",
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = color.copy(alpha = 0.16f),
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

@Composable
private fun SessionHistoryCard(session: GameSession) {
    val mode = session.modeEnum
    val (icon, accent) = when (mode) {
        GameMode.ARITHMETIC -> Icons.Rounded.Calculate to SparkCobalt
        GameMode.SPEED_SPRINT -> Icons.Rounded.Bolt to SparkCoral
        GameMode.PATTERN_LOGIC -> Icons.Rounded.Extension to SparkMint
        GameMode.MEMORY_BALANCE -> Icons.Rounded.Psychology to SparkViolet
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mode.title,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${session.tierEnum.label} • ${session.correctCount}/${session.totalQuestions} right • ${session.durationSeconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = SparkAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "+${session.starsEarned}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
