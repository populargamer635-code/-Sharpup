package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChildProfile
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.ui.components.HeaderMetricsRow
import com.example.ui.components.MascotAvatarBadge
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkCoral
import com.example.ui.theme.SparkMint
import com.example.ui.theme.SparkViolet

@Composable
fun HomePlayScreen(
    profile: ChildProfile,
    onStartGame: (GameMode, DifficultyTier) -> Unit,
    onSelectDifficulty: (DifficultyTier) -> Unit,
    onOpenBrainLab: () -> Unit,
    onOpenTrophies: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTier = profile.tierEnum

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        MascotAvatarBadge(avatar = profile.avatarEnum, size = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hi, ${profile.childName}!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Ready to power up your brain?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                HeaderMetricsRow(
                    stars = profile.totalStars,
                    streakDays = profile.currentStreakDays,
                    brainScore = profile.brainScore
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickHubButton(
                        title = "Brain Lab",
                        subtitle = "${profile.overallAccuracyPercent}% accuracy",
                        icon = Icons.Rounded.Psychology,
                        accent = SparkCobalt,
                        onClick = onOpenBrainLab,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_open_brain_lab")
                    )
                    QuickHubButton(
                        title = "Trophies",
                        subtitle = "${profile.unlockedBadges.size}/8 earned",
                        icon = Icons.Rounded.AutoAwesome,
                        accent = SparkAmber,
                        onClick = onOpenTrophies,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_open_trophies")
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_quest_hero_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_brain_quest_banner),
                            contentDescription = "MathSpark colorful adventure island",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xE60F172A),
                                            Color(0x991E3A8A),
                                            Color(0x331E3A8A)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = SparkAmber,
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF451A03),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.daily_brain_quest),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF451A03)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Brain Warmup Challenge",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "10 fun puzzles • Earn bonus stars",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }

                            Button(
                                onClick = { onStartGame(GameMode.ARITHMETIC, activeTier) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = SparkCobalt
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("hero_play_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Quest",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.skill_level_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyTier.entries.forEach { tier ->
                            val selected = tier == activeTier
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectDifficulty(tier) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = tier.label,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            text = tier.ageHint,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SparkCobalt,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tier_chip_${tier.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.choose_adventure),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GameModeCard(
                        mode = GameMode.ARITHMETIC,
                        icon = Icons.Rounded.Calculate,
                        accent = SparkCobalt,
                        onClick = { onStartGame(GameMode.ARITHMETIC, activeTier) }
                    )
                    GameModeCard(
                        mode = GameMode.SPEED_SPRINT,
                        icon = Icons.Rounded.Bolt,
                        accent = SparkCoral,
                        onClick = { onStartGame(GameMode.SPEED_SPRINT, activeTier) }
                    )
                    GameModeCard(
                        mode = GameMode.PATTERN_LOGIC,
                        icon = Icons.Rounded.Extension,
                        accent = SparkMint,
                        onClick = { onStartGame(GameMode.PATTERN_LOGIC, activeTier) }
                    )
                    GameModeCard(
                        mode = GameMode.MEMORY_BALANCE,
                        icon = Icons.Rounded.Psychology,
                        accent = SparkViolet,
                        onClick = { onStartGame(GameMode.MEMORY_BALANCE, activeTier) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GameModeCard(
    mode: GameMode,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("game_card_${mode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accent, accent.copy(alpha = 0.72f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = mode.title,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = mode.brainPillar,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = mode.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play ${mode.title}",
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickHubButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
