package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ChildProfile
import com.example.model.LeaderboardEntry
import com.example.ui.components.MascotAvatarBadge
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkCoral
import com.example.ui.theme.SparkMint
import com.example.ui.theme.SparkViolet
import com.example.viewmodel.UiState

private enum class LeaderboardSort(val label: String) {
    BRAIN_SCORE("Brain Power"),
    STARS("Total Stars"),
    STREAK("Day Streak")
}

@Composable
fun LeaderboardScreen(
    currentUserId: String,
    profile: ChildProfile,
    leaderboardState: UiState<List<LeaderboardEntry>>,
    modifier: Modifier = Modifier
) {
    var sortBy by remember { mutableStateOf(LeaderboardSort.BRAIN_SCORE) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Brain Champions",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See how your math power compares with fellow explorers!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeaderboardSort.entries.forEach { metric ->
                        val selected = metric == sortBy
                        FilterChip(
                            selected = selected,
                            onClick = { sortBy = metric },
                            label = {
                                Text(
                                    text = metric.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SparkCobalt,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("leaderboard_sort_${metric.name.lowercase()}")
                        )
                    }
                }
            }

            when (leaderboardState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = leaderboardState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    val sortedEntries = when (sortBy) {
                        LeaderboardSort.BRAIN_SCORE -> leaderboardState.data.sortedWith(
                            compareByDescending<LeaderboardEntry> { it.brainScore }
                                .thenByDescending { it.totalStars }
                        )
                        LeaderboardSort.STARS -> leaderboardState.data.sortedWith(
                            compareByDescending<LeaderboardEntry> { it.totalStars }
                                .thenByDescending { it.brainScore }
                        )
                        LeaderboardSort.STREAK -> leaderboardState.data.sortedWith(
                            compareByDescending<LeaderboardEntry> { it.currentStreakDays }
                                .thenByDescending { it.brainScore }
                        )
                    }

                    val myRankIndex = sortedEntries.indexOfFirst { it.userId == currentUserId }
                    val myRankText = if (myRankIndex >= 0) "#${myRankIndex + 1}" else "#1"

                    item {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(SparkCobalt, SparkViolet)
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        MascotAvatarBadge(avatar = profile.avatarEnum, size = 50.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Your Current Rank: $myRankText",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${profile.brainScore} Brain Score • ${profile.totalStars} Stars",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.88f)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.EmojiEvents,
                                            contentDescription = "Your Rank",
                                            tint = SparkAmber,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (sortedEntries.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = SparkAmber,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Play a game to join the Leaderboard!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        if (sortedEntries.size >= 2) {
                            item {
                                TopPodiumCard(
                                    topThree = sortedEntries.take(3),
                                    sortBy = sortBy
                                )
                            }
                        }

                        itemsIndexed(
                            items = sortedEntries,
                            key = { _, entry -> entry.userId }
                        ) { index, entry ->
                            LeaderboardRowCard(
                                rank = index + 1,
                                entry = entry,
                                isCurrentUser = entry.userId == currentUserId,
                                sortBy = sortBy
                            )
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
private fun TopPodiumCard(
    topThree: List<LeaderboardEntry>,
    sortBy: LeaderboardSort
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val second = topThree.getOrNull(1)
            val first = topThree.getOrNull(0)
            val third = topThree.getOrNull(2)

            if (second != null) {
                PodiumSpot(
                    rank = 2,
                    entry = second,
                    sortBy = sortBy,
                    medalColor = Color(0xFF94A3B8),
                    avatarSize = 52.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            if (first != null) {
                PodiumSpot(
                    rank = 1,
                    entry = first,
                    sortBy = sortBy,
                    medalColor = SparkAmber,
                    avatarSize = 66.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            if (third != null) {
                PodiumSpot(
                    rank = 3,
                    entry = third,
                    sortBy = sortBy,
                    medalColor = Color(0xFFD97706),
                    avatarSize = 48.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumSpot(
    rank: Int,
    entry: LeaderboardEntry,
    sortBy: LeaderboardSort,
    medalColor: Color,
    avatarSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = medalColor.copy(alpha = 0.2f),
            border = BorderStroke(1.5.dp, medalColor)
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelMedium,
                color = medalColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        MascotAvatarBadge(avatar = entry.avatarEnum, size = avatarSize)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.childName,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = metricValueText(entry, sortBy),
            style = MaterialTheme.typography.labelMedium,
            color = SparkCobalt
        )
    }
}

@Composable
private fun LeaderboardRowCard(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    sortBy: LeaderboardSort
) {
    val rankAccent = when (rank) {
        1 -> SparkAmber
        2 -> Color(0xFF64748B)
        3 -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentUser) BorderStroke(1.5.dp, SparkCobalt) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleLarge,
                color = rankAccent,
                modifier = Modifier.width(38.dp)
            )

            MascotAvatarBadge(avatar = entry.avatarEnum, size = 44.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.childName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SparkCobalt
                        ) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = entry.tierEnum.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val (metricIcon, metricColor) = when (sortBy) {
                LeaderboardSort.BRAIN_SCORE -> Icons.Rounded.Psychology to SparkCobalt
                LeaderboardSort.STARS -> Icons.Rounded.Star to SparkAmber
                LeaderboardSort.STREAK -> Icons.Rounded.LocalFireDepartment to SparkCoral
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = metricColor.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = metricIcon,
                        contentDescription = sortBy.label,
                        tint = metricColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = metricValueText(entry, sortBy),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (sortBy == LeaderboardSort.STARS) MaterialTheme.colorScheme.onSurface else metricColor
                    )
                }
            }
        }
    }
}

private fun metricValueText(entry: LeaderboardEntry, sortBy: LeaderboardSort): String = when (sortBy) {
    LeaderboardSort.BRAIN_SCORE -> "${entry.brainScore} IQ"
    LeaderboardSort.STARS -> "${entry.totalStars}"
    LeaderboardSort.STREAK -> "${entry.currentStreakDays}d"
}
