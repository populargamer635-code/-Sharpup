package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class DifficultyTier(
    val label: String,
    val ageHint: String,
    val description: String
) {
    EXPLORER("Explorer", "Ages 5–7", "Friendly numbers 1–20 & visual counting blocks"),
    CHALLENGER("Challenger", "Ages 8–10", "Times tables, division & tricky patterns"),
    GENIUS("Genius", "Ages 11+", "Fast multi-step math & brain-stretching logic");

    companion object {
        fun fromRaw(raw: String?): DifficultyTier =
            entries.firstOrNull { it.name == raw } ?: EXPLORER
    }
}

enum class GameMode(
    val title: String,
    val subtitle: String,
    val brainPillar: String,
    val questionCount: Int
) {
    ARITHMETIC(
        title = "Number Quest",
        subtitle = "Add, subtract, multiply & divide with helper blocks",
        brainPillar = "Accuracy",
        questionCount = 10
    ),
    SPEED_SPRINT(
        title = "Lightning Sprint",
        subtitle = "45-second rapid mental math dash!",
        brainPillar = "Speed",
        questionCount = 99
    ),
    PATTERN_LOGIC(
        title = "Pattern Detective",
        subtitle = "Crack number sequences & hidden rules",
        brainPillar = "Logic",
        questionCount = 8
    ),
    MEMORY_BALANCE(
        title = "Brain Chain & Scale",
        subtitle = "Remember steps & balance mystery equations",
        brainPillar = "Memory",
        questionCount = 8
    );

    companion object {
        fun fromRaw(raw: String?): GameMode =
            entries.firstOrNull { it.name == raw } ?: ARITHMETIC
    }
}

enum class AvatarOption(val id: String, val label: String) {
    OWL("owl", "Sparky Owl"),
    FOX("fox", "Clever Fox"),
    PANDA("panda", "Zen Panda"),
    ROCKET("rocket", "Math Rocket"),
    STAR("star", "Super Star"),
    DINO("dino", "Brainy Dino");

    companion object {
        fun fromId(raw: String?): AvatarOption =
            entries.firstOrNull { it.id == raw } ?: OWL
    }
}

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String
)

object MathBadges {
    val all = listOf(
        BadgeDefinition("first_spark", "First Spark", "Complete your very first math adventure", "bolt"),
        BadgeDefinition("perfect_round", "Sharp Shooter", "Score 100% accuracy in a Number Quest or Logic round", "star"),
        BadgeDefinition("combo_master", "Combo Rocket", "Get 5 or more answers right in a row", "rocket"),
        BadgeDefinition("speed_flash", "Lightning Brain", "Solve 10+ problems in a single Lightning Sprint", "timer"),
        BadgeDefinition("pattern_sleuth", "Pattern Pro", "Complete a Pattern Detective adventure", "extension"),
        BadgeDefinition("memory_wizard", "Memory Wizard", "Complete a Brain Chain & Scale adventure", "psychology"),
        BadgeDefinition("star_collector", "100 Star Club", "Collect 100 shining stars in total", "trophy"),
        BadgeDefinition("streak_keeper", "3-Day Streak", "Train your brain 3 days in a row", "flame")
    )
}

@Keep
@IgnoreExtraProperties
data class ChildProfile(
    val userId: String = "",
    val childName: String = "Math Explorer",
    val avatarId: String = "owl",
    val difficultyTier: String = "EXPLORER",
    val totalStars: Long = 0L,
    val brainScore: Long = 100L,
    val arithmeticSkill: Long = 20L,
    val speedSkill: Long = 20L,
    val logicSkill: Long = 20L,
    val memorySkill: Long = 20L,
    val currentStreakDays: Long = 0L,
    val bestStreakCombo: Long = 0L,
    val sessionsCompleted: Long = 0L,
    val totalCorrect: Long = 0L,
    val totalAnswered: Long = 0L,
    val lastPlayedDate: String = "",
    val unlockedBadges: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val tierEnum: DifficultyTier get() = DifficultyTier.fromRaw(difficultyTier)
    val avatarEnum: AvatarOption get() = AvatarOption.fromId(avatarId)
    val overallAccuracyPercent: Int
        get() = if (totalAnswered <= 0L) 0 else ((totalCorrect * 100L) / totalAnswered).toInt().coerceIn(0, 100)
}

@Keep
@IgnoreExtraProperties
data class GameSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = "ARITHMETIC",
    val difficultyTier: String = "EXPLORER",
    val score: Long = 0L,
    val starsEarned: Long = 0L,
    val correctCount: Long = 0L,
    val totalQuestions: Long = 1L,
    val bestCombo: Long = 0L,
    val durationSeconds: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val modeEnum: GameMode get() = GameMode.fromRaw(mode)
    val tierEnum: DifficultyTier get() = DifficultyTier.fromRaw(difficultyTier)
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    val userId: String = "",
    val childName: String = "Math Explorer",
    val avatarId: String = "owl",
    val difficultyTier: String = "EXPLORER",
    val totalStars: Long = 0L,
    val brainScore: Long = 100L,
    val currentStreakDays: Long = 0L,
    val publicEntry: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val avatarEnum: AvatarOption get() = AvatarOption.fromId(avatarId)
    val tierEnum: DifficultyTier get() = DifficultyTier.fromRaw(difficultyTier)
}

data class BrainQuestion(
    val id: Int,
    val mode: GameMode,
    val promptTop: String,
    val mainDisplay: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val explanation: String,
    val visualLeftCount: Int? = null,
    val visualRightCount: Int? = null,
    val visualOperator: String? = null,
    val sequenceChips: List<String> = emptyList(),
    val memorySteps: List<String> = emptyList()
)
