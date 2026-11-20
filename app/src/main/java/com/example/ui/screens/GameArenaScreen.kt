package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrainQuestion
import com.example.model.GameMode
import com.example.model.MathBadges
import com.example.ui.components.CelebrationConfettiOverlay
import com.example.ui.components.CountingBlocksVisual
import com.example.ui.theme.SparkAmber
import com.example.ui.theme.SparkCobalt
import com.example.ui.theme.SparkCoral
import com.example.ui.theme.SparkMint
import com.example.ui.theme.SparkViolet
import com.example.viewmodel.ActiveGameState

@Composable
fun GameArenaScreen(
    gameState: ActiveGameState,
    onSubmitAnswer: (Int) -> Unit,
    onToggleVisualBlocks: () -> Unit,
    onReplayMemorySteps: () -> Unit,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    BackHandler {
        onExit()
    }

    if (gameState.isFinished) {
        RoundResultScreen(
            gameState = gameState,
            onPlayAgain = onPlayAgain,
            onBackHome = onExit
        )
        return
    }

    val question = gameState.currentQuestion ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Arena HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("arena_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Exit Game"
                    )
                }

                if (gameState.mode == GameMode.SPEED_SPRINT) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (gameState.timeLeftSeconds <= 10) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = "Time left",
                                tint = if (gameState.timeLeftSeconds <= 10) SparkCoral else SparkCobalt,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${gameState.timeLeftSeconds}s",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (gameState.timeLeftSeconds <= 10) SparkCoral else SparkCobalt
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${gameState.questionNumber} / ${gameState.totalQuestions}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (gameState.combo >= 2) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Combo",
                                    tint = SparkCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${gameState.combo}x",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SparkCoral
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Score",
                                tint = SparkAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = gameState.score.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Progress bar
            val progressFraction = if (gameState.mode == GameMode.SPEED_SPRINT) {
                (gameState.timeLeftSeconds / 45f).coerceIn(0f, 1f)
            } else {
                (gameState.questionNumber.toFloat() / gameState.totalQuestions.toFloat()).coerceIn(0f, 1f)
            }
            LinearProgressIndicator(
                progress = { progressFraction },
                color = if (gameState.mode == GameMode.SPEED_SPRINT) SparkCoral else SparkCobalt,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
            )

            // Main Question Card
            QuestionChallengeCard(
                question = question,
                gameState = gameState,
                onToggleVisualBlocks = onToggleVisualBlocks,
                onReplayMemorySteps = onReplayMemorySteps
            )

            // Feedback Banner (correct/try again)
            AnimatedVisibility(
                visible = gameState.selectedOption != null && gameState.mode != GameMode.SPEED_SPRINT,
                enter = fadeIn() + scaleIn(initialScale = 0.94f)
            ) {
                val isCorrect = gameState.isAnswerCorrect == true
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = if (isCorrect) SparkMint else SparkCoral,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isCorrect) "Awesome Brain Power!" else "Good Try! Keep Learning",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCorrect) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCorrect) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 2x2 Tactile Answer Options
            if (gameState.memoryReadyToAnswer) {
                val options = question.options
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (rowIdx in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (colIdx in 0 until 2) {
                                val optIndex = rowIdx * 2 + colIdx
                                val optValue = options.getOrNull(optIndex)
                                if (optValue != null) {
                                    AnswerOptionTile(
                                        value = optValue,
                                        selectedOption = gameState.selectedOption,
                                        correctAnswer = question.correctAnswer,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSubmitAnswer(optValue)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("answer_option_$optIndex")
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Watch closely and hold the number in your mind...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuestionChallengeCard(
    question: BrainQuestion,
    gameState: ActiveGameState,
    onToggleVisualBlocks: () -> Unit,
    onReplayMemorySteps: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = question.mode.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Text(
                text = question.promptTop,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (question.sequenceChips.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    question.sequenceChips.forEach { chip ->
                        val isMissing = chip == "?"
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isMissing) SparkAmber.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isMissing) BorderStroke(2.dp, SparkAmber) else null
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (isMissing) SparkAmber else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else if (question.memorySteps.isNotEmpty()) {
                if (!gameState.memoryReadyToAnswer) {
                    val stepText = question.memorySteps.getOrElse(gameState.memoryRevealStep) { "" }
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = SparkViolet.copy(alpha = 0.14f),
                        border = BorderStroke(2.dp, SparkViolet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Step ${gameState.memoryRevealStep + 1} of ${question.memorySteps.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SparkViolet
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = question.mainDisplay,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    TextButton(
                        onClick = onReplayMemorySteps,
                        enabled = gameState.selectedOption == null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay,
                            contentDescription = "Replay Steps",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Steps Again")
                    }
                }
            } else {
                Text(
                    text = question.mainDisplay,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            if (question.visualLeftCount != null && question.visualRightCount != null && question.visualOperator != null) {
                TextButton(onClick = onToggleVisualBlocks) {
                    Icon(
                        imageVector = if (gameState.showVisualBlocks) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (gameState.showVisualBlocks) stringResource(R.string.hide_visual_helper)
                        else stringResource(R.string.show_visual_helper)
                    )
                }

                AnimatedVisibility(visible = gameState.showVisualBlocks) {
                    CountingBlocksVisual(
                        leftCount = question.visualLeftCount,
                        rightCount = question.visualRightCount,
                        operator = question.visualOperator
                    )
                }
            }
        }
    }
}

@Composable
private fun AnswerOptionTile(
    value: Int,
    selectedOption: Int?,
    correctAnswer: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedOption == value
    val hasAnswered = selectedOption != null
    val isThisCorrect = value == correctAnswer

    val containerColor = when {
        !hasAnswered -> MaterialTheme.colorScheme.surface
        isThisCorrect -> SparkMint
        isSelected && !isThisCorrect -> SparkCoral
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val contentColor = when {
        !hasAnswered -> MaterialTheme.colorScheme.onSurface
        isThisCorrect || isSelected -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "option_scale"
    )

    Button(
        onClick = onClick,
        enabled = !hasAnswered,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        border = if (!hasAnswered) BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (hasAnswered) 0.dp else 3.dp),
        modifier = modifier
            .height(78.dp)
            .scale(scale)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RoundResultScreen(
    gameState: ActiveGameState,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val total = maxOf(1, gameState.answeredCount)
    val accuracyPct = (gameState.correctCount * 100) / total

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        CelebrationConfettiOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.size(118.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_sparky_mascot),
                    contentDescription = "Sparky cheering for you",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (accuracyPct >= 80) "Brilliant Brainwork!" else "Great Effort!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You finished ${gameState.mode.title} (${gameState.tier.label})",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatColumn(
                            label = "Stars Earned",
                            value = "+${gameState.starsEarned}",
                            tint = SparkAmber
                        )
                        ResultStatColumn(
                            label = "Accuracy",
                            value = "$accuracyPct%",
                            tint = SparkMint
                        )
                        ResultStatColumn(
                            label = "Best Combo",
                            value = "${gameState.bestCombo}x",
                            tint = SparkCoral
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = SparkCobalt
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Score: ${gameState.score} pts • ${gameState.correctCount}/$total right",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (gameState.newlyUnlockedBadges.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "New Trophy Unlocked!",
                                style = MaterialTheme.typography.titleMedium,
                                color = SparkAmber
                            )
                            gameState.newlyUnlockedBadges.forEach { badgeId ->
                                val def = MathBadges.all.firstOrNull { it.id == badgeId }
                                if (def != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AutoAwesome,
                                                contentDescription = null,
                                                tint = SparkAmber
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = def.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Text(
                                                    text = def.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (gameState.saveError != null) {
                        Text(
                            text = gameState.saveError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SparkCobalt),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_again_button")
            ) {
                Text(
                    text = stringResource(R.string.play_again),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("back_home_button")
            ) {
                Text(
                    text = stringResource(R.string.back_home),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ResultStatColumn(
    label: String,
    value: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
