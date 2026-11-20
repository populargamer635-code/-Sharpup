package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MathSparkRepository
import com.example.domain.MathQuestionGenerator
import com.example.model.AvatarOption
import com.example.model.BrainQuestion
import com.example.model.ChildProfile
import com.example.model.DifficultyTier
import com.example.model.GameMode
import com.example.model.GameSession
import com.example.model.LeaderboardEntry
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class ActiveGameState(
    val mode: GameMode = GameMode.ARITHMETIC,
    val tier: DifficultyTier = DifficultyTier.EXPLORER,
    val currentQuestion: BrainQuestion? = null,
    val questionNumber: Int = 1,
    val totalQuestions: Int = 10,
    val score: Int = 0,
    val correctCount: Int = 0,
    val answeredCount: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val timeLeftSeconds: Int = 45,
    val elapsedSeconds: Int = 0,
    val selectedOption: Int? = null,
    val isAnswerCorrect: Boolean? = null,
    val showVisualBlocks: Boolean = true,
    val memoryRevealStep: Int = 0,
    val memoryReadyToAnswer: Boolean = true,
    val isFinished: Boolean = false,
    val starsEarned: Int = 0,
    val newlyUnlockedBadges: List<String> = emptyList(),
    val saveError: String? = null
)

class MathSparkViewModel(
    private val repository: MathSparkRepository,
    private val currentUserId: String,
    defaultDisplayName: String
) : ViewModel() {

    val profileState: StateFlow<UiState<ChildProfile>> = repository.observeProfile(currentUserId)
        .map<ChildProfile?, UiState<ChildProfile>> { profile ->
            UiState.Success(profile ?: ChildProfile(userId = currentUserId, childName = defaultDisplayName.ifBlank { "Math Explorer" }))
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val sessionsState: StateFlow<UiState<List<GameSession>>> = repository.observeRecentSessions(currentUserId)
        .map<List<GameSession>, UiState<List<GameSession>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Error observing sessions", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load recent games"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> = repository.observeLeaderboard(currentUserId)
        .map<List<LeaderboardEntry>, UiState<List<LeaderboardEntry>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(TAG, "Error observing leaderboard", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load leaderboard"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    private val _profileSaveMessage = MutableStateFlow<String?>(null)
    val profileSaveMessage: StateFlow<String?> = _profileSaveMessage.asStateFlow()

    private var timerJob: Job? = null
    private var memoryJob: Job? = null
    private var feedbackAdvanceJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureProfileExists(currentUserId, defaultDisplayName)
        }
    }

    fun startGame(mode: GameMode, tier: DifficultyTier) {
        cancelGameJobs()
        val firstQuestion = MathQuestionGenerator.generateQuestion(0, mode, tier)
        val hasMemorySteps = firstQuestion.memorySteps.isNotEmpty()
        _gameState.value = ActiveGameState(
            mode = mode,
            tier = tier,
            currentQuestion = firstQuestion,
            questionNumber = 1,
            totalQuestions = mode.questionCount,
            timeLeftSeconds = 45,
            showVisualBlocks = tier == DifficultyTier.EXPLORER,
            memoryRevealStep = if (hasMemorySteps) 0 else 0,
            memoryReadyToAnswer = !hasMemorySteps
        )

        if (hasMemorySteps) {
            runMemorySequence(firstQuestion.memorySteps.size)
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _gameState.value
                if (current.isFinished) break
                if (current.mode == GameMode.SPEED_SPRINT) {
                    val nextTime = (current.timeLeftSeconds - 1).coerceAtLeast(0)
                    _gameState.update {
                        it.copy(
                            timeLeftSeconds = nextTime,
                            elapsedSeconds = it.elapsedSeconds + 1
                        )
                    }
                    if (nextTime <= 0) {
                        finishGame()
                        break
                    }
                } else {
                    _gameState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    private fun runMemorySequence(totalSteps: Int) {
        memoryJob?.cancel()
        memoryJob = viewModelScope.launch {
            for (step in 0 until totalSteps) {
                _gameState.update { it.copy(memoryRevealStep = step, memoryReadyToAnswer = false) }
                delay(1250L)
            }
            _gameState.update { it.copy(memoryRevealStep = totalSteps, memoryReadyToAnswer = true) }
        }
    }

    fun replayMemorySteps() {
        val q = _gameState.value.currentQuestion ?: return
        if (q.memorySteps.isNotEmpty() && _gameState.value.selectedOption == null) {
            runMemorySequence(q.memorySteps.size)
        }
    }

    fun toggleVisualBlocks() {
        _gameState.update { it.copy(showVisualBlocks = !it.showVisualBlocks) }
    }

    fun submitAnswer(option: Int) {
        val state = _gameState.value
        val question = state.currentQuestion ?: return
        if (state.isFinished || state.selectedOption != null || !state.memoryReadyToAnswer) return

        val isCorrect = option == question.correctAnswer
        val newCombo = if (isCorrect) state.combo + 1 else 0
        val newBestCombo = maxOf(state.bestCombo, newCombo)
        val tierBonus = when (state.tier) {
            DifficultyTier.EXPLORER -> 10
            DifficultyTier.CHALLENGER -> 15
            DifficultyTier.GENIUS -> 22
        }
        val pointsEarned = if (isCorrect) tierBonus + (newCombo.coerceAtMost(5) * 4) else 0

        _gameState.update {
            it.copy(
                selectedOption = option,
                isAnswerCorrect = isCorrect,
                score = it.score + pointsEarned,
                correctCount = it.correctCount + if (isCorrect) 1 else 0,
                answeredCount = it.answeredCount + 1,
                combo = newCombo,
                bestCombo = newBestCombo
            )
        }

        val pauseMs = if (state.mode == GameMode.SPEED_SPRINT) 450L else 1200L
        feedbackAdvanceJob?.cancel()
        feedbackAdvanceJob = viewModelScope.launch {
            delay(pauseMs)
            advanceToNextQuestion()
        }
    }

    fun advanceToNextQuestion() {
        feedbackAdvanceJob?.cancel()
        val state = _gameState.value
        if (state.isFinished) return

        if (state.mode != GameMode.SPEED_SPRINT && state.questionNumber >= state.totalQuestions) {
            finishGame()
            return
        }

        val nextNum = state.questionNumber + 1
        val nextQ = MathQuestionGenerator.generateQuestion(nextNum - 1, state.mode, state.tier)
        val hasMemorySteps = nextQ.memorySteps.isNotEmpty()
        _gameState.update {
            it.copy(
                currentQuestion = nextQ,
                questionNumber = nextNum,
                selectedOption = null,
                isAnswerCorrect = null,
                memoryRevealStep = 0,
                memoryReadyToAnswer = !hasMemorySteps
            )
        }
        if (hasMemorySteps) {
            runMemorySequence(nextQ.memorySteps.size)
        }
    }

    private fun finishGame() {
        cancelGameJobs()
        val state = _gameState.value
        if (state.isFinished) return

        val totalAnswered = maxOf(1, state.answeredCount)
        val baseStars = state.correctCount * 2
        val comboBonus = if (state.bestCombo >= 5) 5 else if (state.bestCombo >= 3) 2 else 0
        val perfectBonus = if (state.mode != GameMode.SPEED_SPRINT && state.correctCount == state.totalQuestions) 6 else 0
        val stars = (baseStars + comboBonus + perfectBonus).coerceIn(1, 500)

        _gameState.update {
            it.copy(
                isFinished = true,
                starsEarned = stars
            )
        }

        val currentProfile = (profileState.value as? UiState.Success)?.data
            ?: ChildProfile(userId = currentUserId)

        viewModelScope.launch {
            val result = repository.recordGameSession(
                userId = currentUserId,
                currentProfile = currentProfile,
                mode = state.mode,
                difficultyTier = state.tier,
                score = state.score.toLong(),
                starsEarned = stars.toLong(),
                correctCount = state.correctCount.toLong(),
                totalQuestions = totalAnswered.toLong(),
                bestCombo = state.bestCombo.toLong(),
                durationSeconds = maxOf(1, state.elapsedSeconds).toLong()
            )
            result.onSuccess { badges ->
                _gameState.update { it.copy(newlyUnlockedBadges = badges, saveError = null) }
            }.onFailure { err ->
                _gameState.update { it.copy(saveError = err.localizedMessage ?: "Couldn't save score to cloud.") }
            }
        }
    }

    fun quitGameEarly() {
        cancelGameJobs()
    }

    fun saveProfileSettings(
        childName: String,
        avatar: AvatarOption,
        difficultyTier: DifficultyTier
    ) {
        viewModelScope.launch {
            val result = repository.updateProfileSettings(currentUserId, childName, avatar, difficultyTier)
            result.onSuccess {
                _profileSaveMessage.value = "Profile saved!"
            }.onFailure { err ->
                _profileSaveMessage.value = err.localizedMessage ?: "Could not save profile."
            }
        }
    }

    fun updateDifficultyQuick(difficultyTier: DifficultyTier) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        saveProfileSettings(currentProfile.childName, currentProfile.avatarEnum, difficultyTier)
    }

    fun clearProfileMessage() {
        _profileSaveMessage.value = null
    }

    private fun cancelGameJobs() {
        timerJob?.cancel()
        memoryJob?.cancel()
        feedbackAdvanceJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelGameJobs()
    }

    private companion object {
        const val TAG = "MathSparkVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
